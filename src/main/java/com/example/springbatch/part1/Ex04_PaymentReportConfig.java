package com.example.springbatch.part1;

import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

/*
    - 실행방법
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=paymentReportJobEx04 paymentDate=2025-05-01

    - 강의정리
       - ItemReader : JPA 페이징 방식을 통해 처리할 데이터를 조회할 수 있다.
       - ItemProcessor : ItemReader를 통해 읽은 데이터를 가공하여 ItemWriter로 전달한다.
       - ItemWriter : 최종적으로 데이터를 저장한다.

       위 방식은 모두 Chunk 기반으로 동작한다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex04_PaymentReportConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job paymentReportJobEx04(
            Step paymentReportStepEx04
    ) {
        return new JobBuilder("paymentReportJobEx04", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paymentReportStepEx04)
                .build();
    }

    @Bean
    public Step paymentReportStepEx04(
            JpaPagingItemReader<PaymentSource> paymentReportReader
    ) {
        return new StepBuilder("paymentReportStepEx04", jobRepository)
                .<PaymentSource, Payment>chunk(10, transactionManager)
                .reader(paymentReportReader)
                .processor(paymentReportProcessor())
                .writer(paymentReportWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<PaymentSource> paymentReportReader(
            @Value("#{jobParameters['paymentDate']}") LocalDate paymentDate
    ) {
        return new JpaPagingItemReaderBuilder<PaymentSource>()
                .name("paymentSourceItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT ps FROM PaymentSource ps WHERE ps.paymentDate = :paymentDate")
                .parameterValues(Collections.singletonMap("paymentDate", paymentDate))
                .pageSize(10)
                .build();
    }

    private ItemProcessor<PaymentSource, Payment> paymentReportProcessor() {
        return paymentSource -> {
            if (paymentSource.getFinalAmount().compareTo(BigDecimal.ZERO) == 0) {
                return null; // ItemProcessor에서 null로 반환되면 writer에는 해당 데이터가 전달되지 않는다. (필터링 개념)
            }
            return new Payment(
                    null,
                    paymentSource.getFinalAmount(),
                    paymentSource.getPaymentDate(),
                    "PAYMENT"
            );
        };
    }

    @Bean
    public JpaItemWriter<Payment> paymentReportWriter() {
        JpaItemWriter<Payment> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

}