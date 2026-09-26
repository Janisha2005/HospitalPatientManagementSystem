package com.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentExceedsOutstandingException extends RuntimeException {
    public PaymentExceedsOutstandingException(String message) {
        super(message);
    }
}
