package com.example.springbatch.common;


import com.example.springbatch.common.entity.Grade;
import org.springframework.stereotype.Service;

@Service
public class OrderClient {

    // 외부의 주문 API를 호출하여 데이터를 가져오고, 그것을 기반으로 등급을 산출한다고 가정.
    public Grade getGrade(Long userId) {
        try {
            // 150ms 대기, 외부 API 호출하는 것처럼 응답 지연
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (userId % 3 == 0) {
            return Grade.VIP;
        } else if (userId % 2 == 0) {
            return Grade.PREMIUM;
        } else {
            return Grade.BASIC;
        }
    }

}
