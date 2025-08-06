package com.example.springbatch.part1;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_source")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String partnerCorpName;

    // 원래 금액
    @Column(nullable = false)
    private BigDecimal originalAmount;

    // 할인 금액
    @Column(nullable = false)
    private BigDecimal discountAmount;

    // 최종 금액
    @Column(nullable = false)
    private BigDecimal finalAmount;

    // 결제 일자
    @Column(nullable = false)
    private LocalDate paymentDate;
}