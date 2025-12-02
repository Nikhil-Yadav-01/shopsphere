package com.rudraksha.shopsphere.returns.exception;

public class ReturnWindowExpiredException extends RuntimeException {
    public ReturnWindowExpiredException(String message) {
        super(message);
    }

    public ReturnWindowExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
