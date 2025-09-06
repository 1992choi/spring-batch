package com.example.springbatch.common;

import com.example.springbatch.common.entity.PaymentSourceV2;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.springbatch.common.entity.QPaymentSourceV2.paymentSourceV2;

@Repository
public class PaymentSourceRepositoryImpl extends QuerydslCustomRepositorySupport implements PaymentSourceRepositoryCustom {

    public PaymentSourceRepositoryImpl() {
        super(PaymentSourceV2.class);
    }

    @Override
    public Set<LocalDate> findPaymentDatesByTodayUpdates() {
        return selectFrom(paymentSourceV2)
                .where(paymentSourceV2.updatedAt.between(
                        LocalDate.now().atStartOfDay(),
                        LocalDateTime.of(LocalDate.now(), LocalTime.MAX)
                ))
                .fetch()
                .stream()
                .map(it -> it.getPaymentDateTime().toLocalDate())
                .collect(Collectors.toSet());
    }

}
