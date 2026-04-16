package com.designx.erp;

import com.designx.erp.components.*;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.controller.OrderTrackingService;
import com.designx.erp.external.*;
import com.designx.erp.external.db.*;
import com.designx.erp.exception.*;
import com.designx.erp.model.Payment;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MULTI-THREADED DATABASE ORDER PROCESSING DEMO
 * =============================================
 * 
 * Demonstrates parallel processing of quotes directly from MySQL database.
 * Each quote is fetched and converted to an order, then processed in parallel.
 * 
 * Workflow:
 * 1. Connect to database
 * 2. Fetch all quotes (or a batch)
 * 3. Submit each quote to a thread pool for processing
 * 4. Each thread: Fetch quote → Map to Order → Process order pipeline
 * 5. Collect results and display statistics
 * 
 * Benefits:
 *   - Process multiple database quotes simultaneously
 *   - Efficient database connection pooling
 *   - Better throughput for high volume quote-to-order conversion
 *   - Scalable to handle enterprise order volumes
 */
public class MultiThreadedDBOrderProcessingDemo {

    public static void main(String[] args) {
        System.out.println("========== MULTI-THREADED DATABASE ORDER PROCESSING ==========\n");
        System.out.println("[Demo] Processing quotes from MySQL database in parallel...\n");

        try {
            // ── STEP 1: Initialize Database Connection ────────────────────────
            System.out.println("[Step 1] Initializing database connection...");
            DBConnection dbConnection = DBConnection.getInstance();
            Connection conn = dbConnection.getConnection();
            System.out.println("[Step 1] ✓ Connected to polymorphs database\n");

            // ── STEP 2: Initialize OrderDataFetcher ─────────────────────────
            System.out.println("[Step 2] Initializing OrderDataFetcher...");
            OrderDataFetcher dataFetcher = new OrderDataFetcher(conn);
            System.out.println("[Step 2] ✓ Ready to fetch from quotes, quote_items, customers\n");

            // ── STEP 3: Initialize OrderMapper ───────────────────────────────
            System.out.println("[Step 3] Initializing OrderMapper (Factory Pattern)...");
            OrderMapper orderMapper = new OrderMapper(dataFetcher);
            System.out.println("[Step 3] ✓ Ready to convert quotes to Order objects\n");

            // ── STEP 4: Setup Components ────────────────────────────────────
            System.out.println("[Step 4] Initializing Order Processing pipeline...");
            CrmModuleAdapter       crm       = new CrmModuleAdapter();
            InventoryModuleAdapter inventory = new InventoryModuleAdapter();
            FinanceModuleAdapter   finance   = new FinanceModuleAdapter();

            OrderCaptureUI          captureUI    = new OrderCaptureUI(crm);
            ValidationEngine        validator    = new ValidationEngine(inventory);
            ApprovalWorkflow        approval     = new ApprovalWorkflow();
            FulfillmentOrchestrator fulfillment  = new FulfillmentOrchestrator(inventory);
            BillingGenerator        billing      = new BillingGenerator(finance);
            PaymentProcessor        payment      = new PaymentProcessor(finance);
            OrderAnalytics          analytics    = new OrderAnalytics();

            OrderProcessingController controller = new OrderProcessingController(
                    captureUI, validator, approval, fulfillment, billing, payment, analytics, inventory,
                    orderMapper, dataFetcher);
            System.out.println("[Step 4] ✓ Order Processing pipeline ready\n");

            // ── STEP 5: Setup Multi-Threaded Processing ────────────────────────
            System.out.println("[Step 5] Setting up multi-threaded quote processing...");
            int numThreads = 4;
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            List<Future<PaymentResult>> futures = new ArrayList<>();
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            System.out.println("[Step 5] ✓ Thread pool created with " + numThreads + " threads\n");

            // ── STEP 6: Submit Quote Processing Tasks ───────────────────────
            System.out.println("[Step 6] Submitting quote processing tasks...\n");

            // Submit Quote 1
            futures.add(executor.submit(() -> 
                processQuoteInThread(controller, 1, successCount, failureCount, "Quote 1 (Tata Nexon)")));

            // Submit Quote 2
            futures.add(executor.submit(() -> 
                processQuoteInThread(controller, 2, successCount, failureCount, "Quote 2 (Tata Harrier)")));

            // Submit Quote 3
            futures.add(executor.submit(() -> 
                processQuoteInThread(controller, 3, successCount, failureCount, "Quote 3 (Tata Altroz)")));

            System.out.println("[Step 6] ✓ All quote processing tasks submitted\n");

            // ── STEP 7: Collect Results ─────────────────────────────────────
            System.out.println("[Step 7] Waiting for all tasks to complete...\n");

            List<PaymentResult> results = new ArrayList<>();
            long startTime = System.currentTimeMillis();

            for (Future<PaymentResult> future : futures) {
                try {
                    PaymentResult result = future.get(5, TimeUnit.MINUTES);
                    results.add(result);
                } catch (TimeoutException | ExecutionException | InterruptedException e) {
                    System.err.println("[Step 7] Task error: " + e.getMessage());
                    failureCount.incrementAndGet();
                }
            }

            long endTime = System.currentTimeMillis();
            executor.shutdown();

            // ── STEP 8: Display Results ────────────────────────────────────
            System.out.println("\n========== PROCESSING COMPLETE ==========");
            System.out.println("\n[Demo] Multi-Threaded Processing Statistics:");
            System.out.println("  Total Quotes Processed: " + results.size());
            System.out.println("  Successful Payments: " + successCount.get());
            System.out.println("  Failed Orders: " + failureCount.get());
            System.out.println("  Execution Time: " + (endTime - startTime) + " ms");
            System.out.println("  Throughput: " + 
                String.format("%.2f", (results.size() * 1000.0) / (endTime - startTime)) + 
                " quotes/second");

            OrderTrackingService trackingService = controller.getTrackingService();
            System.out.println("\n" + trackingService.getOrdersSummary());

            System.out.println("\n========== DEMO FINISHED ==========");
            System.out.println("✅ All quotes processed in parallel from MySQL database!");

        } catch (Exception e) {
            System.err.println("\n❌ DEMO FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process a single quote in a separate thread.
     * This method runs on a thread pool thread.
     */
    private static PaymentResult processQuoteInThread(
            OrderProcessingController controller,
            int quoteId,
            AtomicInteger successCount,
            AtomicInteger failureCount,
            String description) {

        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Starting: " + description);

        PaymentResult result = new PaymentResult(quoteId, description);

        try {
            Payment payment = controller.processOrderFromQuote(
                    String.valueOf(quoteId),
                    Payment.PaymentMethod.BANK_TRANSFER,
                    "DB-QUOTE-" + quoteId);

            if (payment != null && payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
                System.out.println("[" + threadName + "] ✓ " + description + 
                        " → Payment SUCCESS (ID: " + payment.getPaymentId() + ")");
                result.setPayment(payment);
                result.setSuccess(true);
                successCount.incrementAndGet();
            } else {
                System.out.println("[" + threadName + "] ✗ " + description + " → Payment FAILED");
                result.setSuccess(false);
                failureCount.incrementAndGet();
            }

        } catch (OrderNotFoundException e) {
            System.err.println("[" + threadName + "] ✗ " + description + 
                    " → ERROR: " + e.getMessage());
            result.setError(e.getMessage());
            result.setSuccess(false);
            failureCount.incrementAndGet();

        } catch (InvalidCustomerDataException e) {
            System.err.println("[" + threadName + "] ✗ " + description + 
                    " → ERROR: " + e.getMessage());
            result.setError(e.getMessage());
            result.setSuccess(false);
            failureCount.incrementAndGet();

        } catch (NegativeOrderValueException e) {
            System.err.println("[" + threadName + "] ✗ " + description + 
                    " → ERROR: " + e.getMessage());
            result.setError(e.getMessage());
            result.setSuccess(false);
            failureCount.incrementAndGet();

        } catch (Exception e) {
            System.err.println("[" + threadName + "] ✗ " + description + 
                    " → UNEXPECTED ERROR: " + e.getMessage());
            result.setError(e.getMessage());
            result.setSuccess(false);
            failureCount.incrementAndGet();
        }

        return result;
    }

    /**
     * Simple data class to hold the result of a quote processing task.
     */
    private static class PaymentResult {
        int quoteId;
        String description;
        Payment payment;
        String error;
        boolean success;

        PaymentResult(int quoteId, String description) {
            this.quoteId = quoteId;
            this.description = description;
            this.success = false;
        }

        void setPayment(Payment payment) { this.payment = payment; }
        void setError(String error) { this.error = error; }
        void setSuccess(boolean success) { this.success = success; }

        @Override
        public String toString() {
            return "PaymentResult{" +
                    "quoteId=" + quoteId +
                    ", description='" + description + '\'' +
                    ", success=" + success +
                    (error != null ? ", error='" + error + '\'' : "") +
                    '}';
        }
    }
}
