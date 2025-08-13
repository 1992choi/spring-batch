package com.example.springbatch.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 결제 금액
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * 결제일
     */
    @Column(nullable = false)
    private LocalDate paymentDate;

    /**
     * 결제 파트너의 상호명
     */
    @Column(nullable = true, length = 100)
    private String partnerCorpName;

    /**
     * 결제 상태, 취소, 부분 취소
     */
    @Column(nullable = false, length = 50)
    private String status;
}
