package com.designx.erp.model;

/**
 * Enum representing the lifecycle states of an order in the Order Processing system.
 * 
 * States flow: CAPTURED → VALIDATED → APPROVED → ALLOCATED → DISPATCHED → INVOICED → 
 *            PAYMENT_PENDING → PAYMENT_SUCCESS
 * 
 * Alternative paths: 
 *   - REJECTED (from VALIDATED or APPROVED)
 *   - CANCELLED (at any point before DISPATCHED)
 *   - PAYMENT_FAILED (from PAYMENT_PENDING, can retry with PAYMENT_PENDING)
 */
public enum OrderStatus {
    CAPTURED,           // Order initially captured from customer
    VALIDATED,          // Order passed validation checks
    APPROVED,           // Order approved for processing
    REJECTED,           // Order rejected (validation or approval failed)
    ALLOCATED,          // Inventory allocated for order
    DISPATCHED,         // Order sent for shipment/fulfillment
    INVOICED,           // Invoice generated for order
    PAYMENT_PENDING,    // Waiting for payment processing
    PAYMENT_SUCCESS,    // Payment processed successfully
    FAILED,             // Order failed during processing
    CANCELLED           // Order cancelled by customer or system
}
