package com.hospital.exception;

public class ExpiredBatchException extends RuntimeException {
    public ExpiredBatchException(String message) {
        super(message);
    }
}
