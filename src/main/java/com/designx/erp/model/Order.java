package com.designx.erp.model;

import java.time.LocalDate;
import java.util.*;

/**
 * Represents a customer vehicle order in the car manufacturing ERP system.
 * GRASP: Information Expert — holds all order-related data.
 * Tracks complete lifecycle with status history and audit trail.
 */
public class Order {

    private final String orderId;
    private final String customerId;
    private final String customerName;
    private final String customerContactDetails;

    // Vehicle-specific fields (car manufacturing context)
    private final String vehicleModel;
    private final String vehicleVariant;
    private final String vehicleColor;
    private final String customFeaturesOrAddOns;

    private final LocalDate orderDate;
    private final String orderDetails;
    private final double orderValue;

    private OrderStatus status;
    private String rejectionReason;
    private final List<OrderHistory> history;
    
    // Fields for order modification tracking
    private String modifiedVehicleVariant;
    private String modifiedVehicleColor;
    private String modifiedFeaturesOrAddOns;
    private double modifiedOrderValue;

    // GRASP: Creator — Order is created with all mandatory fields
    public Order(String customerId, String customerName, String customerContactDetails,
                 String vehicleModel, String vehicleVariant, String vehicleColor,
                 String customFeaturesOrAddOns, String orderDetails, double orderValue) {
        this.orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerContactDetails = customerContactDetails;
        this.vehicleModel = vehicleModel;
        this.vehicleVariant = vehicleVariant;
        this.vehicleColor = vehicleColor;
        this.customFeaturesOrAddOns = customFeaturesOrAddOns;
        this.orderDate = LocalDate.now();
        this.orderDetails = orderDetails;
        this.orderValue = orderValue;
        this.status = OrderStatus.CAPTURED;
        this.history = new ArrayList<>();
        this.history.add(new OrderHistory(OrderStatus.CAPTURED, "Order captured: " + vehicleModel + " " + vehicleVariant));
    }

    /**
     * Alternative constructor for creating orders from database data.
     * MEMBER 1 (Database Integration): Used by OrderMapper factory pattern.
     * 
     * @param customerId customer identifier from DB
     * @param customerName customer name from DB
     * @param customerContactDetails email or phone from DB
     * @param quoteReference quote ID or reference from DB
     * @param sourceSystem system/quote status from source
     * @param vehicleModel product model (optional)
     * @param vehicleVariant product variant (optional)
     * @param vehicleColor product color/option (optional)
     * @param customFeaturesOrAddOns additional features
     * @param orderDetails description/notes
     * @param orderValue total amount from DB
     */
    public Order(String customerId, String customerName, String customerContactDetails,
                 String quoteReference, String sourceSystem, String vehicleModel, 
                 String vehicleVariant, String vehicleColor, String customFeaturesOrAddOns, 
                 String orderDetails, double orderValue) {
        this.orderId = "ORD-DBQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerContactDetails = customerContactDetails;
        this.vehicleModel = vehicleModel != null ? vehicleModel : "[Quote-based]";
        this.vehicleVariant = vehicleVariant != null ? vehicleVariant : quoteReference;
        this.vehicleColor = vehicleColor != null ? vehicleColor : sourceSystem;
        this.customFeaturesOrAddOns = customFeaturesOrAddOns;
        this.orderDate = LocalDate.now();
        this.orderDetails = orderDetails + " [Source: " + quoteReference + "]";
        this.orderValue = orderValue;
        this.status = OrderStatus.CAPTURED;
        this.history = new ArrayList<>();
    }

    // Getters
    public String getOrderId()                  { return orderId; }
    public String getCustomerId()               { return customerId; }
    public String getCustomerName()             { return customerName; }
    public String getCustomerContactDetails()   { return customerContactDetails; }
    public String getVehicleModel()             { return vehicleModel; }
    public String getVehicleVariant()           { return vehicleVariant; }
    public String getVehicleColor()             { return vehicleColor; }
    public String getCustomFeaturesOrAddOns()   { return customFeaturesOrAddOns; }
    public LocalDate getOrderDate()             { return orderDate; }
    public String getOrderDetails()             { return orderDetails; }
    public double getOrderValue()               { return orderValue; }
    public OrderStatus getStatus()              { return status; }
    public String getRejectionReason()          { return rejectionReason; }
    public List<OrderHistory> getHistory()      { return new ArrayList<>(history); }

    public void setStatus(OrderStatus status)           { this.status = status; }
    public void setRejectionReason(String reason)       { this.rejectionReason = reason; }
    
    /**
     * Updates order status and records the change in history.
     * @param newStatus the new status
     * Updates order status and records the change in history.
     * MEMBER 2 (Lifecycle Management): Enforces state transition rules via State Pattern.
     * 
     * @param newStatus the new status
     * @param message descriptive message for the history entry
     * @throws IllegalStateException if transition is not allowed
     */
    public void updateStatus(OrderStatus newStatus, String message) {
        // MEMBER 2: State Pattern - validate transition before allowing change
        if (!OrderStateTransition.isTransitionAllowed(this.status, newStatus)) {
            throw new IllegalStateException(
                    "Invalid state transition: " + this.status + " → " + newStatus);
        }
        this.status = newStatus;
        this.history.add(new OrderHistory(newStatus, message));
    }
    
    /**
     * Adds a custom history entry for tracking important events.
     */
    public void addHistoryEntry(OrderStatus status, String message) {
        this.history.add(new OrderHistory(status, message));
    }
    
    /**
     * Returns the current vehicle variant (modified or original).
     */
    public String getCurrentVariant() {
        return modifiedVehicleVariant != null ? modifiedVehicleVariant : vehicleVariant;
    }
    
    /**
     * Returns the current vehicle color (modified or original).
     */
    public String getCurrentColor() {
        return modifiedVehicleColor != null ? modifiedVehicleColor : vehicleColor;
    }
    
    /**
     * Returns the current features/add-ons (modified or original).
     */
    public String getCurrentFeatures() {
        return modifiedFeaturesOrAddOns != null ? modifiedFeaturesOrAddOns : customFeaturesOrAddOns;
    }
    
    /**
     * Returns the current order value (modified or original).
     */
    public double getCurrentOrderValue() {
        return modifiedOrderValue > 0 ? modifiedOrderValue : orderValue;
    }
    
    /**
     * Applies modifications to the order (before DISPATCHED state).
     */
    public void applyModifications(String variant, String color, String features, double value) {
        this.modifiedVehicleVariant = variant;
        this.modifiedVehicleColor = color;
        this.modifiedFeaturesOrAddOns = features;
        this.modifiedOrderValue = value;
    }
    
    /**
     * Clears applied modifications.
     */
    public void clearModifications() {
        this.modifiedVehicleVariant = null;
        this.modifiedVehicleColor = null;
        this.modifiedFeaturesOrAddOns = null;
        this.modifiedOrderValue = 0;
    }

    @Override
    public String toString() {
        return String.format("Order[id=%s, customer=%s, vehicle=%s %s, status=%s]",
                orderId, customerName, vehicleModel, vehicleVariant, status);
    }
}
