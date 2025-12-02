package com.rudraksha.shopsphere.coupon.exception;

public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String message) {
        super(message);
    }

    public CouponNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouponNotFoundException(String code, Long id) {
        super(String.format("Coupon not found: code=%s, id=%d", code, id));
    }
}
