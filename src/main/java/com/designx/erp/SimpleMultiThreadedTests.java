package com.designx.erp;

import com.designx.erp.components.*;
import com.designx.erp.controller.MultiThreadedOrderProcessor;
import com.designx.erp.controller.OrderProcessingController;
import com.designx.erp.external.*;
import com.designx.erp.model.Payment;
import java.util.List;

public class SimpleMultiThreadedTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("MULTI-THREADED ORDER PROCESSING - TEST SUITE");
        System.out.println("=".repeat(80) + "\n");

        test1_BasicThreeOrders();
        test2_HighValueOrders();
        test3_PaymentMethods();
        test4_InvalidCustomer();
        test5_InvalidValues();
        test6_LargeBatch50();
        test7_SingleThread();
        test8_LargeThreadPool();
        test9_CounterAccuracy();

        printReport();
    }

    private static void test1_BasicThreeOrders() {
        print("Test 1: Basic 3-Order Processing");
        try {
            MultiThreadedOrderProcessor p = createProcessor(4);
            p.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Sunroof", "Std", 1450000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
            p.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "Fleet", 3200000.0, Payment.PaymentMethod.LOAN_FINANCING, "TXN-002");
            p.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "Std", 900000.0, Payment.PaymentMethod.ONLINE, "TXN-003");
            List<Payment> r = p.processAndGetResults();
            pass(r.size() == 3 && p.getSuccessCount() == 3, "Results: " + r.size() + ", Success: " + p.getSuccessCount());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test2_HighValueOrders() {
        print("Test 2: High-Value Orders");
        try {
            MultiThreadedOrderProcessor p = createProcessor(3);
            p.submitOrder("C001", "Tata Safari", "Adventure", "Tropical Mist", "Premium", "High", 3500000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-001");
            p.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "Premium", "High", 2800000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-002");
            List<Payment> r = p.processAndGetResults();
            pass(r.size() == 2 && p.getSuccessCount() == 2, "Success: " + p.getSuccessCount() + "/2");
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test3_PaymentMethods() {
        print("Test 3: Multiple Payment Methods");
        try {
            MultiThreadedOrderProcessor p = createProcessor(4);
            p.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "Bank", 1500000.0, Payment.PaymentMethod.BANK_TRANSFER, "NEFT-001");
            p.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "Loan", 3200000.0, Payment.PaymentMethod.LOAN_FINANCING, "LOAN-001");
            p.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "Online", 900000.0, Payment.PaymentMethod.ONLINE, "UPI-001");
            List<Payment> r = p.processAndGetResults();
            pass(r.size() == 3, "3 methods processed");
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test4_InvalidCustomer() {
        print("Test 4: Invalid Customer ID");
        try {
            MultiThreadedOrderProcessor p = createProcessor(2);
            p.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "Valid", 1500000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
            p.submitOrder("C999", "Tata Harrier", "XZA", "Orcus White", "None", "Invalid", 2000000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-999");
            List<Payment> r = p.processAndGetResults();
            pass(p.getFailureCount() >= 1, "Failed: " + p.getFailureCount());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test5_InvalidValues() {
        print("Test 5: Invalid Order Values");
        try {
            MultiThreadedOrderProcessor p = createProcessor(2);
            p.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "Valid", 1500000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
            p.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "Zero", 0.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-002");
            p.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "Neg", -1000000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-003");
            List<Payment> r = p.processAndGetResults();
            pass(p.getFailureCount() >= 2, "Failed: " + p.getFailureCount());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test6_LargeBatch50() {
        print("Test 6: Large Batch - 50 Orders");
        try {
            MultiThreadedOrderProcessor p = createProcessor(8);
            String[] vehicles = {"Nexon", "Harrier", "Altroz", "Safari"};
            String[] variants = {"XZ+", "XZA", "XZ", "Adventure"};
            for (int i = 0; i < 50; i++) {
                int cid = (i % 3) + 1;
                p.submitOrder("C" + String.format("%03d", cid), "Tata " + vehicles[i % 4], variants[i % 4], "Color" + (i % 3), "Opt" + i, "Batch", 1000000.0 + (i * 50000), Payment.PaymentMethod.BANK_TRANSFER, "TXN-" + i);
            }
            List<Payment> r = p.processAndGetResults();
            pass(r.size() == 50, "50 orders processed, success: " + p.getSuccessCount());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test7_SingleThread() {
        print("Test 7: Single Thread Pool");
        try {
            MultiThreadedOrderProcessor p = createProcessor(1);
            p.submitOrder("C001", "Tata Nexon", "XZ+", "Flame Red", "Opt", "T1", 1500000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-001");
            p.submitOrder("C002", "Tata Harrier", "XZA", "Orcus White", "None", "T2", 3200000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-002");
            p.submitOrder("C003", "Tata Altroz", "XZ", "Avenue White", "None", "T3", 900000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-003");
            List<Payment> r = p.processAndGetResults();
            pass(r.size() == 3 && p.getSuccessCount() == 3, "Sequential: " + p.getSuccessCount() + " success");
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test8_LargeThreadPool() {
        print("Test 8: Large Thread Pool - 32 Threads");
        try {
            MultiThreadedOrderProcessor p = createProcessor(32);
            for (int i = 0; i < 10; i++) {
                int cid = (i % 3) + 1;
                p.submitOrder("C" + String.format("%03d", cid), "Tata Nexon", "XZ+", "Flame Red", "Opt" + i, "Pool", 1500000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-" + i);
            }
            List<Payment> r = p.processAndGetResults();
            pass(r.size() == 10, "10 orders with 32 threads");
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static void test9_CounterAccuracy() {
        print("Test 9: Counter Accuracy");
        try {
            MultiThreadedOrderProcessor p = createProcessor(8);
            for (int i = 0; i < 30; i++) {
                int cid = (i % 3) + 1;
                p.submitOrder("C" + String.format("%03d", cid), "Tata Nexon", "XZ+", "Flame Red", "Opt" + i, "Counter", 1500000.0, Payment.PaymentMethod.BANK_TRANSFER, "TXN-" + i);
            }
            List<Payment> r = p.processAndGetResults();
            int submitted = p.getTotalOrders();
            int success = p.getSuccessCount();
            int failed = p.getFailureCount();
            boolean match = (success + failed) == submitted;
            pass(match, "Submitted: " + submitted + ", Success: " + success + ", Failed: " + failed);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    private static MultiThreadedOrderProcessor createProcessor(int threads) {
        CrmModuleAdapter crm = new CrmModuleAdapter();
        InventoryModuleAdapter inv = new InventoryModuleAdapter();
        FinanceModuleAdapter fin = new FinanceModuleAdapter();
        OrderProcessingController ctrl = new OrderProcessingController(
            new OrderCaptureUI(crm), new ValidationEngine(inv),
            new ApprovalWorkflow(), new FulfillmentOrchestrator(inv),
            new BillingGenerator(fin), new PaymentProcessor(fin),
            new OrderAnalytics(), inv);
        return new MultiThreadedOrderProcessor(ctrl, threads);
    }

    private static void print(String msg) {
        System.out.println("\n  > " + msg);
    }

    private static void pass(boolean condition, String msg) {
        if (condition) {
            passed++;
            System.out.println("    PASS: " + msg);
        } else {
            failed++;
            System.out.println("    FAIL: " + msg);
        }
    }

    private static void fail(String msg) {
        failed++;
        System.out.println("    FAIL: " + msg);
    }

    private static void printReport() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST REPORT");
        System.out.println("=".repeat(80));
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Total: " + (passed + failed));
        if (failed == 0) {
            System.out.println("\nALL TESTS PASSED!");
        }
        System.out.println("=".repeat(80) + "\n");
    }
}
