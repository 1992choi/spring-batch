package com.example.springbatch.common;

import java.time.LocalDate;
import java.util.Set;

public interface PaymentSourceRepositoryCustom {

    Set<LocalDate> findPaymentDatesByTodayUpdates();

}
