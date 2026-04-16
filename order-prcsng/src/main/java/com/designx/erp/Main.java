package com.designx.erp;

import com.designx.erp.components.*;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.controller.OrderTrackingService;
import com.designx.erp.external.*;
import com.designx.erp.model.Payment;

/**
 * Entry point — wires all components via constructor injection (no Spring needed)
 * and runs a demo of the Order Processing subsystem for a car manufacturing ERP.
 */
public class Main {

    public static void main(String[] args) {

        // ── External module adapters (stubs for CRM, Inventory, Finance) ──────
        CrmModuleAdapter       crm       = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter   finance   = new FinanceModuleAdapter();

        // ── Internal components (dependency-injected) ─────────────────────────
        OrderCaptureUI          captureUI    = new OrderCaptureUI(crm);
        ValidationEngine        validator    = new ValidationEngine(inventory);
        ApprovalWorkflow        approval     = new ApprovalWorkflow();
        FulfillmentOrchestrator fulfillment  = new FulfillmentOrchestrator(inventory);
        BillingGenerator        billing      = new BillingGenerator(finance);
        PaymentProcessor        payment      = new PaymentProcessor(finance);
        OrderAnalytics          analytics    = new OrderAnalytics();

        // ── GRASP Controller ties everything together ─────────────────────────
        OrderProcessingController controller = new OrderProcessingController(
                captureUI, validator, approval, fulfillment, billing, payment, analytics, inventory);

        // ── Test Case 1: Normal order — Tata Nexon ────────────────────────────
        Payment result1 = controller.processOrder(
                "C001",
                "Tata Nexon", "XZ+", "Flame Red",
                "Sunroof, Rear Camera",
                "Standard retail purchase",
                1_450_000.0,
                Payment.PaymentMethod.BANK_TRANSFER,
                "NEFT-TXN-20240401-001"
        );

        // ── Test Case 2: High-value order — Tata Harrier ─────────────────────
        Payment result2 = controller.processOrder(
                "C002",
                "Tata Harrier", "XZA", "Orcus White",
                "None",
                "Corporate fleet order",
                3_200_000.0,
                Payment.PaymentMethod.LOAN_FINANCING,
                "LOAN-TXN-20240401-002"
        );
        
        // ── Extract order IDs for tracking ────────────────────────────────────
        OrderTrackingService tracking = controller.getTrackingService();
        
        // Test Case: Get order status and history
        System.out.println("\n========== ORDER TRACKING SERVICE DEMO ==========");
        if (result1 != null) {
            String invoiceId = result1.getInvoiceId();
            String orderId = result1.getInvoiceId().substring(0, result1.getInvoiceId().indexOf('-'));
            System.out.println("Result1 Invoice ID: " + invoiceId);
            System.out.println("Result1 Order ID: " + orderId);
        }
        
        if (result2 != null) {
            String invoiceId = result2.getInvoiceId();
            String orderId = result2.getInvoiceId().substring(0, result2.getInvoiceId().indexOf('-'));
            System.out.println("Result2 Invoice ID: " + invoiceId);
            System.out.println("Result2 Order ID: " + orderId);
        }
        
        // Show tracking summary
        System.out.println("\n" + tracking.getOrdersSummary());

        // ── Test Case 3: Invalid customer ID — should fail at capture ─────────
        try {
            controller.processOrder(
                    "C999",                          // non-existent customer
                    "Tata Safari", "Adventure", "Tropical Mist",
                    "None", "Order with bad customer", 2_500_000.0,
                    Payment.PaymentMethod.CHEQUE, "CHQ-001"
            );
        } catch (IllegalArgumentException e) {
            System.out.println("[Main] Expected error caught: " + e.getMessage() + "\n");
        }

        // ── Test Case 4: Out-of-stock vehicle ─────────────────────────────────
        Payment result4 = controller.processOrder(
                "C003",
                "Tata Altroz", "XZ", "Avenue White",   // Using correct variant
                "None", "Out of stock test", 900_000.0,
                Payment.PaymentMethod.ONLINE, "UPI-TXN-004"
        );
        
        // Extract and show order tracking
        if (result4 != null) {
            String invoiceId = result4.getInvoiceId();
            String orderId = invoiceId.substring(0, invoiceId.indexOf('-'));
            System.out.println("\n========== TEST ORDER TRACKING FOR ORDER " + orderId + " ==========");
            try {
                System.out.println("Order Status: " + tracking.getOrderStatus(orderId));
            } catch (Exception e) {
                System.out.println("Error retrieving status: " + e.getMessage());
            }
            System.out.println("\n" + tracking.getOrderHistoryAsString(orderId));
        }

        // ── Analytics Report ──────────────────────────────────────────────────
        System.out.println(controller.getAnalyticsReport());
    }
}
