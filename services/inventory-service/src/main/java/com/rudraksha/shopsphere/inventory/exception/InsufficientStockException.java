package com.rudraksha.shopsphere.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    
    private final int availableQuantity;
    private final int requestedQuantity;
    
    public InsufficientStockException(String message, int availableQuantity, int requestedQuantity) {
        super(message);
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
        this.availableQuantity = 0;
        this.requestedQuantity = 0;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
