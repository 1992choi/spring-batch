package com.example.springbatch.part1;

import com.example.springbatch.common.listener.JobDurationTrackerListener;
import com.example.springbatch.common.entity.Payment;
import com.example.springbatch.common.entity.PaymentSource;
import com.example.springbatch.common.listener.*;
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
      - --job.name=paymentReportJobEx07 paymentDate=2025-05-01

    - 강의정리
       -  Listener는 배치 실행 과정에서 발생하는 특정 이벤트 시점에 개입해서 추가 로직을 실행할 수 있도록 도와주는 콜백(Callback) 역할을 한다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex07_PaymentReportConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job paymentReportJobEx06(
            Step paymentReportStepEx06
    ) {
        return new JobBuilder("paymentReportJobEx07", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobDurationTrackerListener()) // listener 등록
                .start(paymentReportStepEx06)
                .build();
    }

    @Bean
    public Step paymentReportStepEx06(
            JpaPagingItemReader<PaymentSource> paymentReportReader
    ) {
        return new StepBuilder("paymentReportStepEx07", jobRepository)
                .<PaymentSource, Payment>chunk(10, transactionManager)
                .listener(new StepDurationTrackerListener()) // listener 등록
                .reader(paymentReportReader)
                .processor(paymentReportProcessor())
                .writer(paymentReportWriter())
                /*
                    ChunkListener 등록
                    ItemReadListener 등록
                    ItemProcessListener 등록
                    ItemWriteListener 등록
                 */
                .listener(new SampleChunkListener())
                .listener(new SampleItemReadListener())
                .listener(new SampleItemProcessListener())
                .listener(new SampleItemWriterListener())
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
            final Payment payment = new Payment(
                    null,
                    paymentSource.getFinalAmount(),
                    paymentSource.getPaymentDate(),
                    "partnerCorpName",
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