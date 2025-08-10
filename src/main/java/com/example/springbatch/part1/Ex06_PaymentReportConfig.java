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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Collections;

/*
    - 실행방법
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=paymentReportJobEx06 paymentDate=2025-05-01

    - 강의정리
       - paymentReportProcessor를 처리하다 오류가 발생하면, 기본적으로는 배치는 모두 실패처리된다.
       - 하지만 faultTolerant()와 retry 옵션을 사용해서 핸들링 가능한 오류는 retry 처리하여 배치를 정상 완료시킬 수 있다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex06_PaymentReportConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PartnerCorporationService partnerCorporationService;

    @Bean
    public Job paymentReportJobEx06(
            Step paymentReportStepEx06
    ) {
        return new JobBuilder("paymentReportJobEx06", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paymentReportStepEx06)
                .build();
    }

    @Bean
    public Step paymentReportStepEx06(
            JpaPagingItemReader<PaymentSource> paymentReportReader
    ) {
        return new StepBuilder("paymentReportStepEx06", jobRepository)
                .<PaymentSource, Payment>chunk(10, transactionManager)
                .reader(paymentReportReader)
                .processor(paymentReportProcessor())
                .writer(paymentReportWriter())
                .faultTolerant()
                .retry(PartnerHttpException.class)
                .retryLimit(10)
                .retryPolicy(new TimeoutRetryPolicy(1000L))
//                .retryPolicy(new SimpleRetryPolicy(
//                        10,
//                        new BinaryExceptionClassifier(Collections.singletonMap(PartnerHttpException.class, Boolean.TRUE))
//                ))
//                .retryPolicy(new NeverRetryPolicy())
//                .retryPolicy(new AlwaysRetryPolicy())
//                .retryPolicy(new CompositeRetryPolicy())
                .noRollback(InvalidPaymentAmountException.class)
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

            final String partnerCorpName = partnerCorporationService.getPartnerCorpName(paymentSource.getPartnerBusinessRegistrationNumber());
            final Payment payment = new Payment(
                    null,
                    paymentSource.getFinalAmount(),
                    paymentSource.getPaymentDate(),
                    partnerCorpName,
                    "PAYMENT"
            );

            log.info("Processor payment: {}", payment);
            return payment;
        };
    }

    @Bean
    public ItemWriter<Payment> paymentReportWriter() {
        return chunk -> {
            for (Payment payment : chunk) {
                log.info("Writer payment: {}", payment);
            }
        };
    }

}