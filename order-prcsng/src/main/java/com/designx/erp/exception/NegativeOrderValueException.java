package com.designx.erp.exception;

/**
 * Thrown when order total value is negative or invalid.
 * 
 * GRASP Principle: Separation of Concerns
 * Purpose: Explicit error handling for invalid order amounts
 */
public class NegativeOrderValueException extends Exception {
    
    public NegativeOrderValueException(double value) {
        super("Order value cannot be negative: ₹" + value);
    }

    public NegativeOrderValueException(String message, Throwable cause) {
        super("Invalid order value: " + message, cause);
    }
}
