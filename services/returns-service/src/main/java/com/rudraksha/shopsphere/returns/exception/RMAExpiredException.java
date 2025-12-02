package com.rudraksha.shopsphere.returns.exception;

public class RMAExpiredException extends RuntimeException {
    public RMAExpiredException(String message) {
        super(message);
    }

    public RMAExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
