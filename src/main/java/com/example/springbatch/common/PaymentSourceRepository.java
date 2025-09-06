package com.example.springbatch.common;

import com.example.springbatch.common.entity.PaymentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSourceRepository extends JpaRepository<PaymentSource, Long>, PaymentSourceRepositoryCustom {
}