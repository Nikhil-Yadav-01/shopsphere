package com.rudraksha.shopsphere.shipping.exception;

public class InvalidShipmentStateException extends RuntimeException {
    public InvalidShipmentStateException(String message) {
        super(message);
    }

    public InvalidShipmentStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
