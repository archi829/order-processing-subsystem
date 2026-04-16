package com.designx.erp.external;

import com.erp.integration.endpoints.OrdersEndpoints;
import com.erp.model.dto.OrderDTO;

import com.designx.erp.components.ApprovalWorkflow;
import com.designx.erp.components.BillingGenerator;
import com.designx.erp.components.FulfillmentOrchestrator;
import com.designx.erp.components.OrderAnalytics;
import com.designx.erp.components.OrderCaptureUI;
import com.designx.erp.components.PaymentProcessor;
import com.designx.erp.components.ValidationEngine;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.model.Order;
import com.designx.erp.model.Invoice;
import com.designx.erp.model.Payment;
import com.designx.erp.model.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Concrete implementation of the UI team's OrdersEndpoints interface.
 * This class acts as the true Adapter/Facade, translating UI DTOs into
 * internal DesignX Order models and routing them to the correct orchestrators.
 *
 * Fix notes (aligned with actual codebase):
 *  - ApprovalWorkflow only exposes process(Order) — no separate approve/reject methods
 *  - FulfillmentOrchestrator uses fulfill(Order), not initiateShipment()
 *  - PaymentProcessor.processPayment() requires Invoice + Order + PaymentMethod + transactionDetails
 *  - OrderAnalytics has no individual stat getters; uses generatePerformanceReport()
 *  - OrderRepository API uses findAll(), findById(String), save() — not getAllOrders() / getOrderById(int)
 *  - All subsystem components require constructor-injected dependencies
 */
public class OrdersEndpointsImpl implements OrdersEndpoints {

    // External module adapters (required by internal components)
    private final CrmModuleAdapter       crmAdapter;
    private final InventoryModuleAdapter inventoryAdapter;
    private final FinanceModuleAdapter   financeAdapter;

    // Internal components
    private final OrderCaptureUI          orderCaptureUI;
    private final ValidationEngine        validationEngine;
    private final ApprovalWorkflow        approvalWorkflow;
    private final FulfillmentOrchestrator fulfillmentOrchestrator;
    private final BillingGenerator        billingGenerator;
    private final PaymentProcessor        paymentProcessor;
    private final OrderAnalytics          orderAnalytics;

    // Central controller
    private final OrderProcessingController orderController;

    public OrdersEndpointsImpl() {
        // 1. Initialise external adapters first — they satisfy interface dependencies
        this.crmAdapter       = new CrmModuleAdapter();
        this.inventoryAdapter = new InventoryModuleAdapter();
        this.financeAdapter   = new FinanceModuleAdapter();

        // 2. Initialise internal components with their required dependencies
        this.orderCaptureUI          = new OrderCaptureUI(crmAdapter);
        this.validationEngine        = new ValidationEngine(inventoryAdapter);
        this.approvalWorkflow        = new ApprovalWorkflow();
        this.fulfillmentOrchestrator = new FulfillmentOrchestrator(inventoryAdapter);
        this.billingGenerator        = new BillingGenerator(financeAdapter);
        this.paymentProcessor        = new PaymentProcessor(financeAdapter);
        this.orderAnalytics          = new OrderAnalytics();

        // 3. Wire everything into the central controller
        this.orderController = new OrderProcessingController(
                orderCaptureUI, validationEngine, approvalWorkflow,
                fulfillmentOrchestrator, billingGenerator, paymentProcessor,
                orderAnalytics, inventoryAdapter);
    }

    // =========================================================================
    // OrdersEndpoints interface implementations
    // =========================================================================

    /**
     * Returns all orders as DTOs.
     * Uses OrderRepository.findAll() which returns Map<String, Order>.
     */
    @Override
    public List<OrderDTO> getOrders() throws Exception {
        List<OrderDTO> result = new ArrayList<>();
        for (Order order : getAllTrackedOrders().values()) {
            result.add(mapToDTO(order));
        }
        return result;
    }

    /**
     * Creates a new order end-to-end through the controller pipeline.
     * Maps the UI DTO fields to the processOrder() signature.
     */
    @Override
    public boolean createOrder(OrderDTO uiOrder) throws Exception {
        if (uiOrder == null) {
            throw new IllegalArgumentException("Order payload cannot be null");
        }

        String customerId = resolveCustomerId(uiOrder);
        String carModel = nonBlankOrDefault(uiOrder.getCarModel(), "Unknown Model");
        String chassisType = nonBlankOrDefault(uiOrder.getChassisType(), "Standard");
        String notes = nonBlankOrDefault(uiOrder.getNotes(), "");
        String vin = nonBlankOrDefault(uiOrder.getCarVIN(), "N/A");

        double orderValue = uiOrder.getAmount() == null ? 0.0 : uiOrder.getAmount().doubleValue();
        if (orderValue <= 0.0) {
            throw new IllegalArgumentException("Order amount must be greater than zero");
        }

        String orderDetails = "VIN: " + vin + (notes.isBlank() ? "" : " | Notes: " + notes);

        Payment payment = orderController.processOrder(
                customerId,
                carModel,
                chassisType,
                "Factory-Default",
                notes,
                orderDetails,
                orderValue,
                Payment.PaymentMethod.BANK_TRANSFER,
                "UI-ORDER-" + System.currentTimeMillis()
        );
        return payment != null && payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS;
    }

    /**
     * Approves an order.
     * ApprovalWorkflow only exposes process(Order) — it internally decides approve/reject.
     * Here we run the approval step directly for an already-captured/validated order.
     */
    @Override
    public boolean approveOrder(String orderId) throws Exception {
        Optional<Order> optOrder = orderController.getTrackingService().getOrderDetails(orderId);
        if (optOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        Order order = optOrder.get();

        if (order.getStatus() == OrderStatus.APPROVED) {
            return true;
        }
        if (order.getStatus() != OrderStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Only VALIDATED orders can be approved. Current status: " + order.getStatus());
        }

        boolean approved = approvalWorkflow.process(order);
        if (approved) {
            order.updateStatus(OrderStatus.APPROVED, "Manually approved via UI");
        } else {
            order.updateStatus(OrderStatus.REJECTED,
                    "Approval rejected: " + order.getRejectionReason());
        }
        return approved;
    }

    /**
     * Rejects an order with a reason.
     * ApprovalWorkflow has no separate rejectOrder() method; rejection is handled by
     * setting the rejection reason on the Order and updating the status directly.
     */
    @Override
    public boolean rejectOrder(String orderId, String rejectionReason) throws Exception {
        Optional<Order> optOrder = orderController.getTrackingService().getOrderDetails(orderId);
        if (optOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        Order order = optOrder.get();

        if (order.getStatus() == OrderStatus.REJECTED) {
            return true;
        }
        if (order.getStatus() != OrderStatus.VALIDATED && order.getStatus() != OrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only VALIDATED/APPROVED orders can be rejected. Current status: " + order.getStatus());
        }

        order.setRejectionReason(rejectionReason);
        order.updateStatus(OrderStatus.REJECTED, "Rejected via UI: " + rejectionReason);
        return true;
    }

    /**
     * Revises an order by delegating to OrderProcessingController.modifyOrder().
     * modifyOrder() handles state checks and re-validation internally.
     */
    @Override
    public boolean reviseOrder(String orderId, OrderDTO updatedOrder) throws Exception {
        if (updatedOrder == null) {
            throw new IllegalArgumentException("Updated order payload cannot be null");
        }

        String newVariant = blankToNull(updatedOrder.getChassisType());
        String newFeatures = blankToNull(updatedOrder.getNotes());
        double newValue = updatedOrder.getAmount() == null ? 0.0 : updatedOrder.getAmount().doubleValue();

        return orderController.modifyOrder(
                orderId,
                newVariant,
                null,
                newFeatures,
                newValue
        );
    }

    /**
     * Ships an order by running the fulfillment step directly.
     * FulfillmentOrchestrator.fulfill() requires the order to be in APPROVED status.
     */
    @Override
    public boolean shipOrder(String orderId) throws Exception {
        Optional<Order> optOrder = orderController.getTrackingService().getOrderDetails(orderId);
        if (optOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        Order order = optOrder.get();

        if (order.getStatus() == OrderStatus.DISPATCHED) {
            return true;
        }
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "Only APPROVED orders can be shipped. Current status: " + order.getStatus());
        }

        fulfillmentOrchestrator.fulfill(order);
        order.updateStatus(OrderStatus.ALLOCATED, "Inventory allocated via UI shipOrder");
        order.updateStatus(OrderStatus.DISPATCHED, "Order shipped via UI");
        return true;
    }

    /**
     * Processes payment for an order.
     *
     * PaymentProcessor.processPayment() requires an Invoice, not just an order ID + amount.
     * We generate the invoice first via BillingGenerator, then process payment.
     *
     * @param orderId the order to pay
     * @param amount  kept for API compatibility; actual amount comes from the Order itself
     */
    @Override
    public boolean payOrder(String orderId, double amount) throws Exception {
        Optional<Order> optOrder = orderController.getTrackingService().getOrderDetails(orderId);
        if (optOrder.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        Order order = optOrder.get();

        if (amount > 0 && Math.abs(amount - order.getOrderValue()) > 0.01) {
            throw new IllegalArgumentException(
                    "This implementation supports full payment only. Expected amount: " + order.getOrderValue());
        }

        if (order.getStatus() == OrderStatus.PAYMENT_SUCCESS) {
            return true;
        }

        if (order.getStatus() != OrderStatus.DISPATCHED
                && order.getStatus() != OrderStatus.INVOICED
                && order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    "Payment is allowed only from DISPATCHED/INVOICED/PAYMENT_PENDING. Current status: "
                            + order.getStatus());
        }

        Invoice invoice = billingGenerator.generateInvoice(order);
        if (order.getStatus() == OrderStatus.DISPATCHED) {
            order.updateStatus(OrderStatus.INVOICED, "Invoice generated via UI payOrder");
            order.updateStatus(OrderStatus.PAYMENT_PENDING, "Payment initiated via UI");
        } else if (order.getStatus() == OrderStatus.INVOICED) {
            order.updateStatus(OrderStatus.PAYMENT_PENDING, "Payment initiated via UI");
        }

        Payment payment = paymentProcessor.processPayment(
                invoice,
                order,
                Payment.PaymentMethod.BANK_TRANSFER,
                "UI-PAY-" + orderId + "-" + System.currentTimeMillis()
        );

        orderAnalytics.recordTransaction(payment);
        if (payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
            order.updateStatus(OrderStatus.PAYMENT_SUCCESS, "Payment successful via UI");
            return true;
        } else {
            order.updateStatus(OrderStatus.FAILED, "Payment failed via UI");
            return false;
        }
    }

    /**
     * Cancels an order.
     * Delegates entirely to OrderProcessingController.cancelOrder() which handles
     * state validation and optional inventory release.
     */
    @Override
    public boolean cancelOrder(String orderId, String cancellationReason) throws Exception {
        return orderController.cancelOrder(orderId, cancellationReason);
    }

    /**
     * Returns order stats for the UI dashboard.
     * OrderAnalytics has no individual stat getters; stats are derived from
     * generatePerformanceReport() or from the tracking service's summary.
     *
     * The map keys mirror what the UI team expects; values are extracted from
     * the tracking service where possible, otherwise from the analytics report text.
     */
    @Override
    public Map<String, Object> getOrderStats() throws Exception {
        Map<String, Order> allOrders = getAllTrackedOrders();

        long totalOrders = allOrders.size();

        double totalRevenue = allOrders.values().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAYMENT_SUCCESS)
                .mapToDouble(Order::getOrderValue)
                .sum();

        long pendingApprovals = allOrders.values().stream()
                .filter(o -> o.getStatus() == OrderStatus.VALIDATED)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders",      totalOrders);
        stats.put("revenue",          totalRevenue);
        stats.put("pendingApprovals", pendingApprovals);
        stats.put("approved", allOrders.values().stream().filter(o -> o.getStatus() == OrderStatus.APPROVED).count());
        stats.put("inTransit", allOrders.values().stream().filter(o -> o.getStatus() == OrderStatus.DISPATCHED).count());
        stats.put("cancelled", allOrders.values().stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        return stats;
    }

    // =========================================================================
    // Private helper methods for DTO <-> internal model mapping
    // =========================================================================

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCustomerName(order.getCustomerName());
        dto.setCarVIN(extractVin(order.getOrderDetails()));
        dto.setCarModel(order.getVehicleModel());
        dto.setChassisType(order.getCurrentVariant());
        dto.setAmount(BigDecimal.valueOf(order.getCurrentOrderValue()));
        dto.setDate(order.getOrderDate());
        dto.setNotes(order.getOrderDetails());
        if (order.getStatus() != null) {
            dto.setStatus(mapStatus(order.getStatus()));
            dto.setPaymentStatus(mapPaymentStatus(order.getStatus()));
        }
        if (order.getStatus() == OrderStatus.REJECTED || order.getStatus() == OrderStatus.CANCELLED) {
            dto.setCancellationReason(order.getRejectionReason());
        }
        return dto;
    }

    private Map<String, Order> getAllTrackedOrders() {
        Map<String, Order> orders = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            for (Order order : orderController.getTrackingService().getOrdersByStatus(status)) {
                orders.put(order.getOrderId(), order);
            }
        }
        return orders;
    }

    private String resolveCustomerId(OrderDTO dto) {
        String maybeId = dto.getCustomerName();
        if (maybeId != null && crmAdapter.isValidCustomer(maybeId.trim())) {
            return maybeId.trim();
        }

        String customerName = dto.getCustomerName() == null ? "" : dto.getCustomerName().trim();
        for (int i = 1; i <= 999; i++) {
            String candidate = String.format("C%03d", i);
            if (crmAdapter.isValidCustomer(candidate)
                    && crmAdapter.getCustomerProfile(candidate).equalsIgnoreCase(customerName)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(
                "Unknown customer. Provide a valid customer ID (e.g., C001) or a known CRM customer name.");
    }

    private String mapStatus(OrderStatus status) {
        if (status == null) {
            return OrderDTO.PENDING;
        }
        switch (status) {
            case APPROVED:
                return OrderDTO.APPROVED;
            case REJECTED:
                return OrderDTO.REJECTED;
            case DISPATCHED:
            case ALLOCATED:
                return OrderDTO.IN_TRANSIT;
            case PAYMENT_SUCCESS:
                return OrderDTO.DELIVERED;
            case CANCELLED:
                return OrderDTO.CANCELLED;
            default:
                return OrderDTO.PENDING;
        }
    }

    private String mapPaymentStatus(OrderStatus status) {
        if (status == OrderStatus.PAYMENT_SUCCESS) {
            return OrderDTO.PAY_PAID;
        }
        if (status == OrderStatus.FAILED) {
            return OrderDTO.PAY_FAILED;
        }
        if (status == OrderStatus.CANCELLED) {
            return OrderDTO.PAY_REFUNDED;
        }
        return OrderDTO.PAY_PENDING;
    }

    private String extractVin(String orderDetails) {
        if (orderDetails == null || orderDetails.isBlank()) {
            return "N/A";
        }
        String prefix = "VIN:";
        int start = orderDetails.indexOf(prefix);
        if (start < 0) {
            return "N/A";
        }
        int valueStart = start + prefix.length();
        int valueEnd = orderDetails.indexOf('|', valueStart);
        if (valueEnd < 0) {
            valueEnd = orderDetails.length();
        }
        return orderDetails.substring(valueStart, valueEnd).trim();
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String nonBlankOrDefault(String value, String defaultValue) {
        String normalized = blankToNull(value);
        return normalized == null ? defaultValue : normalized;
    }
}