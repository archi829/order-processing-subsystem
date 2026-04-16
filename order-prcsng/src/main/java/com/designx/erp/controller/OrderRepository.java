package com.designx.erp.controller;

import com.designx.erp.model.Order;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory repository for storing and retrieving orders.
 * GRASP (Repository): Centralizes all order persistence logic (in-memory for now).
 * SOLID (SRP): Only responsible for storage and retrieval operations.
 * 
 * Thread-safety: Uses synchronized methods for multi-threaded environments.
 */
public class OrderRepository {

    private final Map<String, Order> orders = new HashMap<>();

    /**
     * Stores an order in the repository.
     * @param order the order to store
     */
    public synchronized void save(Order order) {
        orders.put(order.getOrderId(), order);
    }

    /**
     * Retrieves an order by ID.
     * @param orderId the order ID
     * @return Optional containing the order if found, empty otherwise
     */
    public synchronized Optional<Order> findById(String orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    /**
     * Checks if an order exists.
     * @param orderId the order ID
     * @return true if order exists, false otherwise
     */
    public synchronized boolean exists(String orderId) {
        return orders.containsKey(orderId);
    }

    /**
     * Returns the total number of stored orders.
     */
    public synchronized int count() {
        return orders.size();
    }

    /**
     * Clears all orders from the repository (useful for testing).
     */
    public synchronized void clear() {
        orders.clear();
    }

    /**
     * Returns a copy of all stored orders.
     */
    public synchronized Map<String, Order> findAll() {
        return new HashMap<>(orders);
    }
}
