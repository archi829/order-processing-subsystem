package com.designx.erp.controller;

import com.designx.erp.model.Order;
import com.designx.erp.model.OrderHistory;
import com.designx.erp.model.OrderStatus;
import com.designx.erp.exception.OrderNotFoundException;

import java.util.*;

/**
 * MEMBER 4 - Tracking & Integration Support
 * ==========================================
 * 
 * Responsibility: Provide read-only query access to orders and tracking information
 * 
 * GRASP Principle: Information Expert - knows how to query order data
 * SOLID Principle: I - Interface Segregation (read-only interface)
 * 
 * Acts as facade for:
 *   - Order status queries
 *   - Order detail retrieval
 *   - History tracking
 *   - Order filtering by status
 *   - Reporting and analytics
 * 
 * No modification operations - maintains single responsibility (read-only).
 */
public class OrderTrackingService {

    private final OrderRepository orderRepository;

    public OrderTrackingService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Retrieves the current status of an order.
     * 
     * @param orderId the order ID
     * @return the current OrderStatus, or null if order not found
     * @throws OrderNotFoundException if order does not exist
     */
    public OrderStatus getOrderStatus(String orderId) throws OrderNotFoundException {
        return orderRepository.findById(orderId)
                .map(Order::getStatus)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Retrieves complete details of an order.
     * 
     * @param orderId the order ID
     * @return Optional containing the order if found
     */
    public Optional<Order> getOrderDetails(String orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Retrieves the complete history of status changes for an order.
     * Maintains complete audit trail of order lifecycle.
     * 
     * @param orderId the order ID
     * @return List of OrderHistory entries, empty if order not found
     */
    public List<OrderHistory> getOrderHistory(String orderId) {
        return orderRepository.findById(orderId)
                .map(Order::getHistory)
                .orElse(new ArrayList<>());
    }

    /**
     * Gets a formatted string representation of order history.
     * @param orderId the order ID
     * @return formatted history string
     */
    public String getOrderHistoryAsString(String orderId) {
        List<OrderHistory> history = getOrderHistory(orderId);
        if (history.isEmpty()) {
            return "No history found for order: " + orderId;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Order History for ").append(orderId).append(":\n");
        for (OrderHistory entry : history) {
            sb.append("  ").append(entry.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Checks if an order exists in the repository.
     * @param orderId the order ID
     * @return true if order exists
     */
    public boolean orderExists(String orderId) {
        return orderRepository.exists(orderId);
    }

    /**
     * Gets summary information about all tracked orders.
     * @return summary string with counts by status
     */
    public String getOrdersSummary() {
        Map<String, Order> allOrders = orderRepository.findAll();
        Map<OrderStatus, Integer> statusCounts = new HashMap<>();

        for (Order order : allOrders.values()) {
            statusCounts.merge(order.getStatus(), 1, Integer::sum);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Order Tracking Summary:\n");
        sb.append("Total Orders: ").append(allOrders.size()).append("\n");
        for (OrderStatus status : OrderStatus.values()) {
            int count = statusCounts.getOrDefault(status, 0);
            sb.append("  ").append(status).append(": ").append(count).append("\n");
        }
        return sb.toString();
    }

    /**
     * Retrieves all orders matching a specific status.
     * @param status the status to filter by
     * @return list of orders with the specified status
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        Map<String, Order> allOrders = orderRepository.findAll();
        List<Order> result = new ArrayList<>();
        for (Order order : allOrders.values()) {
            if (order.getStatus() == status) {
                result.add(order);
            }
        }
        return result;
    }
}
