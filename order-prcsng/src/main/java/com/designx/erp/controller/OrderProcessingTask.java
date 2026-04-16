package com.designx.erp.controller;

import com.designx.erp.model.Payment;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MEMBER 3 - Individual Order Processing Task
 * ==========================================
 * 
 * Represents a single order processing task that runs on a thread.
 * Encapsulates all parameters needed to process an order and handles
 * both successful and failed processing gracefully.
 * 
 * When executed, calls OrderProcessingController.processOrder() which
 * runs the complete pipeline: Capture → Validate → Approve → Fulfill → Bill → Pay
 * 
 * Features:
 *   - Thread-safe execution
 *   - Atomic success/failure counters
 *   - Comprehensive error handling
 *   - Thread identification for logging
 */
public class OrderProcessingTask implements java.util.concurrent.Callable<Payment> {

    private final OrderProcessingController controller;
    private final String customerId;
    private final String vehicleModel;
    private final String vehicleVariant;
    private final String vehicleColor;
    private final String customFeaturesOrAddOns;
    private final String orderDetails;
    private final double orderValue;
    private final Payment.PaymentMethod paymentMethod;
    private final String transactionDetails;
    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;
    
    private Payment result;
    private Exception exception;
    private long executionTimeMs;

    /**
     * Create a new order processing task.
     */
    public OrderProcessingTask(
            OrderProcessingController controller,
            String customerId,
            String vehicleModel,
            String vehicleVariant,
            String vehicleColor,
            String customFeaturesOrAddOns,
            String orderDetails,
            double orderValue,
            Payment.PaymentMethod paymentMethod,
            String transactionDetails,
            AtomicInteger successCount,
            AtomicInteger failureCount) {

        this.controller = controller;
        this.customerId = customerId;
        this.vehicleModel = vehicleModel;
        this.vehicleVariant = vehicleVariant;
        this.vehicleColor = vehicleColor;
        this.customFeaturesOrAddOns = customFeaturesOrAddOns;
        this.orderDetails = orderDetails;
        this.orderValue = orderValue;
        this.paymentMethod = paymentMethod;
        this.transactionDetails = transactionDetails;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    /**
     * Execute the order processing on this thread.
     * Called by the thread pool executor.
     * Implements Callable<Payment> to return result via Future.
     */
    @Override
    public Payment call() throws Exception {
        long taskStart = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();

        try {
            System.out.println("[" + threadName + "] Starting order processing for customer: " + customerId);

            // Call the controller to process the order
            result = controller.processOrder(
                    customerId, vehicleModel, vehicleVariant, vehicleColor,
                    customFeaturesOrAddOns, orderDetails, orderValue,
                    paymentMethod, transactionDetails);

            // Update success counter
            if (result != null) {
                successCount.incrementAndGet();
                System.out.println("[" + threadName + "] ✓ Order for " + customerId + " completed successfully");
            } else {
                failureCount.incrementAndGet();
                System.out.println("[" + threadName + "] ✗ Order for " + customerId + " was rejected");
            }

        } catch (Exception e) {
            exception = e;
            failureCount.incrementAndGet();
            System.err.println("[" + threadName + "] ✗ Exception processing order for " + customerId + ": " + 
                    e.getMessage());
            e.printStackTrace();
        } finally {
            executionTimeMs = System.currentTimeMillis() - taskStart;
            System.out.println("[" + threadName + "] Task completed in " + executionTimeMs + " ms");
        }

        return result;
    }

    /**
     * Call this from main thread to get the result.
     * (Can also throw exception using get() from Future)
     */
    public Payment getResult() {
        return result;
    }

    /**
     * Get any exception that occurred during execution.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Get the execution time of this task.
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Check if task executed successfully.
     */
    public boolean isSuccessful() {
        return exception == null && result != null;
    }
}
