package com.example.springbatch.common;

import com.example.springbatch.common.entity.PaymentSourceV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentSourceRepository extends JpaRepository<PaymentSourceV2, Long>, PaymentSourceRepositoryCustom {
}