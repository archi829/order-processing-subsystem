package com.designx.erp.model;

import java.time.LocalDateTime;

/**
 * Represents a single entry in an order's history log.
 * Tracks status changes with timestamp and descriptive message.
 * 
 * GRASP (Information Expert): Knows all details about a specific point in order lifecycle.
 */
public class OrderHistory {

    private final OrderStatus status;
    private final LocalDateTime timestamp;
    private final String message;

    public OrderHistory(OrderStatus status, String message) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    // Getters
    public OrderStatus getStatus()              { return status; }
    public LocalDateTime getTimestamp()         { return timestamp; }
    public String getMessage()                  { return message; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", timestamp, status, message);
    }
}
