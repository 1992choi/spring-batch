package com.example.springbatch.part3;

import com.example.springbatch.common.entity.PaymentDailyStatistics;
import com.example.springbatch.common.listener.ChunkDurationTrackerListener;
import com.example.springbatch.common.listener.StepDurationTrackerListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;

/*
    - 실행방법
      - setup.sql에서 'Part 3 > Ch 1' 데이터 추가
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=paymentStatisticsJob paymentDate=2025-01-01

    - 강의목적
       - 재처리가 가능한 배치 만들기
       - 강의 진행내용을 토대로 해당 파일을 점진적으로 수정하여 commit 했기 때문에 커밋 이력을 보고 변경사항 추적 필요

    - 첫번째 커밋
       - 결제 데이터를 통계내기 위하여 일별 집계 테이블에 데이터를 생성한다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex15_PaymentStatisticsJobConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;

    private final int chunkSize = 100;

    /**
     * 일일 결제 통계 데이터를 생성하는 Spring Batch Job을 정의합니다.
     */
    @Bean
    public Job paymentStatisticsJob(Step paymentStatisticsStep) {
        return new JobBuilder("paymentStatisticsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
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
            ItemProcessor<PaymentStatisticsDailySum, PaymentDailyStatistics> paymentStatisticsProcessor,
            JpaItemWriter<PaymentDailyStatistics> paymentStatisticsWriter
    ) {
        return new StepBuilder("paymentStatisticsStep", jobRepository)
                .<PaymentStatisticsDailySum, PaymentDailyStatistics>chunk(chunkSize, transactionManager)
                .listener(new StepDurationTrackerListener()) // Step 소요 시간 측정 리스너
                .reader(paymentStatisticsReader)
                .processor(paymentStatisticsProcessor)
                .writer(paymentStatisticsWriter)
                .listener(new ChunkDurationTrackerListener()) // Chunk 소요 시간 측정 리스너
                .build();
    }

    /**
     * [Reader]
     * 특정 날짜의 결제 데이터를 사업자 번호 기준으로 합산하여 읽어옵니다.
     *
     * @param paymentDate Job Parameter로 실행 시점에 날짜를 전달받습니다. (형식: yyyy-MM-dd)
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<PaymentStatisticsDailySum> paymentStatisticsReader(
            @Value("#{jobParameters['paymentDate']}") LocalDate paymentDate
    ) {
        // MySQL 기준 SQL 쿼리
        String sql = String.format("""
                SELECT
                    SUM(amount) as totalAmount,
                    corp_name as corpName,
                    business_registration_number as businessRegistrationNumber
                FROM payment_source_v2
                WHERE payment_date_time >= '%s 00:00:00'
                  AND payment_date_time < '%s 00:00:00'
                GROUP BY business_registration_number, corp_name
                """, paymentDate, paymentDate.plusDays(1));

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
     * [Processor]
     * Reader에서 읽어온 집계 데이터(DTO)를 실제 저장할 엔티티(PaymentDailyStatistics)로 변환합니다.
     * 이미 해당 날짜/사업자번호로 저장된 통계가 있다면 금액을 더하고, 없다면 새로 생성합니다. (UPSERT 로직)
     */
    @Bean
    @StepScope
    public ItemProcessor<PaymentStatisticsDailySum, PaymentDailyStatistics> processor(
            @Value("#{jobParameters['paymentDate']}") LocalDate paymentDate
    ) {
        return dto -> new PaymentDailyStatistics(
                dto.corpName,
                dto.businessRegistrationNumber,
                dto.totalAmount,
                paymentDate
        );
    }

    /**
     * [Writer]
     * Processor가 전달한 PaymentDailyStatistics 엔티티를 DB에 저장합니다.
     * JpaItemWriter는 엔티티의 상태에 따라 자동으로 INSERT 또는 UPDATE를 수행합니다.
     */
    @Bean
    public JpaItemWriter<PaymentDailyStatistics> writer() {
        return new JpaItemWriterBuilder<PaymentDailyStatistics>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    /**
     * Reader가 SQL 조회 결과를 매핑할 DTO(Data Transfer Object) 클래스입니다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class PaymentStatisticsDailySum {
        private BigDecimal totalAmount;
        private String corpName;
        private String businessRegistrationNumber;
    }

}
