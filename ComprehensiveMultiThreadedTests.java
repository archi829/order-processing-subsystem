package com.designx.erp;

import com.designx.erp.components.*;
import com.designx.erp.controller.MultiThreadedOrderProcessor;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.external.*;
import com.designx.erp.model.Payment;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComprehensiveMultiThreadedTests {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("\\n" + "=".repeat(80));
        System.out.println("COMPREHENSIVE MULTI-THREADED TEST SUITE");
        System.out.println("=".repeat(80) + "\\n");
        try {
            System.out.println("TEST SUITE 1: BASIC FUNCTIONALITY\\n");
            test1_BasicOrderProcessing();
            test2_HighValueOrders();
            test3_MultiplePaymentMethods();

            System.out.println("\\nTEST SUITE 2: ERROR HANDLING\\n");
            test4_InvalidCustomerIds();
            test5_NegativeOrderValues();

            System.out.println("\\nTEST SUITE 3: LARGE-SCALE\\n");
            test7_LargeBatch50Orders();

            System.out.println("\\nTEST SUITE 4: THREAD POOL\\n");
            test9_SingleThreadPool();
            test10_LargeThreadPool();

            System.out.println("\\nTEST SUITE 5: DATA CONSISTENCY\\n");
            test12_CounterAccuracy();

            printFinalReport();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void test1_BasicOrderProcessing() {
        printTestHeader("Test 1: Basic 3-Order");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 4);
        processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Sunroof", "Std", 1_450_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
        processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "Fleet", 3_200_000.0, Payment.PaymentMethod.LOAN_FINANCING, "TXN-002");
        processor.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "Std", 900_000.0, Payment.PaymentMethod.ONLINE, "TXN-003");
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 3 && processor.getSuccessCount() == 3;
        printTestResult("3-order parallel", passed, String.format("Results: %d, Success: %d", results.size(), processor.getSuccessCount()));
    }

    private static void test2_HighValueOrders() {
        printTestHeader("Test 2: High-Value");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 3);
        processor.submitOrder("C001", "Tata Safari", "Adventure", "Tropical Mist", "Premium", "High", 3_500_000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-001");
        processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "Premium", "High", 2_800_000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-002");
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 2 && processor.getSuccessCount() == 2;
        printTestResult("High-value orders", passed, String.format("Success: %d/%d", processor.getSuccessCount(), results.size()));
    }

    private static void test3_MultiplePaymentMethods() {
        printTestHeader("Test 3: Payment Methods");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 4);
        processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "Bank", 1_500_000.0, Payment.PaymentMethod.BANK_TRANSFER, "NEFT-001");
        processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "Loan", 3_200_000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-001");
        processor.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "Online", 900_000.0, Payment.PaymentMethod.ONLINE, "UPI-001");
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 3;
        printTestResult("3 methods", passed, String.format("Success: %d/%d", processor.getSuccessCount(), results.size()));
    }

    private static void test4_InvalidCustomerIds() {
        printTestHeader("Test 4: Invalid Customer");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 2);
        processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "Valid", 1_500_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
        processor.submitOrder("C999", "Tata Harrier", "XZA", "Orcus White", "None", "Invalid", 2_000_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-999");
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 2 && processor.getSuccessCount() >= 1 && processor.getFailureCount() >= 1;
        printTestResult("Invalid rejection", passed, String.format("Success: %d, Failed: %d", processor.getSuccessCount(), processor.getFailureCount()));
    }

    private static void test5_NegativeOrderValues() {
        printTestHeader("Test 5: Invalid Values");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 2);
        processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "Valid", 1_500_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
        processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "Zero", 0.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-002");
        processor.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "Negative", -1_000_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-003");
        List<Payment> results = processor.processAndGetResults();
        boolean passed = processor.getFailureCount() >= 2;
        printTestResult("Invalid rejected", passed, String.format("Failed: %d", processor.getFailureCount()));
    }

    private static void test7_LargeBatch50Orders() {
        printTestHeader("Test 7: 50 Orders");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 8);
        String[] vehicles = {"Nexon", "Harrier", "Altroz", "Safari"};
        String[] variants = {"XZ+", "XZA", "XZ", "Adventure"};
        for (int i = 0; i < 50; i++) {
            int custId = (i % 3) + 1;
            processor.submitOrder("C" + String.format("%03d", custId), "Tata " + vehicles[i % 4], variants[i % 4], "Color" + (i % 3), "Options " + i, "Batch", 1_000_000 + (i * 50_000), Payment.PaymentMethod.BANK_TRANSFER, "TXN-" + i);
        }
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 50;
        printTestResult("50 orders", passed, String.format("Success: %d", processor.getSuccessCount()));
    }

    private static void test9_SingleThreadPool() {
        printTestHeader("Test 9: Single Thread");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 1);
        processor.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "T1", 1_500_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
        processor.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "T2", 3_200_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-002");
        processor.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "T3", 900_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-003");
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 3 && processor.getSuccessCount() == 3;
        printTestResult("Sequential", passed, String.format("Success: %d", processor.getSuccessCount()));
    }

    private static void test10_LargeThreadPool() {
        printTestHeader("Test 10: 32 Threads");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 32);
        for (int i = 0; i < 10; i++) {
            int custId = (i % 3) + 1;
            processor.submitOrder("C" + String.format("%03d", custId), "Tata Nexon", "XZ+", "Flame Red", "Options " + i, "Pool", 1_500_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-" + i);
        }
        List<Payment> results = processor.processAndGetResults();
        boolean passed = results.size() == 10;
        printTestResult("32 threads", passed, String.format("Success: %d", processor.getSuccessCount()));
    }

    private static void test12_CounterAccuracy() {
        printTestHeader("Test 12: Counter Accuracy");
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inventory = new InventoryModuleAdapter();
        FinanceModuleAdapter finance = new FinanceModuleAdapter();
        OrderProcessingController controller = new OrderProcessingController(
                new OrderCaptureUI(crm), new ValidationEngine(inventory),
                new ApprovalWorkflow(), new FulfillmentOrchestrator(inventory),
                new BillingGenerator(finance), new PaymentProcessor(finance),
                new OrderAnalytics(), inventory);
        MultiThreadedOrderProcessor processor = new MultiThreadedOrderProcessor(controller, 8);
        int numOrders = 30;
        for (int i = 0; i < numOrders; i++) {
            int custId = (i % 3) + 1;
            processor.submitOrder("C" + String.format("%03d", custId), "Tata Nexon", "XZ+", "Flame Red", "Options " + i, "Counter", 1_500_000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-" + i);
        }
        List<Payment> results = processor.processAndGetResults();
        int totalSubmitted = processor.getTotalOrders();
        int successCount = processor.getSuccessCount();
        int failureCount = processor.getFailureCount();
        boolean countersMatch = (successCount + failureCount) == totalSubmitted;
        printTestResult("Counters accurate", countersMatch, String.format("Submitted: %d, Success: %d, Failed: %d", totalSubmitted, successCount, failureCount));
    }

    private static void printTestHeader(String testName) {
        System.out.println("  > " + testName);
    }

    private static void printTestResult(String scenario, boolean passed, String details) {
        if (passed) {
            testsPassed++;
            System.out.println("    PASS: " + scenario);
        } else {
            testsFailed++;
            System.out.println("    FAIL: " + scenario);
        }
        System.out.println("    " + details + "\\n");
    }

    private static void printFinalReport() {
        System.out.println("\\n" + "=".repeat(80));
        System.out.println("FINAL TEST REPORT");
        System.out.println("=".repeat(80));
        System.out.println("\\nTests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("Total: " + (testsPassed + testsFailed));
        double passRate = (testsPassed * 100.0) / (testsPassed + testsFailed);
        System.out.println("Pass Rate: " + String.format("%.1f%%", passRate));
        if (testsFailed == 0) {
            System.out.println("\\nALL TESTS PASSED!");
        } else {
            System.out.println("\\nSOME TESTS FAILED");
        }
        System.out.println("=".repeat(80) + "\\n");
    }
}