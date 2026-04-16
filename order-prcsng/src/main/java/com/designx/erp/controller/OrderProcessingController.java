package com.designx.erp.controller;

import com.designx.erp.components.*;
import com.designx.erp.external.InventoryModuleAdapter;
import com.designx.erp.external.db.OrderDataFetcher;
import com.designx.erp.external.db.OrderMapper;
import com.designx.erp.model.*;
import com.designx.erp.exception.*;

/**
 * MEMBER 3 - Controller & Workflow Orchestration
 * =============================================
 * 
 * GRASP (Controller): Central facade that receives system events and delegates
 *                     to the correct specialist component.
 *                     Owns NO business logic — only orchestrates the pipeline.
 *
 * Responsibilities:
 *   1. Orchestrate complete order processing workflow
 *   2. Manage order lifecycle using State Pattern (via Order.updateStatus)
 *   3. Support order creation from database quotes (via OrderMapper)
 *   4. Support order modification and cancellation
 *   5. Maintain order repository
 *
 * SOLID (SRP): Coordinates the flow only; all actual work is in the components.
 * SOLID (DIP): Receives all collaborators via constructor injection (no new inside).
 *
 * Order Processing Pipeline:
 *   DB/Quote → Mapping → Validation → Approval → Fulfill → Bill → Pay → Analytics
 *
 * Enhanced with:
 *   - Order Lifecycle State Management (with history tracking via State Pattern)
 *   - Database integration via OrderMapper (Factory Pattern)
 *   - Order Modification and Cancellation
 *   - Order Tracking Service
 */
public class OrderProcessingController {

    private final OrderCaptureUI           orderCaptureUI;
    private final ValidationEngine         validationEngine;
    private final ApprovalWorkflow         approvalWorkflow;
    private final FulfillmentOrchestrator  fulfillmentOrchestrator;
    private final BillingGenerator         billingGenerator;
    private final PaymentProcessor         paymentProcessor;
    private final OrderAnalytics           orderAnalytics;
    
    // NEW: Order tracking and repository
    private final OrderRepository          orderRepository;
    private final OrderTrackingService     orderTrackingService;
    private final InventoryModuleAdapter   inventoryAdapter;
    
    // MEMBER 3: Database integration (optional)
    private final OrderMapper              orderMapper;
    @SuppressWarnings("unused")
    private final OrderDataFetcher         orderDataFetcher;

    public OrderProcessingController(
            OrderCaptureUI          orderCaptureUI,
            ValidationEngine        validationEngine,
            ApprovalWorkflow        approvalWorkflow,
            FulfillmentOrchestrator fulfillmentOrchestrator,
            BillingGenerator        billingGenerator,
            PaymentProcessor        paymentProcessor,
            OrderAnalytics          orderAnalytics) {

        this.orderCaptureUI          = orderCaptureUI;
        this.validationEngine        = validationEngine;
        this.approvalWorkflow        = approvalWorkflow;
        this.fulfillmentOrchestrator = fulfillmentOrchestrator;
        this.billingGenerator        = billingGenerator;
        this.paymentProcessor        = paymentProcessor;
        this.orderAnalytics          = orderAnalytics;
        
        // NEW: Initialize repository and tracking service
        this.orderRepository         = new OrderRepository();
        this.orderTrackingService    = new OrderTrackingService(orderRepository);
        this.inventoryAdapter        = null;
        this.orderMapper             = null;
        this.orderDataFetcher        = null;
    }

    /**
     * Alternative constructor that accepts inventory adapter (for modification/cancellation).
     */
    public OrderProcessingController(
            OrderCaptureUI          orderCaptureUI,
            ValidationEngine        validationEngine,
            ApprovalWorkflow        approvalWorkflow,
            FulfillmentOrchestrator fulfillmentOrchestrator,
            BillingGenerator        billingGenerator,
            PaymentProcessor        paymentProcessor,
            OrderAnalytics          orderAnalytics,
            InventoryModuleAdapter  inventoryAdapter) {

        this.orderCaptureUI          = orderCaptureUI;
        this.validationEngine        = validationEngine;
        this.approvalWorkflow        = approvalWorkflow;
        this.fulfillmentOrchestrator = fulfillmentOrchestrator;
        this.billingGenerator        = billingGenerator;
        this.paymentProcessor        = paymentProcessor;
        this.orderAnalytics          = orderAnalytics;
        this.inventoryAdapter        = inventoryAdapter;
        
        // NEW: Initialize repository and tracking service
        this.orderRepository         = new OrderRepository();
        this.orderTrackingService    = new OrderTrackingService(orderRepository);
        this.orderMapper             = null;
        this.orderDataFetcher        = null;
    }

    /**
     * MEMBER 3: Full constructor with database integration components.
     * Allows processing orders directly from database quotes.
     */
    public OrderProcessingController(
            OrderCaptureUI          orderCaptureUI,
            ValidationEngine        validationEngine,
            ApprovalWorkflow        approvalWorkflow,
            FulfillmentOrchestrator fulfillmentOrchestrator,
            BillingGenerator        billingGenerator,
            PaymentProcessor        paymentProcessor,
            OrderAnalytics          orderAnalytics,
            InventoryModuleAdapter  inventoryAdapter,
            OrderMapper             orderMapper,
            OrderDataFetcher        orderDataFetcher) {

        this.orderCaptureUI          = orderCaptureUI;
        this.validationEngine        = validationEngine;
        this.approvalWorkflow        = approvalWorkflow;
        this.fulfillmentOrchestrator = fulfillmentOrchestrator;
        this.billingGenerator        = billingGenerator;
        this.paymentProcessor        = paymentProcessor;
        this.orderAnalytics          = orderAnalytics;
        this.inventoryAdapter        = inventoryAdapter;
        this.orderMapper             = orderMapper;
        this.orderDataFetcher        = orderDataFetcher;
        
        // Initialize repository and tracking service
        this.orderRepository         = new OrderRepository();
        this.orderTrackingService    = new OrderTrackingService(orderRepository);
    }

    /**
     * End-to-end order processing entry point.
     * Enhanced with status tracking and order repository storage.
     *
     * @return the resulting Payment, or null if the order was rejected before payment
     */
    public Payment processOrder(String customerId,
                                String vehicleModel,
                                String vehicleVariant,
                                String vehicleColor,
                                String customFeaturesOrAddOns,
                                String orderDetails,
                                double orderValue,
                                Payment.PaymentMethod paymentMethod,
                                String transactionDetails) {

        System.out.println("\n========== ORDER PROCESSING STARTED ==========");

        // Step 1 — Capture
        Order order = orderCaptureUI.captureOrder(
                customerId, vehicleModel, vehicleVariant,
                vehicleColor, customFeaturesOrAddOns, orderDetails, orderValue);
        // Order already initialized with CAPTURED status in constructor
        orderRepository.save(order);  // NEW: Store in repository
        orderAnalytics.recordOrder(order);

        // Step 2 — Validate
        if (!validationEngine.validate(order)) {
            order.updateStatus(OrderStatus.REJECTED, 
                    "Validation failed: " + order.getRejectionReason());
            orderRepository.save(order);  // NEW: Update in repository
            System.out.println("[Controller] Order REJECTED at validation: " + order.getRejectionReason());
            System.out.println("========== ORDER PROCESSING ENDED (REJECTED) ==========\n");
            return null;
        }
        order.updateStatus(OrderStatus.VALIDATED, "Order passed validation");
        orderRepository.save(order);  // NEW: Update in repository

        // Step 3 — Approve
        if (!approvalWorkflow.process(order)) {
            order.updateStatus(OrderStatus.REJECTED, 
                    "Approval failed: " + order.getRejectionReason());
            orderRepository.save(order);  // NEW: Update in repository
            System.out.println("[Controller] Order REJECTED at approval: " + order.getRejectionReason());
            System.out.println("========== ORDER PROCESSING ENDED (REJECTED) ==========\n");
            return null;
        }
        order.updateStatus(OrderStatus.APPROVED, "Order approved for processing");
        orderRepository.save(order);  // NEW: Update in repository

        // Step 4 — Fulfill
        Shipment shipment = fulfillmentOrchestrator.fulfill(order);
        order.updateStatus(OrderStatus.ALLOCATED, "Inventory allocated");
        orderRepository.save(order);  // NEW: Update in repository
        order.updateStatus(OrderStatus.DISPATCHED, "Order dispatched: " + shipment);
        orderRepository.save(order);  // NEW: Update in repository
        System.out.println("[Controller] Shipment ready: " + shipment);

        // Step 5 — Bill
        Invoice invoice = billingGenerator.generateInvoice(order);
        order.updateStatus(OrderStatus.INVOICED, "Invoice generated: " + invoice.getInvoiceId());
        orderRepository.save(order);  // NEW: Update in repository

        // Step 6 — Pay
        order.updateStatus(OrderStatus.PAYMENT_PENDING, "Payment pending - processing payment");
        orderRepository.save(order);  // NEW: Update in repository
        Payment payment = paymentProcessor.processPayment(
                invoice, order, paymentMethod, transactionDetails);
        if (payment != null && payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
            order.updateStatus(OrderStatus.PAYMENT_SUCCESS, "Payment processed successfully");
        } else {
            order.updateStatus(OrderStatus.FAILED, "Payment processing failed");
        }
        orderRepository.save(order);  // NEW: Update in repository
        orderAnalytics.recordTransaction(payment);

        System.out.println("[Controller] Final Order Status: " + order.getStatus());
        System.out.println("========== ORDER PROCESSING COMPLETE ==========\n");
        return payment;
    }

    /**
     * MEMBER 3: Process order directly from database quote.
     * 
     * Bridge between Sales subsystem (DB) and Order Processing.
     * Uses Factory Pattern (OrderMapper) to convert quote data to Order domain object.
     *
     * @param quoteId the quote ID from database
     * @param paymentMethod payment method for this order
     * @param transactionDetails transaction reference
     * @return the resulting Payment, or null if order was rejected
     * @throws OrderNotFoundException if quote not found in database
     * @throws InvalidCustomerDataException if customer data invalid
     * @throws NegativeOrderValueException if order amount invalid
     */
    public Payment processOrderFromQuote(String quoteId, 
                                         Payment.PaymentMethod paymentMethod,
                                         String transactionDetails)
            throws OrderNotFoundException, InvalidCustomerDataException, NegativeOrderValueException {
        
        if (orderMapper == null) {
            throw new IllegalStateException("OrderMapper not configured in controller");
        }

        System.out.println("\n========== ORDER PROCESSING FROM QUOTE ==========");
        System.out.println("[Controller] Creating order from quote: " + quoteId);

        // MEMBER 1 Integration: Use Factory Pattern to map DB data to Order
        Order order = orderMapper.createOrderFromQuote(quoteId);
        orderRepository.save(order);
        orderAnalytics.recordOrder(order);

        // Continue with normal workflow
        System.out.println("[Controller] Order created: " + order.getOrderId());

        // Validate
        if (!validationEngine.validate(order)) {
            order.updateStatus(OrderStatus.REJECTED, 
                    "Validation failed: " + order.getRejectionReason());
            orderRepository.save(order);
            System.out.println("[Controller] Order REJECTED at validation: " + order.getRejectionReason());
            return null;
        }
        order.updateStatus(OrderStatus.VALIDATED, "Order passed validation");
        orderRepository.save(order);

        // Approve
        if (!approvalWorkflow.process(order)) {
            order.updateStatus(OrderStatus.REJECTED, 
                    "Approval failed: " + order.getRejectionReason());
            orderRepository.save(order);
            System.out.println("[Controller] Order REJECTED at approval");
            return null;
        }
        order.updateStatus(OrderStatus.APPROVED, "Order approved" );
        orderRepository.save(order);

        // Fulfill
        Shipment shipment = fulfillmentOrchestrator.fulfill(order);
        order.updateStatus(OrderStatus.ALLOCATED, "Inventory allocated from quote");
        orderRepository.save(order);
        order.updateStatus(OrderStatus.DISPATCHED, "Order dispatched: " + shipment);
        orderRepository.save(order);

        // Bill
        Invoice invoice = billingGenerator.generateInvoice(order);
        order.updateStatus(OrderStatus.INVOICED, "Invoice generated: " + invoice.getInvoiceId());
        orderRepository.save(order);

        // Pay - First mark as PAYMENT_PENDING before processing
        order.updateStatus(OrderStatus.PAYMENT_PENDING, "Payment pending - processing payment from quote");
        orderRepository.save(order);
        
        Payment payment = paymentProcessor.processPayment(
                invoice, order, paymentMethod, transactionDetails);
        if (payment != null && payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
            order.updateStatus(OrderStatus.PAYMENT_SUCCESS, "Payment processed successfully from quote");
        } else {
            order.updateStatus(OrderStatus.FAILED, "Payment processing failed from quote");
        }
        orderRepository.save(order);
        orderAnalytics.recordTransaction(payment);

        System.out.println("[Controller] Quote-based order completed: " + order.getOrderId());
        System.out.println("========== ORDER FROM QUOTE COMPLETE ==========\n");
        return payment;
    }

    /**
     * Modifies an existing order (allowed only before DISPATCHED state).
     * Re-runs validation and inventory allocation after modification.
     *
     * @param orderId the order to modify
     * @param newVariant new vehicle variant (or null to keep current)
     * @param newColor new vehicle color (or null to keep current)
     * @param newFeatures new custom features (or null to keep current)
     * @param newOrderValue new order value (or 0 to keep current)
     * @return true if modification successful, false otherwise
     */
    public boolean modifyOrder(String orderId, String newVariant, String newColor, 
                                String newFeatures, double newOrderValue) {
        
        var optOrder = orderRepository.findById(orderId);
        if (optOrder.isEmpty()) {
            System.out.println("[Controller] Order not found: " + orderId);
            return false;
        }

        Order order = optOrder.get();

        // Check if modification is allowed (before DISPATCHED)
        if (order.getStatus() == OrderStatus.DISPATCHED || 
            order.getStatus() == OrderStatus.INVOICED ||
            order.getStatus() == OrderStatus.PAYMENT_PENDING ||
            order.getStatus() == OrderStatus.PAYMENT_SUCCESS ||
            order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("[Controller] Cannot modify order in state: " + order.getStatus());
            order.addHistoryEntry(order.getStatus(), "Modification attempted but denied - order too advanced");
            orderRepository.save(order);
            return false;
        }

        // Apply modifications
        order.applyModifications(
                newVariant != null ? newVariant : order.getVehicleVariant(),
                newColor != null ? newColor : order.getVehicleColor(),
                newFeatures != null ? newFeatures : order.getCustomFeaturesOrAddOns(),
                newOrderValue > 0 ? newOrderValue : order.getOrderValue()
        );

        order.addHistoryEntry(order.getStatus(), "Order modified: variant=" + order.getCurrentVariant() 
                + ", color=" + order.getCurrentColor() + ", value=" + order.getCurrentOrderValue());

        // Re-validate
        System.out.println("[Controller] Re-validating modified order...");
        if (!validationEngine.validate(order)) {
            order.updateStatus(OrderStatus.REJECTED, 
                    "Re-validation failed after modification: " + order.getRejectionReason());
            orderRepository.save(order);
            return false;
        }

        order.updateStatus(OrderStatus.VALIDATED, "Modified order passed re-validation");
        orderRepository.save(order);
        System.out.println("[Controller] Order modification successful: " + orderId);
        return true;
    }

    /**
     * Cancels an existing order.
     * If stock was allocated, releases it back to inventory.
     *
     * @param orderId the order to cancel
     * @param reason cancellation reason
     * @return true if cancellation successful, false otherwise
     */
    public boolean cancelOrder(String orderId, String reason) {
        var optOrder = orderRepository.findById(orderId);
        if (optOrder.isEmpty()) {
            System.out.println("[Controller] Order not found: " + orderId);
            return false;
        }

        Order order = optOrder.get();

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            System.out.println("[Controller] Order already cancelled: " + orderId);
            return false;
        }

        if (order.getStatus() == OrderStatus.PAYMENT_SUCCESS) {
            System.out.println("[Controller] Cannot cancel order with completed payment: " + orderId);
            return false;
        }

        // If stock was allocated, release it
        if (order.getStatus() == OrderStatus.ALLOCATED || 
            order.getStatus() == OrderStatus.DISPATCHED) {
            if (inventoryAdapter != null) {
                inventoryAdapter.releaseStock(orderId);
            }
        }

        // Update status to cancelled
        order.updateStatus(OrderStatus.CANCELLED, "Order cancelled: " + reason);
        orderRepository.save(order);
        
        System.out.println("[Controller] Order cancelled successfully: " + orderId + " - Reason: " + reason);
        return true;
    }

    /**
     * Retrieves the tracking service for querying orders.
     */
    public OrderTrackingService getTrackingService() {
        return orderTrackingService;
    }

    /** Returns the analytics performance report. */
    public String getAnalyticsReport() {
        return orderAnalytics.generatePerformanceReport();
    }
}
