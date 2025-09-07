package com.example.springbatch.part3;

import com.example.springbatch.common.ArgumentProperties;
import com.example.springbatch.common.PaymentDailyStatisticsRepository;
import com.example.springbatch.common.PaymentSourceRepository;
import com.example.springbatch.common.entity.PaymentDailyStatistics;
import com.example.springbatch.common.entity.PaymentSourceV2;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * PaymentStatisticsJob의 통합 테스트 클래스입니다.
 * Spring Batch의 Job을 실제와 유사한 환경에서 테스트합니다.
 */
@SpringBatchTest
@SpringBootTest(properties = {"spring.batch.job.enabled=false"})
@TestPropertySource(properties = {"args.payment-date=2025-01-05"})
@ActiveProfiles("test")
class PaymentStatisticsJobConfigTest {

    @Resource
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job paymentStatisticsJob;

    @Autowired
    private ArgumentProperties properties;

    @Autowired
    private PaymentDailyStatisticsRepository paymentDailyStatisticsRepository;

    @Autowired
    private PaymentSourceRepository paymentSourceRepository;

    @BeforeEach
    void setUp() {
        this.jobLauncherTestUtils.setJob(paymentStatisticsJob);
    }

    @Test
    void paymentStatisticsJob_test() throws Exception {
        // given: 테스트에 필요한 데이터를 설정합니다.
        paymentSourceRepository.saveAll(
                List.of(
                        // '사업자1'에 대한 두 건의 결제 데이터
                        new PaymentSourceV2("사업자1", "10002000", new BigDecimal("100"), LocalDateTime.of(2025, 1, 5, 0, 1, 2)),
                        new PaymentSourceV2("사업자1", "10002000", new BigDecimal("300"), LocalDateTime.of(2025, 1, 5, 23, 1, 2)),
                        // '사업자2'에 대한 한 건의 결제 데이터
                        new PaymentSourceV2("사업자2", "2002231", new BigDecimal("1000"), LocalDateTime.of(2025, 1, 5, 0, 1, 2))
                )
        );

        // when: 설정된 Job을 실행합니다.
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // then: Job 실행 결과와 데이터베이스 상태를 검증합니다.
        // Job의 최종 실행 상태가 'COMPLETED'인지 확인합니다.
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        // Job 실행 후, 데이터베이스에 저장된 일일 결제 통계 데이터를 조회합니다.
        final List<PaymentDailyStatistics> paymentDailyStatistics = paymentDailyStatisticsRepository.findByPaymentDate(properties.getPaymentDate());
        // 사업자별로 통계가 집계되므로, 총 2개의 통계 데이터가 생성되었는지 확인합니다.
        then(paymentDailyStatistics).hasSize(2);
        // 각 통계 데이터가 예상대로 집계되었는지 상세하게 검증합니다.
        then(paymentDailyStatistics).allSatisfy(dailySum -> {
            switch (dailySum.getBusinessRegistrationNumber()) {
                case "10002000" -> { // '사업자1'의 통계 검증
                    then(dailySum.getCorpName()).isEqualTo("사업자1");
                    // 100 + 300 = 400
                    then(dailySum.getAmount()).isEqualByComparingTo(new BigDecimal("400"));
                }
                case "2002231" -> { // '사업자2'의 통계 검증
                    then(dailySum.getCorpName()).isEqualTo("사업자2");
                    then(dailySum.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
                }
                default -> fail("예상치 못한 사업자 번호입니다: " + dailySum.getBusinessRegistrationNumber());
            }
        });
    }

}