package com.example.springbatch.part3;

import com.example.springbatch.common.ArgumentProperties;
import com.example.springbatch.common.PaymentDailyStatisticsRecoveryService;
import com.example.springbatch.common.PaymentStatisticsDailySum;
import com.example.springbatch.common.listener.ChunkDurationTrackerListener;
import com.example.springbatch.common.listener.PrepareTargetDatesJobListener;
import com.example.springbatch.common.listener.StepDurationTrackerListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
    - 실행방법
      - 첫번째 커밋 : setup.sql에서 'Part 3 > Ch 1' 데이터 추가
      - 세번째 커밋 : setup.sql에서 'Part 3 > Ch 5 ~ 7' 데이터 추가

    - 강의목적
       - 재처리가 가능한 배치 만들기
       - 강의 진행내용을 토대로 해당 파일을 점진적으로 수정하여 commit 했기 때문에 커밋 이력을 보고 변경사항 추적 필요

    - 첫번째 커밋
       - 결제 데이터를 통계내기 위하여 일별 집계 테이블에 데이터를 생성한다.
       - 실행 : Program arguments에 옵션 추가 후 Run >>> --job.name=paymentStatisticsJob paymentDate=2025-01-01
    - 두번째 커밋
       - 첫번째 커밋 방식의 문제점
         - 결제 데이터가 누락되어 데이터를 추가하고, 통계를 다시 맞춰달라는 요구사항이 있을 경우 대부분의 데이터가 중복으로 생성되는 문제가 발생한다.
       - 이를 해결하는 가장 간단한 방법은 기존 데이터를 삭제하고 다시 저장하는 방식이다.
       - 실행 : Program arguments에 옵션 추가 후 Run >>> --job.name=paymentStatisticsJob
    - 세번째 커밋
       - 두번째 커밋 방식의 문제점
         - 기존 데이터를 삭제하고 다시 저장하는 방식은 데이터양이 적을 때는 상관없지만 데이터양이 많을 때는 딜레이가 발생할 수도 있다.
         - 이처럼 1건만 추가하기 위하여 특정 사업자의 특정일의 모든 데이터를 지우고 다시 실행하는건 비효율적일 수 있다.
       - 이를 해결하기 위해서 재처리가 가능한 batch로 개선한다. (더욱 유연하게 변경)
         - 결제 건이 누락되어서 기존 데이터에 누락된 만큼 합산하여 update 하는 케이스
         - 합계된 데이터에 없는 사용자의 누락된 데이터가 추가되는 경우에는 insert 하는 케이스
         - 누락된 데이터가 +100, -100과 같이 합산하면 0이 되므로 최종적으로는 데이터 변경이 없는 케이스
       - 실행 : Program arguments에 옵션 추가 후 Run >>> --job.name=paymentStatisticsJob
         - 처음 실행하면, 데이터가 없는 상태여서 '새로운 대상 저장: 2건'이 콘솔에 찍힘.
         - 재실행하면, 데이터가 이미 있으며 변경사항이 없으므로 '기존 데이터와 amount 일치 (변경 없음)'이 콘솔에 찍힘
         - setup.sql에 임의의 데이터를 추가하고 재실행하면, 이미 있는 데이터이지만 변경사항이 생기므로 '기존 데이터와 amount 불일치(변경 대상): 사업자번호=10002000, 결제일자=2025-01-01, 기존 amount=400.00, 새 amount=500.00' 와 같이 콘솔에 찍힘.
     - 네번째 커밋
       - 세번째 커밋 방식의 문제점
         - 배치가 전날 결제일을 기준으로 동작한다고 가정해보자.
           만약 누락된 데이터가 전날 결제일이 아니고 더 이전이라면, 현재 조회쿼리에서는 누락 데이터를 조회할 수가 없어서 처리가 불가능하다.
           이를 보완하기 위해서 조회 기준을 결제일이 아닌 updatedAt으로 한다.
         - createdAt이 아닌 updatedAt을 기준으로 한 이유
           - 만약 누락된 데이터 100원이 1/3일에 들어왔다고 가정. 하지만 이 값이 잘못된 값이라 1/4일에 200원으로 변경이 됨.
             createAt을 기준으로 한다면, 재변경이 일어난 100 -> 200원 수정을 알아챌 수 없음. 따라서 최종변경사항을 알 수 있는 updatedAt을 기준으로 하는게 현재의 비즈니스에는 적당하다.
       - 실행 : Program arguments에 옵션 추가 후 Run >>> --job.name=paymentStatisticsJob
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex15_PaymentStatisticsJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final ArgumentProperties properties;
    private final PrepareTargetDatesJobListener prepareTargetDatesJobListener;
    private final PaymentDailyStatisticsRecoveryService paymentDailyStatisticsRecoveryService;

    private final int chunkSize = 100;

    /**
     * 일일 결제 통계 데이터를 생성하는 Spring Batch Job을 정의합니다.
     */
    @Bean
    public Job paymentStatisticsJob(Step paymentStatisticsStep) {
        return new JobBuilder("paymentStatisticsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(prepareTargetDatesJobListener)
                .start(paymentStatisticsStep)
                .build();
    }

    /**
     * Job의 핵심 로직을 담당하는 Step을 정의합니다.
     * Reader, Processor, Writer를 사용하여 데이터를 읽고, 가공하고, 저장합니다.
     */
    @Bean
    public Step paymentStatisticsStep(
            JdbcCursorItemReader<PaymentStatisticsDailySum> paymentStatisticsReader,
            ItemWriter<PaymentStatisticsDailySum> paymentStatisticsWriter
    ) {
        return new StepBuilder("paymentStatisticsStep", jobRepository)
                .<PaymentStatisticsDailySum, PaymentStatisticsDailySum>chunk(chunkSize, transactionManager)
                .listener(new StepDurationTrackerListener()) // Step 소요 시간 측정 리스너
                .reader(paymentStatisticsReader)
                .writer(paymentStatisticsWriter)
                .listener(new ChunkDurationTrackerListener()) // Chunk 소요 시간 측정 리스너
                .build();
    }

    /**
     * [Reader]
     * 특정 날짜의 결제 데이터를 사업자 번호 기준으로 합산하여 읽어옵니다.
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<PaymentStatisticsDailySum> paymentStatisticsReader() {
        final Set<LocalDate> targetPaymentDates = properties.getTargetPaymentDates();
        String whereConditions = targetPaymentDates.stream()
                .map(date -> String.format("(payment_date_time >= '%s 00:00:00' AND payment_date_time < '%s 00:00:00')", date, date.plusDays(1)))
                .collect(Collectors.joining(" OR "));

        // MySQL 기준 SQL 쿼리
        String sql = String.format("""
                SELECT
                    SUM(amount) as totalAmount,
                    corp_name as corpName,
                    business_registration_number as businessRegistrationNumber,
                    DATE(payment_date_time) as paymentDate
                FROM payment_source_v2
                WHERE %s
                GROUP BY business_registration_number, corp_name, DATE(payment_date_time)
                """, whereConditions);

        return new JdbcCursorItemReaderBuilder<PaymentStatisticsDailySum>()
                .name("paymentStatisticsReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper(new BeanPropertyRowMapper<>(PaymentStatisticsDailySum.class))
                .fetchSize(Integer.MIN_VALUE)
                .verifyCursorPosition(false)
                .build();
    }

    /**
     * [Writer]
     * Processor가 전달한 PaymentDailyStatistics 엔티티를 DB에 저장합니다.
     * JpaItemWriter는 엔티티의 상태에 따라 자동으로 INSERT 또는 UPDATE를 수행합니다.
     */
    @Bean
    public ItemWriter<PaymentStatisticsDailySum> writer() {
        return chunk -> {
            @SuppressWarnings("unchecked") final List<PaymentStatisticsDailySum> items = (List<PaymentStatisticsDailySum>) chunk.getItems();
            paymentDailyStatisticsRecoveryService.recovery(items);
        };
    }

}
