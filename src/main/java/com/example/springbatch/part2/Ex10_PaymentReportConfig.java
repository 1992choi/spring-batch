package com.example.springbatch.part2;

import com.example.springbatch.common.entity.Payment;
import com.example.springbatch.common.entity.PaymentSource;
import com.example.springbatch.common.listener.ChunkDurationTrackerListener;
import com.example.springbatch.common.listener.StepDurationTrackerListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.HibernateHints;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
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
       - cursor 방식은 limit offset 방식의 한계를 보완할 수 있는 방식이다.
       - limit offset 방식과 다르게 뒤로 갈수록 느려지지 않는다. (페이지에 관계없이 성능이 균등함.)
       - cursor 방식에는 client side와 server side가 존재한다.
         - .hintValues(Collections.singletonMap(HibernateHints.HINT_FETCH_SIZE, Integer.MIN_VALUE)) 옵션으로 동작 방식을 제어할 수 있다.
         - client side (위 옵션이 없다면 default로 client side 방식이 채택된다.)
           - 모든 데이터를 조회해서 메모리에 올린 후 사용
           - OOM이 발생할 수 있다.
         - server side
           - DB에 커서를 열고 한 행씩 순차적으로 읽는 방식
           - 스레드 및 커넥션이 장기 점유된다는 단점이 있다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex10_PaymentReportConfig {

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
//            JpaCursorItemReader<PaymentSource> cursorItemReader
//    ) {
//        return new StepBuilder("paymentReportStep", jobRepository)
//                .<PaymentSource, Payment>chunk(chunkSize, transactionManager)
//                .listener(new StepDurationTrackerListener())
//                .reader(cursorItemReader)
//                .processor(paymentReportProcessor())
//                .writer(paymentReportWriter())
//                .listener(new ChunkDurationTrackerListener())
//                .build();
//    }
//
//    /**
//     * JpaCursorItemReader를 사용하여 특정 결제일(`paymentDate`)의 결제 원천 데이터를 조회합니다.
//     * <p>
//     * - `paymentDate`는 JobParameter로 전달받습니다.
//     * - Cursor 기반으로 동작하여 대용량 데이터를 처리할 때 메모리 사용량을 최소화할 수 있습니다.
//     * - `HINT_FETCH_SIZE`를 `Integer.MIN_VALUE`로 설정하여 ResultSet에서 한 번에 한 행씩 가져오도록 DB에 힌트를 줍니다. (스트리밍)
//     *
//     * @param paymentDate JobParameter로 전달되는 결제일
//     * @return JpaCursorItemReader<PaymentSource>
//     */
//    @Bean
//    @StepScope
//    public JpaCursorItemReader<PaymentSource> cursorItemReader(
//            @Value("#{jobParameters['paymentDate']}") LocalDate paymentDate
//    ) {
//        return new JpaCursorItemReaderBuilder<PaymentSource>()
//                .name("cursorItemReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT ps FROM PaymentSource ps WHERE ps.paymentDate = :paymentDate")
//                .parameterValues(Collections.singletonMap("paymentDate", paymentDate))
//                // Hibernate Hint 설정: HINT_FETCH_SIZE
//                // 이 옵션은 데이터베이스에서 데이터를 가져올 때
//                // 서버 측 커서(Server-Side Cursor)를 사용할지 클라이언트 측 커서(Client-Side Cursor)를 사용할지 결정하는 중요한 역할을 합니다.
//                // Integer.MIN_VALUE: 이 값을 설정하면 JDBC 드라이버에게 결과를 스트리밍 방식으로 받아오도록 요청합니다.
//                // 즉, 데이터베이스 서버에 커서를 유지한 채로, 애플리케이션에서는 next()를 호출할 때마다 한 건씩 데이터를 가져옵니다.
//                // 이를 통해 대용량의 데이터셋을 처리하더라도 애플리케이션의 메모리 사용량을 최소화할 수 있어 OOM(OutOfMemory) 에러를 방지합니다.
//                // 만약 이 옵션이 없다면, JPA 구현체(Hibernate)는 기본적으로 조회된 모든 결과를 애플리케이션 메모리로 가져와서(Client-Side Cursor) 처리하려고 시도할 수 있습니다.
////                .hintValues(Collections.singletonMap(HibernateHints.HINT_FETCH_SIZE, Integer.MIN_VALUE))
//                .build();
//    }
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