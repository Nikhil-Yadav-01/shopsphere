package com.rudraksha.shopsphere.returns.exception;

public class InvalidReturnStateException extends RuntimeException {
    public InvalidReturnStateException(String message) {
        super(message);
    }

    public InvalidReturnStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
