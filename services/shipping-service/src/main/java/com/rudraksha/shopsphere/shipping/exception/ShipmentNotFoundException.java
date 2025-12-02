package com.rudraksha.shopsphere.shipping.exception;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String message) {
        super(message);
    }

    public ShipmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
