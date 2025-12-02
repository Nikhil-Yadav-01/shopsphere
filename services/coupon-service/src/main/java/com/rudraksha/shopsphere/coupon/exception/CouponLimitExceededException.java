package com.rudraksha.shopsphere.coupon.exception;

public class CouponLimitExceededException extends RuntimeException {
    public CouponLimitExceededException(String code) {
        super(String.format("Coupon usage limit exceeded: %s", code));
    }

    public CouponLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
