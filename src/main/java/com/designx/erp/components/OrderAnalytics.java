package com.designx.erp.components;

import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;
import com.designx.erp.model.Payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GRASP (Information Expert): Has access to all order lifecycle and transaction data.
 * SOLID (SRP): Concerned with analytics and reporting only.
 * SOLID (OCP): New report types can be added without modifying existing methods.
 *
 * Reads:  Order ID, Order Processing Time, Order Status Data, Revenue Data
 * Writes: Performance Reports, Dashboards, Metrics
 */
public class OrderAnalytics {

    private final List<Order>   orderData       = new ArrayList<>();
    private final List<Payment> transactionData = new ArrayList<>();

    /** Called by the controller after each order is captured. */
    public void recordOrder(Order order) {
        orderData.add(order);
    }

    /** Called by the controller after each payment attempt. */
    public void recordTransaction(Payment payment) {
        transactionData.add(payment);
    }

    /**
     * Generates a plain-text performance report covering:
     *  - total / completed / rejected order counts
     *  - successful payment count
     *  - revenue breakdown by vehicle model
     */
    public String generatePerformanceReport() {
        long total     = orderData.size();
        long completed = orderData.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAYMENT_SUCCESS).count();
        long rejected  = orderData.stream()
                .filter(o -> o.getStatus() == OrderStatus.REJECTED).count();
        long paidCount = transactionData.stream()
                .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.SUCCESS).count();

        Map<String, Long> vehicleBreakdown = orderData.stream()
                .collect(Collectors.groupingBy(Order::getVehicleModel, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        sb.append("============================================\n");
        sb.append("   ORDER PROCESSING PERFORMANCE REPORT\n");
        sb.append("   Car Manufacturing ERP — DesignX Team\n");
        sb.append("============================================\n");
        sb.append(String.format("  Total Orders Received  : %d%n", total));
        sb.append(String.format("  Successfully Completed : %d%n", completed));
        sb.append(String.format("  Rejected               : %d%n", rejected));
        sb.append(String.format("  Successful Payments    : %d%n", paidCount));
        sb.append("--------------------------------------------\n");
        sb.append("  Vehicle Model Breakdown:\n");
        vehicleBreakdown.forEach((model, count) ->
                sb.append(String.format("    %-25s : %d order(s)%n", model, count)));
        sb.append("============================================\n");
        return sb.toString();
    }
}
