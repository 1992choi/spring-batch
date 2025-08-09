package com.example.springbatch.part1;

public class InvalidPaymentAmountException extends RuntimeException {

    public InvalidPaymentAmountException(String msg) {
        super(msg);
    }
}
