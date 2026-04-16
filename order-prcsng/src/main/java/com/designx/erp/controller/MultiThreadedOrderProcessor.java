package com.designx.erp.controller;

import com.designx.erp.model.Payment;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MEMBER 3 - Multi-Threaded Order Processing
 * ==========================================
 * 
 * Orchestrates parallel order processing using ExecutorService thread pool.
 * Each order is processed independently on a separate thread, enabling
 * scalable parallel processing of multiple orders simultaneously.
 * 
 * Key Features:
 *   - Thread-safe processing of multiple orders in parallel
 *   - Configurable thread pool size
 *   - Result collection and tracking
 *   - Exception handling per order
 *   - Performance metrics (execution time, throughput)
 *   - Graceful shutdown and error recovery
 * 
 * Usage Pattern:
 *   MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, numThreads);
 *   processor.submitOrder(customerId, vehicleModel, ...);
 *   processor.submitOrder(customerId2, vehicleModel2, ...);
 *   List<Payment> results = processor.processAndGetResults();
 */
public class MultiThreadedOrderProcessor {

    private final OrderProcessingController controller;
    private final ExecutorService executor;
    private final List<OrderProcessingTask> taskList;
    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;
    private long startTime;
    private long endTime;

    /**
     * Initialize the multi-threaded processor with a thread pool.
     * 
     * @param controller the OrderProcessingController to use for processing
     * @param numThreads the number of threads in the pool (e.g., 4, 8, 16)
     */
    public MultiThreadedOrderProcessor(OrderProcessingController controller, int numThreads) {
        this.controller = controller;
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.taskList = Collections.synchronizedList(new ArrayList<>());
        this.successCount = new AtomicInteger(0);
        this.failureCount = new AtomicInteger(0);
    }

    /**
     * Submit a new order for processing.
     * The order will be queued and processed when a thread becomes available.
     */
    public void submitOrder(String customerId,
                           String vehicleModel,
                           String vehicleVariant,
                           String vehicleColor,
                           String customFeaturesOrAddOns,
                           String orderDetails,
                           double orderValue,
                           Payment.PaymentMethod paymentMethod,
                           String transactionDetails) {

        OrderProcessingTask task = new OrderProcessingTask(
                controller,
                customerId, vehicleModel, vehicleVariant, vehicleColor,
                customFeaturesOrAddOns, orderDetails, orderValue,
                paymentMethod, transactionDetails,
                successCount, failureCount);

        taskList.add(task);
    }

    /**
     * Submit all queued orders to the thread pool and wait for results.
     * Blocks until all orders have been processed.
     * 
     * @return a list of Payment results in the order tasks were submitted
     */
    public List<Payment> processAndGetResults() {
        return processAndGetResults(5, TimeUnit.MINUTES);
    }

    /**
     * Submit all queued orders and wait with a timeout.
     * 
     * @param timeout maximum time to wait
     * @param unit time unit for timeout
     * @return list of Payment results
     */
    public List<Payment> processAndGetResults(long timeout, TimeUnit unit) {
        if (taskList.isEmpty()) {
            System.out.println("[MultiThreadedProcessor] No orders to process.");
            return new ArrayList<>();
        }

        System.out.println("\n========== MULTI-THREADED ORDER PROCESSING STARTED ==========");
        System.out.println("[MultiThreadedProcessor] Processing " + taskList.size() + " orders in parallel...");
        System.out.println("[MultiThreadedProcessor] Thread pool size: " + 
                ((ThreadPoolExecutor) executor).getCorePoolSize());

        startTime = System.currentTimeMillis();
        List<Future<Payment>> futures = new ArrayList<>();

        // Submit all tasks to the executor
        for (OrderProcessingTask task : taskList) {
            futures.add(executor.submit(task));
        }

        // Collect results
        List<Payment> results = new ArrayList<>();
        int completed = 0;

        try {
            for (int i = 0; i < futures.size(); i++) {
                Future<Payment> future = futures.get(i);
                try {
                    Payment payment = future.get(timeout, unit);
                    results.add(payment);
                    completed++;
                    printProgress(i + 1, taskList.size());
                } catch (TimeoutException e) {
                    System.err.println("[Task " + (i + 1) + "] TIMEOUT: Order processing took too long");
                    results.add(null);
                    failureCount.incrementAndGet();
                } catch (ExecutionException e) {
                    System.err.println("[Task " + (i + 1) + "] EXECUTION ERROR: " + e.getCause().getMessage());
                    results.add(null);
                    failureCount.incrementAndGet();
                } catch (InterruptedException e) {
                    System.err.println("[Task " + (i + 1) + "] INTERRUPTED: " + e.getMessage());
                    results.add(null);
                    failureCount.incrementAndGet();
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            endTime = System.currentTimeMillis();
            executor.shutdown();
        }

        printSummary(results);
        return results;
    }

    /**
     * Get the number of submitted orders.
     */
    public int getTotalOrders() {
        return taskList.size();
    }

    /**
     * Get the number of successfully processed orders.
     */
    public int getSuccessCount() {
        return successCount.get();
    }

    /**
     * Get the number of failed orders.
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * Get the total execution time in milliseconds.
     */
    public long getExecutionTimeMs() {
        return endTime - startTime;
    }

    /**
     * Shutdown the executor gracefully.
     */
    public void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * Force shutdown the executor.
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }

    private void printProgress(int completed, int total) {
        int percentage = (int) (100.0 * completed / total);
        System.out.println("[MultiThreadedProcessor] Progress: " + completed + "/" + total + 
                " (" + percentage + "%)");
    }

    private void printSummary(List<Payment> results) {
        long executionTime = endTime - startTime;
        int total = results.size();
        int successfulPayments = (int) results.stream().filter(Objects::nonNull).count();
        int nullPayments = total - successfulPayments;

        System.out.println("\n========== MULTI-THREADED PROCESSING SUMMARY ==========");
        System.out.println("[Summary] Total Orders Submitted: " + total);
        System.out.println("[Summary] Successfully Processed: " + successCount.get());
        System.out.println("[Summary] Failed/Rejected: " + failureCount.get());
        System.out.println("[Summary] Successful Payments: " + successfulPayments);
        System.out.println("[Summary] Null Results (Rejected Orders): " + nullPayments);
        System.out.println("[Summary] Total Execution Time: " + executionTime + " ms");
        if (executionTime > 0) {
            double throughput = (total * 1000.0) / executionTime;
            System.out.println("[Summary] Throughput: " + String.format("%.2f", throughput) + " orders/second");
        }
        System.out.println("========== MULTI-THREADED PROCESSING COMPLETE ==========\n");
    }

    /**
     * Clear all submitted tasks (useful for batch processing).
     */
    public void clear() {
        taskList.clear();
        successCount.set(0);
        failureCount.set(0);
    }
}
