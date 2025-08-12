package com.example.springbatch.part1;

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
