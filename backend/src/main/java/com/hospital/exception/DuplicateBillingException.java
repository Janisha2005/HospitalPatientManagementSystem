package com.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateBillingException extends RuntimeException {
    public DuplicateBillingException(String message) {
        super(message);
    }
}
