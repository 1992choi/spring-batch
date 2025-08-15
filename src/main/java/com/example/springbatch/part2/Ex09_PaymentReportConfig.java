package com.example.springbatch.part2;

import com.example.springbatch.common.entity.Payment;
import com.example.springbatch.common.entity.PaymentSource;
import com.example.springbatch.common.listener.ChunkDurationTrackerListener;
import com.example.springbatch.common.listener.StepDurationTrackerListener;
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
      - --job.name=paymentReportJobEx paymentDate=2025-05-02

    - 강의정리
       - no offset 방식은 limit offset 방식의 한계를 보완할 수 있는 방식이다.
       - limit offset 방식과 다르게 뒤로 갈수록 느려지지 않는다. (페이지에 관계없이 성능이 균등함.)
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex09_PaymentReportConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final int chunkSize = 1_000;

//    @Bean
//    public Job paymentReportJob(
//            Step paymentReportStep
//    ) {
//        return new JobBuilder("paymentReportJob", jobRepository)
//                .incrementer(new RunIdIncrementer())
//                .start(paymentReportStep)
//                .build();
//    }
//
//    @Bean
//    public Step paymentReportStep(
//            Ex09_NoOffsetItemReader<PaymentSource> noOffsetItemReader
//    ) {
//        return new StepBuilder("paymentReportStep", jobRepository)
//                .<PaymentSource, Payment>chunk(chunkSize, transactionManager)
//                .listener(new StepDurationTrackerListener())
//                .reader(noOffsetItemReader)
//                .processor(paymentReportProcessor())
//                .writer(paymentReportWriter())
//                .listener(new ChunkDurationTrackerListener())
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public JpaPagingItemReader<PaymentSource> limitOffsetItemReader(
//            @Value("#{jobParameters['paymentDate']}") LocalDate paymentDate
//    ) {
//        return new JpaPagingItemReaderBuilder<PaymentSource>()
//                .name("paymentSourceItemReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT ps FROM PaymentSource ps WHERE ps.paymentDate = :paymentDate")
//                .parameterValues(Collections.singletonMap("paymentDate", paymentDate))
//                .pageSize(chunkSize)
//                .build();
//    }
//
//    /**
//     * No Offset 기반의 ItemReader를 생성합니다.
//     * 이 리더는 대용량 데이터 처리에 대한 성능 저하를 방지하기 위해 페이지 오프셋을 사용하지 않습니다.
//     * 대신, 마지막으로 읽은 항목의 ID를 사용하여 다음 페이지를 조회합니다 (seek-method).
//     * 'idExtractor'를 통해 각 항목의 고유 ID를 추출하고, 이를 정렬 및 다음 페이지 조회 조건에 사용합니다.
//     *
//     * @param paymentDate JobParameter로 전달받은 조회할 결제 날짜
//     * @return NoOffsetItemReader 인스턴스
//     */
//    @Bean
//    @StepScope
//    public Ex09_NoOffsetItemReader<PaymentSource> noOffsetItemReader(
//            @Value("#{jobParameters['paymentDate']}") LocalDate paymentDate
//    ) {
//        return new Ex09_NoOffsetItemReaderBuilder<PaymentSource>()
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT ps FROM PaymentSource ps WHERE ps.paymentDate = :paymentDate ORDER BY ps.id DESC")
//                .parameterValues(Collections.singletonMap("paymentDate", paymentDate))
//                .chunkSize(chunkSize)
//                .name("noOffsetItemReader")
//                .idExtractor(PaymentSource::getId)
//                .targetType(PaymentSource.class)
//                .build();
//    }
//
//
//    private ItemProcessor<PaymentSource, Payment> paymentReportProcessor() {
//        return paymentSource -> {
//            final Payment payment = new Payment(
//                    null,
//                    paymentSource.getFinalAmount(),
//                    paymentSource.getPaymentDate(),
//                    "partnerCorpName",
//                    "PAYMENT"
//            );
//            return payment;
//        };
//    }
//
//    @Bean
//    public ItemWriter<Payment> paymentReportWriter() {
//        return chunk -> {
//
//        };
//    }

}