package com.rudraksha.shopsphere.coupon.exception;

public class InvalidCouponException extends RuntimeException {
    public InvalidCouponException(String message) {
        super(message);
    }

    public InvalidCouponException(String message, Throwable cause) {
        super(message, cause);
    }
}
