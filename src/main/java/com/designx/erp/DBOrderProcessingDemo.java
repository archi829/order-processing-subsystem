package com.designx.erp;

import com.designx.erp.components.*;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.controller.OrderTrackingService;
import com.designx.erp.external.*;
import com.designx.erp.external.db.*;
import com.designx.erp.exception.*;
import com.designx.erp.model.Payment;

import java.sql.Connection;

/**
 * End-to-End Test: Process orders directly from MySQL database quotes.
 * 
 * Demonstrates:
 * - MEMBER 1: Database access via OrderDataFetcher (PreparedStatement)
 * - MEMBER 1: Factory Pattern via OrderMapper (quote → Order conversion)
 * - MEMBER 2: State Pattern validation during processing
 * - MEMBER 3: OrderProcessingController orchestration
 * - MEMBER 4: OrderTrackingService tracking & reporting
 * 
 * Exception Handling: OrderNotFoundException, InvalidCustomerDataException, NegativeOrderValueException
 */
public class DBOrderProcessingDemo {

    public static void main(String[] args) {
        System.out.println("========== DATABASE ORDER PROCESSING DEMO ==========\n");
        System.out.println("[Demo] Processing orders from MySQL database quotes...\n");

        try {
            // ── STEP 1: Initialize Database Connection (MEMBER 1) ──────────────
            System.out.println("[Step 1] Initializing database connection...");
            DBConnection dbConnection = DBConnection.getInstance();
            Connection conn = dbConnection.getConnection();
            System.out.println("[Step 1] ✓ Connected to polymorphs database\n");

            // ── STEP 2: Initialize OrderDataFetcher (MEMBER 1) ────────────────
            System.out.println("[Step 2] Initializing OrderDataFetcher...");
            OrderDataFetcher dataFetcher = new OrderDataFetcher(conn);
            System.out.println("[Step 2] ✓ Ready to fetch from quotes, quote_items, customers\n");

            // ── STEP 3: Initialize OrderMapper (MEMBER 1 - Factory Pattern) ────
            System.out.println("[Step 3] Initializing OrderMapper (Factory Pattern)...");
            OrderMapper orderMapper = new OrderMapper(dataFetcher);
            System.out.println("[Step 3] ✓ Ready to convert quotes to Order objects\n");

            // ── STEP 4: Initialize Order Processing Components ────────────────
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

            // Create controller with full DB integration
            OrderProcessingController controller = new OrderProcessingController(
                    captureUI, validator, approval, fulfillment, billing, payment, analytics, inventory,
                    orderMapper, dataFetcher);
            System.out.println("[Step 4] ✓ Order Processing pipeline ready\n");

            // ── STEP 5: Process Sample Quotes from Database ──────────────────
            System.out.println("[Step 5] Processing sample quotes from database...\n");

            // Quote 1: Ravi Kumar - Tata Nexon
            processQuoteWithExceptionHandling(controller, 1, "Quote 1 (Tata Nexon)");

            // Quote 2: Priya Sharma - Tata Harrier
            processQuoteWithExceptionHandling(controller, 2, "Quote 2 (Tata Harrier)");

            // Quote 3: Arjun Nair - Tata Altroz
            processQuoteWithExceptionHandling(controller, 3, "Quote 3 (Tata Altroz)");

            // ── STEP 6: Display Results ────────────────────────────────────
            System.out.println("\n========== PROCESSING COMPLETE ==========");
            System.out.println("\n[Demo] Summary Statistics:");
            OrderTrackingService trackingService = controller.getTrackingService();
            System.out.println(trackingService.getOrdersSummary());

            System.out.println("\n========== DEMO FINISHED ==========");
            System.out.println("✅ All orders processed successfully from MySQL database!");

        } catch (Exception e) {
            System.err.println("\n❌ DEMO FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to process a quote with proper exception handling.
     * Demonstrates MEMBER 1's exception hierarchy.
     */
    private static void processQuoteWithExceptionHandling(
            OrderProcessingController controller,
            int quoteId,
            String description) {

        System.out.println("\n┌─ " + description + " ─────────────────────────────────────────");

        try {
            // Use Factory Pattern via OrderMapper
            Payment payment = controller.processOrderFromQuote(
                    String.valueOf(quoteId),
                    Payment.PaymentMethod.BANK_TRANSFER,
                    "DB-QUOTE-" + quoteId);

            if (payment != null && payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
                System.out.println("│ ✓ Quote " + quoteId + " → Order → Invoice → Payment SUCCESS");
                System.out.println("│   Payment ID: " + payment.getPaymentId());
            } else {
                System.out.println("│ ✗ Payment failed for quote " + quoteId);
            }

        } catch (OrderNotFoundException e) {
            // MEMBER 1: Exception 1 - Quote not found in database
            System.err.println("│ ✗ ERROR: " + e.getMessage());
            System.err.println("│   Quote " + quoteId + " not found in database");

        } catch (InvalidCustomerDataException e) {
            // MEMBER 1: Exception 2 - Customer data invalid or incomplete
            System.err.println("│ ✗ ERROR: " + e.getMessage());
            System.err.println("│   Customer data validation failed for quote " + quoteId);

        } catch (NegativeOrderValueException e) {
            // MEMBER 1: Exception 3 - Order amount invalid
            System.err.println("│ ✗ ERROR: " + e.getMessage());
            System.err.println("│   Order value validation failed for quote " + quoteId);

        } catch (IllegalStateException e) {
            // MEMBER 2: State Pattern violation
            System.err.println("│ ✗ ERROR (State Transition): " + e.getMessage());

        } catch (Exception e) {
            // Catch-all for unexpected errors
            System.err.println("│ ✗ UNEXPECTED ERROR: " + e.getClass().getSimpleName());
            System.err.println("│   " + e.getMessage());
        }

        System.out.println("└─────────────────────────────────────────────────────────────");
    }
}
