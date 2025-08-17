package com.example.springbatch.part2;

import com.example.springbatch.common.OrderClient;
import com.example.springbatch.common.entity.Grade;
import com.example.springbatch.common.entity.User;
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
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;


/*
    - 실행방법
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=userGradleApplyJob

    - 강의정리
       - user의 등급을 갱신하기 위하여, 외부 API를 호출하여 user의 주문 데이터를 기반으로 등급을 계산한다.
       - processor 에서 I/O 처리가 동반되는 경우에 성능 개선이 필요하다는 것을 확인하는 예제.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex11_UserGradleApplyJobConfiguration {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderClient orderClient;
    private final int chunkSize = 1_000;

    @Bean
    public Job userGradleApplyJob(
            Step userGradleApplyStep
    ) {
        return new JobBuilder("userGradleApplyJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(userGradleApplyStep)
                .build();
    }

    @Bean
    public Step userGradleApplyStep(
            JpaCursorItemReader<User> cursorItemReader,
            ItemProcessor<User, User> processor,
            JpaItemWriter<User> writer
    ) {
        return new StepBuilder("userGradleApplyStep", jobRepository)
                .<User, User>chunk(chunkSize, transactionManager)
                .listener(new StepDurationTrackerListener())
                .reader(cursorItemReader)
                .processor(processor)
                .writer(writer)
                .listener(new ChunkDurationTrackerListener())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<User> cursorItemReader() {
        return new JpaCursorItemReaderBuilder<User>()
                .name("cursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u WHERE u.grade = 'INIT'")
                .build();
    }

    @Bean
    public ItemProcessor<User, User> processor() {
        return user -> {
            final Grade grade = orderClient.getGrade(user.getId());
            user.setGrade(grade);
            return user;
        };
    }

    @Bean
    public JpaItemWriter<User> writer() {
        return new JpaItemWriterBuilder<User>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

}