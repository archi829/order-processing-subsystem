package com.designx.erp.exception;

/**
 * Thrown when an order is not found in the system.
 * 
 * GRASP Principle: Separation of Concerns
 * Purpose: Explicit error handling for missing orders
 */
public class OrderNotFoundException extends Exception {
    
    public OrderNotFoundException(String orderId) {
        super("Order not found: " + orderId);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
