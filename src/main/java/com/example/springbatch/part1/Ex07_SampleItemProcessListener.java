package com.example.springbatch.part1;

import com.example.springbatch.common.Payment;
import com.example.springbatch.common.PaymentSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;

@Slf4j
public class Ex07_SampleItemProcessListener implements ItemProcessListener<PaymentSource, Payment> {

    @Override
    public void beforeProcess(PaymentSource item) {
        log.info("sample - 3 SampleItemProcessListener beforeProcess");
    }

    @Override
    public void afterProcess(PaymentSource item, Payment result) {
        log.info("sample - 3 SampleItemProcessListener afterProcess");
    }

    @Override
    public void onProcessError(PaymentSource item, Exception e) {
        log.info("sample - 3 SampleItemProcessListener onProcessError");
    }

}
