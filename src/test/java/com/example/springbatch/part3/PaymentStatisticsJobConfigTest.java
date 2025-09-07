package com.example.springbatch.part3;

import com.example.springbatch.common.entity.PaymentDailyStatistics;
import com.example.springbatch.common.entity.PaymentSourceV2;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.springbatch.common.entity.QPaymentDailyStatistics.paymentDailyStatistics;
import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

/*
    테스트 코드가 늘어날수록 반복 작성이 될 여지가 높은 코드들은 별도의 파일로 분리하여 재사용성을 높인다.
    - SpringBatchTestSupport로 분리
 */
@TestPropertySource(properties = {"args.payment-date=2025-01-05"})
class PaymentStatisticsJobConfigTest extends SpringBatchTestSupport {

    @Test
    void paymentStatisticsJob_test() throws Exception {
        // given
        saveAll(
                List.of(
                        new PaymentSourceV2("사업자1", "10002000", new BigDecimal("100"), LocalDateTime.of(2025, 1, 5, 0, 1, 2)),
                        new PaymentSourceV2("사업자1", "10002000", new BigDecimal("300"), LocalDateTime.of(2025, 1, 5, 23, 1, 2)),
                        new PaymentSourceV2("사업자2", "2002231", new BigDecimal("1000"), LocalDateTime.of(2025, 1, 5, 0, 1, 2))
                )
        );

        // when
        launchJob(paymentStatisticsJob);

        // then
        thenBatchCompleted();
        final List<PaymentDailyStatistics> dailyStatistics = query.selectFrom(paymentDailyStatistics)
                .where(paymentDailyStatistics.paymentDate.eq(properties.getPaymentDate()))
                .fetch();

        then(dailyStatistics).hasSize(2);
        then(dailyStatistics).allSatisfy(dailySum -> {
            switch (dailySum.getBusinessRegistrationNumber()) {
                case "10002000" -> {
                    then(dailySum.getCorpName()).isEqualTo("사업자1");
                    then(dailySum.getAmount()).isEqualByComparingTo(new BigDecimal("400"));
                }
                case "2002231" -> {
                    then(dailySum.getCorpName()).isEqualTo("사업자2");
                    then(dailySum.getAmount()).isEqualByComparingTo(new BigDecimal("1000"));
                }
                default -> fail("예상치 못한 사업자 번호입니다: " + dailySum.getBusinessRegistrationNumber());
            }
        });
    }

}