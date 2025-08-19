package com.example.springbatch.part2;

import com.example.springbatch.common.OrderClient;
import com.example.springbatch.common.UserRepository;
import com.example.springbatch.common.entity.User;
import com.example.springbatch.common.listener.ChunkDurationTrackerListener;
import com.example.springbatch.common.listener.StepDurationTrackerListener;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.stream.Collectors;

/*
    - 실행방법
      - Program arguments에 아래 옵션 추가 후 Run
      - --job.name=userGradleApplyJob

    - 강의정리
       - 회원 수마다 UPDATE 쿼리가 나가면, Chunk Size만큼 DB I/O가 발생한다.
       - 이는 성능적인 부분도 이슈이지만, DB에 좋지 못한 영향을 줄 수 있다.
       - 때문에 등급별로 그룹지어 한 번의 UPDATE를 수행하게 성능개선을 할 수 있다.
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class Ex13_UserGradleApplyJobConfiguration {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderClient orderClient;
    private final UserRepository userRepository;
    private final int chunkSize = 1_000;

//    @Bean
//    public Job userGradleApplyJob(
//            Step userGradleApplyStep
//    ) {
//        return new JobBuilder("userGradleApplyJob", jobRepository)
//                .incrementer(new RunIdIncrementer())
//                .start(userGradleApplyStep)
//                .build();
//    }
//
//    @Bean
//    public Step userGradleApplyStep(
//            JpaCursorItemReader<User> cursorItemReader,
//            ItemWriter<User> writer
//    ) {
//        return new StepBuilder("userGradleApplyStep", jobRepository)
//                .<User, User>chunk(chunkSize, transactionManager)
//                .listener(new StepDurationTrackerListener())
//                .reader(cursorItemReader)
//                .writer(writer)
//                .listener(new ChunkDurationTrackerListener())
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public JpaCursorItemReader<User> cursorItemReader() {
//        return new JpaCursorItemReaderBuilder<User>()
//                .name("cursorItemReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT u FROM User u WHERE u.grade = 'INIT'")
//                .build();
//    }
//
//    @Bean
//    public ItemWriter<User> writer() {
//        return users -> {
//            var appliedGradeUsers = Flowable.fromIterable(users.getItems())
//                    .parallel()
//                    .runOn(Schedulers.io())
//                    .map(user -> {
//                        final var grade = orderClient.getGrade(user.getId());
//                        user.setGrade(grade);
//                        return user;
//                    })
//                    .sequential()
//                    .toList()
//                    .blockingGet();
//
//            appliedGradeUsers.stream()
//                    .collect(Collectors.groupingBy(User::getGrade))
//                    .forEach((grade, targetUsers) -> {
//                                final var userIds = targetUsers.stream().map(User::getId).toList();
//                                userRepository.updateGrade(grade, userIds);
//                            }
//                    );
//        };
//    }

}