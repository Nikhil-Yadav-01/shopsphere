package com.rudraksha.shopsphere.review.exception;

public class UnauthorizedReviewException extends RuntimeException {
    public UnauthorizedReviewException(String message) {
        super(message);
    }

    public UnauthorizedReviewException(String message, Throwable cause) {
        super(message, cause);
    }
}
