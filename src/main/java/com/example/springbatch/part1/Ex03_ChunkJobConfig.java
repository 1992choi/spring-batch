package com.example.springbatch.part1;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.Long;
import java.util.List;
import java.util.stream.LongStream;

/*
    - 실행방법
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=chunkJob

    - 강의정리
       - Step 구성방식은 크게 TaskletStep과 ChunkOrientedTasklet으로 나뉜다.
       - ChunkOrientedTasklet은 주로 반복처리에 사용된다.
 */
@Configuration
public class Ex03_ChunkJobConfig {

    @Bean
    public Job chunkJob(
            JobRepository jobRepository,
            Step chunkStep
    ) {
        return new JobBuilder("chunkJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkStep)
                .build();
    }

    @Bean
    public Step chunkStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("chunkStep", jobRepository)
                .<Long, Long>chunk(10, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    private ItemReader<Long> itemReader() {
        return new ListItemReader<>(getItems());
    }

    private ItemProcessor<Long, Long> itemProcessor() {
        return item -> {
            return item;
        };
    }


    private ItemWriter<Long> itemWriter(
    ) {
        return items -> {
            items.forEach(item -> {
                System.out.println("Payment: " + item);
            });
        };
    }

    private List<Long> getItems() {
        return LongStream.rangeClosed(1, 100)
                .boxed()
                .toList();
    }

}