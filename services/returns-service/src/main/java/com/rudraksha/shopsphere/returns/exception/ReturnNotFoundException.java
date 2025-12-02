package com.rudraksha.shopsphere.returns.exception;

public class ReturnNotFoundException extends RuntimeException {
    public ReturnNotFoundException(String message) {
        super(message);
    }

    public ReturnNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
