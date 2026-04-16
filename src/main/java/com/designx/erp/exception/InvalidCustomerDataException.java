package com.designx.erp.exception;

/**
 * Thrown when customer data is invalid or incomplete.
 * 
 * GRASP Principle: Separation of Concerns
 * Purpose: Explicit error handling for invalid customer information
 */
public class InvalidCustomerDataException extends Exception {
    
    public InvalidCustomerDataException(String message) {
        super("Invalid customer data: " + message);
    }

    public InvalidCustomerDataException(String message, Throwable cause) {
        super("Invalid customer data: " + message, cause);
    }
}
