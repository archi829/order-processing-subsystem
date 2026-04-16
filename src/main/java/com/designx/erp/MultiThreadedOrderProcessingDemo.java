package com.designx.erp;

import com.designx.erp.components.*;
import com.designx.erp.controller.MultiThreadedOrderProcessor;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.external.*;
import com.designx.erp.model.Payment;
import java.util.List;

/**
 * MULTI-THREADED ORDER PROCESSING DEMO
 * ====================================
 * 
 * Demonstrates parallel order processing using multiple threads.
 * Each order is processed independently on a separate thread from a thread pool.
 * 
 * Benefits:
 *   ✓ Process multiple orders simultaneously
 *   ✓ Better throughput and resource utilization
 *   ✓ Scalable to handle high order volumes
 *   ✓ Performance metrics to track improvement
 * 
 * Comparison:
 *   - Sequential: Process orders one-by-one (blocking)
 *   - Multi-threaded: Process orders in parallel (non-blocking)
 */
public class MultiThreadedOrderProcessingDemo {

    public static void main(String[] args) {

        // ── Initialize external module adapters ───────────────────────────────
        CrmModuleAdapter       crm       = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter   finance   = new FinanceModuleAdapter();

        // ── Initialize internal components ────────────────────────────────────
        OrderCaptureUI          captureUI    = new OrderCaptureUI(crm);
        ValidationEngine        validator    = new ValidationEngine(inventory);
        ApprovalWorkflow        approval     = new ApprovalWorkflow();
        FulfillmentOrchestrator fulfillment  = new FulfillmentOrchestrator(inventory);
        BillingGenerator        billing      = new BillingGenerator(finance);
        PaymentProcessor        payment      = new PaymentProcessor(finance);
        OrderAnalytics          analytics    = new OrderAnalytics();

        // ── Create the main controller ────────────────────────────────────────
        OrderProcessingController controller = new OrderProcessingController(
                captureUI, validator, approval, fulfillment, billing, payment, analytics, inventory);

        System.out.println("\n" + "=".repeat(70));
        System.out.println("     MULTI-THREADED ORDER PROCESSING DEMONSTRATION");
        System.out.println("=".repeat(70));

        // ════════════════════════════════════════════════════════════════════════
        // DEMO 1: SEQUENTIAL PROCESSING (Traditional Single-Thread)
        // ════════════════════════════════════════════════════════════════════════
        System.out.println("\n\n" + "-".repeat(70));
        System.out.println("DEMO 1: SEQUENTIAL PROCESSING (Baseline)");
        System.out.println("-".repeat(70));

        long seqStart = System.currentTimeMillis();

        Payment seq1 = controller.processOrder(
                "C001", "Tata Nexon", "XZ+", "Flame Red",
                "Sunroof, Rear Camera", "Standard retail purchase",
                1_450_000.0, Payment.PaymentMethod.BANK_TRANSFER, "NEFT-TXN-20240401-001");

        Payment seq2 = controller.processOrder(
                "C002", "Tata Harrier", "XZA", "Orcus White",
                "None", "Corporate fleet order",
                3_200_000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-TXN-20240401-002");

        Payment seq3 = controller.processOrder(
                "C003", "Tata Altroz", "XZ", "Avenue White",
                "None", "Out of stock test",
                900_000.0, Payment.PaymentMethod.ONLINE, "UPI-TXN-20240401-003");

        long seqEnd = System.currentTimeMillis();
        long seqTime = seqEnd - seqStart;

        System.out.println("\n[Sequential Results]");
        System.out.println("  Total Time: " + seqTime + " ms");
        System.out.println("  Successful Payments: " + 
                (int) java.util.stream.Stream.of(seq1, seq2, seq3)
                    .filter(java.util.Objects::nonNull).count());

        // ════════════════════════════════════════════════════════════════════════
        // DEMO 2: PARALLEL PROCESSING (NEW - Multi-threaded)
        // ════════════════════════════════════════════════════════════════════════
        System.out.println("\n\n" + "-".repeat(70));
        System.out.println("DEMO 2: PARALLEL PROCESSING (Multi-threaded - 4 threads)");
        System.out.println("-".repeat(70));

        // Create a new controller for parallel processing
        // (Each processor instance should have its own controller to avoid state conflicts)
        OrderProcessingController controllerParallel = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);

        // Create the multi-threaded processor with 4 threads
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controllerParallel, 4);

        long parStart = System.currentTimeMillis();

        // Submit orders to the thread pool
        processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red",
                "Sunroof, Rear Camera", "Standard retail purchase",
                1_450_000.0, Payment.PaymentMethod.BANK_TRANSFER, "NEFT-TXN-20240401-001");

        processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White",
                "None", "Corporate fleet order",
                3_200_000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-TXN-20240401-002");

        processor.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White",
                "None", "Out of stock test",
                900_000.0, Payment.PaymentMethod.ONLINE, "UPI-TXN-20240401-003");

        // Process all orders in parallel and get results
        List<Payment> parallelResults = processor.processAndGetResults();

        long parEnd = System.currentTimeMillis();
        long parTime = parEnd - parStart;

        // ════════════════════════════════════════════════════════════════════════
        // DEMO 3: HIGH VOLUME PARALLEL PROCESSING (10 orders, 8 threads)
        // ════════════════════════════════════════════════════════════════════════
        System.out.println("\n\n" + "-".repeat(70));
        System.out.println("DEMO 3: HIGH VOLUME PARALLEL PROCESSING (10 orders, 8 threads)");
        System.out.println("-".repeat(70));

        OrderProcessingController controllerBatch = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);

        MultiThreadedOrderProcessor batchProcessor = new MultiThreadedOrderProcessor(controllerBatch, 8);

        long batchStart = System.currentTimeMillis();

        // Submit 10 orders to simulate high volume
        String[] vehicles = {"Nexon", "Harrier", "Altroz", "Safari", "Nexon", 
                           "Harrier", "Altroz", "Safari", "Nexon", "Harrier"};
        String[] variants = {"XZ+", "XZA", "XZ", "Adventure", "XZ+", 
                           "XZA", "XZ", "Adventure", "XZ+", "XZA"};
        String[] colors = {"Flame Red", "Orcus White", "Avenue White", "Tropical Mist", "Flame Red",
                         "Orcus White", "Avenue White", "Tropical Mist", "Flame Red", "Orcus White"};

        for (int i = 0; i < 10; i++) {
            String customerId = "C" + String.format("%03d", i + 100);
            double price = 900_000 + (i * 100_000);

            batchProcessor.submitOrder(
                    customerId,
                    "Tata " + vehicles[i],
                    variants[i],
                    colors[i],
                    "Options variant " + (i + 1),
                    "Batch test order",
                    price,
                    Payment.PaymentMethod.BANK_TRANSFER,
                    "TXN-" + String.format("%03d", i + 1)
            );
        }

        List<Payment> batchResults = batchProcessor.processAndGetResults();

        long batchEnd = System.currentTimeMillis();
        long batchTime = batchEnd - batchStart;

        // ════════════════════════════════════════════════════════════════════════
        // PERFORMANCE COMPARISON
        // ════════════════════════════════════════════════════════════════════════
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("PERFORMANCE COMPARISON");
        System.out.println("=".repeat(70));
        
        System.out.println("\n[3 Orders Processing]");
        System.out.println("  Sequential Time:     " + seqTime + " ms");
        System.out.println("  Parallel Time:       " + parTime + " ms");
        System.out.println("  Speedup:             " + String.format("%.2f", (double)seqTime / parTime) + "x");
        System.out.println("  Time Saved:          " + (seqTime - parTime) + " ms (" + 
                String.format("%.1f", 100.0 * (seqTime - parTime) / seqTime) + "%)");

        System.out.println("\n[10 Orders Processing]");
        System.out.println("  Batch Parallel Time: " + batchTime + " ms");
        System.out.println("  Successful Orders:   " + batchProcessor.getSuccessCount());
        System.out.println("  Failed Orders:       " + batchProcessor.getFailureCount());
        System.out.println("  Throughput:          " + 
                String.format("%.2f", (10000.0 / batchTime)) + " orders/second");

        System.out.println("\n" + "=".repeat(70));
        System.out.println("✅ MULTI-THREADED DEMO COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(70) + "\n");
    }
}
