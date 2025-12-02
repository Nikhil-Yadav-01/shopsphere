package com.rudraksha.shopsphere.coupon.exception;

public class CouponExpiredException extends RuntimeException {
    public CouponExpiredException(String code) {
        super(String.format("Coupon has expired: %s", code));
    }

    public CouponExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
