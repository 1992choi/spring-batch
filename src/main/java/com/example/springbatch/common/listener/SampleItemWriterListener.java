package com.example.springbatch.common.listener;

import com.example.springbatch.common.entity.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

@Slf4j
public class SampleItemWriterListener implements ItemWriteListener<Payment> {

    @Override
    public void beforeWrite(Chunk<? extends Payment> items) {
        log.info("sample - 4 SampleItemWriterListener beforeWrite");
    }

    @Override
    public void afterWrite(Chunk<? extends Payment> items) {
        log.info("sample - 4 SampleItemWriterListener afterWrite");
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends Payment> items) {
        log.info("sample - 4 SampleItemWriterListener onWriteError");
    }

}
