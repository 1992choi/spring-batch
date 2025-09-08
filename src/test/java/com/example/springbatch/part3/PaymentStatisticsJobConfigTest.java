package com.example.springbatch.part3;

import com.example.springbatch.common.entity.PaymentDailyStatistics;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.math.BigDecimal;
import java.util.List;

import static com.example.springbatch.common.entity.QPaymentDailyStatistics.paymentDailyStatistics;
import static org.assertj.core.api.BDDAssertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

/*
    강의내용
    - SpringBatchTestSupport을 통한 효율화
      - 테스트 코드가 늘어날수록 반복 작성이 될 여지가 높은 코드들은 별도의 파일로 분리하여 재사용성을 높인다.
      - SpringBatchTestSupport로 분리

    - 복잡하고 다양한 데이터 셋업의 어려움
      - 테스트를 위해서 다양한 데이터 셋업을 given 영역에서 할 경우, 테스트의 목적이 명확하게 드러나지 않는다. (데이터 셋업은 주요 관심사가 아님)
      - 이를 위해 @Sql 어노테이션을 사용하면 Given 절을 극적으로 간소화할 수 있다.
      - 하지만 단점도 존재한다.
        - 변경에 취약하고 디버깅이 어려움
          - 자바 코드 기반으로 데이터를 만들면 컬럼, 엔티티 변경 등에 대해서 컴파일 시점에 오류를 발견할 수 있으나 현재의 방식에서는 감지할 수 없게된다.
        - 가독성이 낮고 의미 전달이 어려움
          - 객체 기반으로 데이터를 만들 경우 변수명, 함수명 등으로 의미와 의도를 명확하게 전달할 수 있으나 현재의 방식에서는 한계가 있다.
        - 동적 데이터 생성
 */
@TestPropertySource(properties = {"args.payment-date=2025-01-05"})
class PaymentStatisticsJobConfigTest extends SpringBatchTestSupport {

    @Test
    @SqlGroup({
            @Sql(value = {"/sql/payment-source-cleanup.sql", "/sql/payment-source-setup.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(value = "/sql/payment-source-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    void paymentStatisticsJob_test() throws Exception {
        // given

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