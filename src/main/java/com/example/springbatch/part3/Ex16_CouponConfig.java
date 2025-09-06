package com.example.springbatch.part3;

import com.example.springbatch.common.HttpPageItemReaderBuilder;
import com.example.springbatch.common.MemberResponse;
import com.example.springbatch.common.entity.Coupon;
import com.example.springbatch.common.listener.ChunkDurationTrackerListener;
import com.example.springbatch.common.listener.StepDurationTrackerListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
    - 실행방법
      - Program arguments에 옵션 추가 후 Run >>> --job.name=couponJob

    - 강의정리
      - 외부서버로부터 데이터 읽어와서 처리하기 (외부서버인 MemberController를 실행해야한다.)
      - 오류 발생 시 SKIP
        - 외부통신은 다양한 사유에서 장애가 발생할 수 있다. (포맷 오류, 간헐적 통신 오류 등)
          이 때, 장애가 발생한 chunk만 skip할 것인지 이후 작업을 모두 중지할 것인지 비즈니스 요구사항에 따라서 전략을 달리할 수 있다.
          만약 skip해도 문제가 되지 않는다면, 장애발생 지점부터 이후 모든 chunk를 다시 시작하는 것보다 skip후 장애 발생 데이터만 매뉴얼로 처리하는 것이 좋은 전략이 될 수도 있다.
          (장시간 실행되는 배치의 경우, 처음부터 다시 시작하는 것보다 실패한 부분을 재처리할 수 있는 로직이 운영 효율성을 높일 수 있음)
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex16_CouponConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final int chunkSize = 10;

    @Bean
    public Job couponJob(
            Step couponStep
    ) {
        return new JobBuilder("couponJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(couponStep)
                .build();
    }

    @Bean
    public Step couponStep(
            ItemReader<MemberResponse> couponReader
    ) {
        return new StepBuilder("couponStep", jobRepository)
                .<MemberResponse, Coupon>chunk(chunkSize, transactionManager)
                // Step 소요 시간 측정
                .listener(new StepDurationTrackerListener())
                .reader(couponReader)
                .processor(couponProcessor())
                .writer(couponWriter())
                // Chunk 소요 시간 측정
                .listener(new ChunkDurationTrackerListener())
                .build();
    }

    /**
     * RestTemplate을 Bean으로 등록하여 다른 곳에서도 재사용할 수 있도록 합니다.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ItemReader<MemberResponse> couponReader(RestTemplate restTemplate) {
        return new HttpPageItemReaderBuilder<MemberResponse>()
                .baseUrl("http://localhost:8081/api/v1/members") // 요청할 API의 기본 URL
                .size(chunkSize)
                .restTemplate(restTemplate)
                .responseType(new ParameterizedTypeReference<>() {
                })
                .ignoreErrors(true) // 오류 발생시 무시
                .build();
    }

    private ItemProcessor<MemberResponse, Coupon> couponProcessor() {
        return member -> new Coupon(
                "회원가입 쿠폰",
                BigDecimal.valueOf(1000),
                LocalDate.now().plusDays(30),
                false,
                member.getId()
        );
    }

    @Bean
    public JpaItemWriter<Coupon> couponWriter() {
        return new JpaItemWriterBuilder<Coupon>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
