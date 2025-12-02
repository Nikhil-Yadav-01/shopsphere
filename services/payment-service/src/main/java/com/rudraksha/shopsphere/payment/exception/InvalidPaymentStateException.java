package com.rudraksha.shopsphere.payment.exception;

public class InvalidPaymentStateException extends RuntimeException {

    private final String currentStatus;
    private final String requestedOperation;

    public InvalidPaymentStateException(String message, String currentStatus, String requestedOperation) {
        super(message);
        this.currentStatus = currentStatus;
        this.requestedOperation = requestedOperation;
    }

    public InvalidPaymentStateException(String message, Throwable cause) {
        super(message, cause);
        this.currentStatus = null;
        this.requestedOperation = null;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getRequestedOperation() {
        return requestedOperation;
    }
}
