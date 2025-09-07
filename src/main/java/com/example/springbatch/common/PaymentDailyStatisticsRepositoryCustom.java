package com.example.springbatch.common;

import com.example.springbatch.common.entity.PaymentDailyStatistics;

import java.time.LocalDate;
import java.util.List;

public interface PaymentDailyStatisticsRepositoryCustom {

    long deleteByPaymentDate(LocalDate paymentDate);

    List<PaymentDailyStatistics> findBy(List<PaymentDailyStatisticsUniqueKey> keys);

    List<PaymentDailyStatistics> findByPaymentDate(LocalDate paymentDate);

}
