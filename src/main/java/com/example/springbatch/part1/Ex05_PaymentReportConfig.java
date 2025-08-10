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
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Collections;

/*
    - 실행방법
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=paymentReportJobEx05 paymentDate=2025-05-01

    - 강의정리
       - paymentReportProcessor를 처리하다 오류가 발생하면, 기본적으로는 배치는 모두 실패처리된다.
       - 하지만 faultTolerant()와 skip 옵션을 사용해서 핸들링 가능한 오류는 skip 처리하여 배치를 정상 완료시킬 수 있다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex05_PaymentReportConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    // @Bean
    public Job paymentReportJobEx05(
            Step paymentReportStepEx05
    ) {
        return new JobBuilder("paymentReportJobEx05", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(paymentReportStepEx05)
                .build();
    }

    // @Bean
    public Step paymentReportStepEx05(
            JpaPagingItemReader<PaymentSource> paymentReportReader
    ) {
        // FaultTolerantStepBuilder 을 통해 기본 정책 할당, 기본 Policy 정책, Skip limit
        // FaultTolerantChunkProcessor 실질적으로 폴트 톨러런스 내결함 성의 관한 내용이 동작합니다.
        return new StepBuilder("paymentReportStepEx05", jobRepository)
                .<PaymentSource, Payment>chunk(10, transactionManager)
                .reader(paymentReportReader)
                .processor(paymentReportProcessor())
                .writer(paymentReportWriter())
                .faultTolerant()
                .skip(InvalidPaymentAmountException.class) // InvalidPaymentAmountException 예외 발생 시 skip
                .skipLimit(2) // 최대 2번까지 skip 허용
//                .skipPolicy(new LimitCheckingItemSkipPolicy())
//                .skipPolicy(new LimitCheckingItemSkipPolicy(
//                        10, // 최대 10번까지 skip 허용
//                        throwable -> { // 예외에 따라 skip 여부 결정
//                            if (throwable instanceof InvalidPaymentAmountException) {
//                                return true; // InvalidPaymentAmountException 발생 시 skip
//                            } else if (throwable instanceof IllegalStateException) {
//                                return false; // IllegalStateException 발생 시 skip하지 않음
//                            } else {
//                                return false; // 그 외 예외는 skip하지 않음
//                            }
//                        }
//                ))
//                .skipPolicy(new AlwaysSkipItemSkipPolicy()) // 항상 skip하는 정책
//                .skipPolicy(new NeverSkipItemSkipPolicy()) // 절대 skip하지 않는 정책
                .build();
    }

    // @Bean
    // @StepScope
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
            // 할인 금액이 0이 아닌 경우(양수 음수)
            if (paymentSource.getDiscountAmount().signum() == -1 || true) {
                // 할인 금액이 0이 아닌 경우의 처리 로직
                final String msg = "할인 금액이 0이 아닌 결제는 처리할 수 없습니다. 현재 할인 금액: " + paymentSource.getDiscountAmount();
                log.error(msg);
                throw new InvalidPaymentAmountException(msg);
            }

            final Payment payment = new Payment(
                    null,
                    paymentSource.getFinalAmount(),
                    paymentSource.getPaymentDate(),
                    "PAYMENT"
            );

            log.info("Processor payment: {}", payment);
            return payment;
        };
    }

    // @Bean
    public ItemWriter<Payment> paymentReportWriter() {
        return chunk -> {
            for (Payment payment : chunk) {
                log.info("Writer payment: {}", payment);
            }
        };
    }

}