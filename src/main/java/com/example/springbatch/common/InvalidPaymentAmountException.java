package com.example.springbatch.common;

public class InvalidPaymentAmountException extends RuntimeException {

    public InvalidPaymentAmountException(String msg) {
        super(msg);
    }
}
