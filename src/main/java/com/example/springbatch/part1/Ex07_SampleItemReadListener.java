package com.example.springbatch.part1;

import com.example.springbatch.common.PaymentSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;

@Slf4j
public class Ex07_SampleItemReadListener implements ItemReadListener<PaymentSource> {

    @Override
    public void beforeRead() {
        log.info("sample - 2 SampleItemReadListener beforeRead");
    }

    @Override
    public void afterRead(PaymentSource item) {
        log.info("sample - 2 SampleItemReadListener afterRead");
    }

    @Override
    public void onReadError(Exception ex) {
        log.info("sample - 2 SampleItemReadListener onReadError");
    }

}
