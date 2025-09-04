package com.example.springbatch.common;

import com.example.springbatch.common.entity.PaymentDailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDailyStatisticsRepository extends JpaRepository<PaymentDailyStatistics, Long> {
}
