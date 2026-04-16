package com.designx.erp.components;

import com.designx.erp.interfaces.IGeneralLedger;
import com.designx.erp.model.Invoice;
import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;
import com.designx.erp.model.Payment;

/**
 * GRASP (Information Expert): Knows how to process payment for a given invoice.
 * SOLID (SRP): Handles payment processing only; does not generate invoices.
 * SOLID (DIP): Depends on IGeneralLedger interface for recording payments.
 *
 * Reads:  Invoice ID, Transaction Details
 * Writes: Payment ID, Payment Method, Payment Status
 *         → also records the payment in Finance Module via IGeneralLedger
 */
public class PaymentProcessor {

    private final IGeneralLedger financeModule;

    public PaymentProcessor(IGeneralLedger financeModule) {
        this.financeModule = financeModule;
    }

    /**
     * Processes payment for a given invoice and updates the order status accordingly.
     *
     * @param invoice          the invoice to pay
     * @param order            the corresponding order (status is updated here)
     * @param paymentMethod    the method of payment chosen by the customer
     * @param transactionDetails  bank/loan reference string
     * @return the resulting Payment object
     */
    public Payment processPayment(Invoice invoice,
                                  Order order,
                                  Payment.PaymentMethod paymentMethod,
                                  String transactionDetails) {

        Payment payment = new Payment(invoice.getInvoiceId(), paymentMethod, transactionDetails);

        boolean success = simulatePaymentGateway(paymentMethod, invoice.getTotalAmount());

        if (success) {
            payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            financeModule.recordPayment(payment);   // record in Finance Module ledger
            System.out.println("[PaymentProcessor] Payment SUCCESS: " + payment);
        } else {
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            System.out.println("[PaymentProcessor] Payment FAILED: " + payment);
        }

        return payment;
    }

    /**
     * Stub for a real payment gateway call.
     * In production this would call an external payment API.
     */
    private boolean simulatePaymentGateway(Payment.PaymentMethod method, double amount) {
        // Always succeeds in this stub; replace with real gateway logic
        System.out.println("[PaymentProcessor] Gateway called — method: " + method
                + ", amount: ₹" + String.format("%.2f", amount));
        return true;
    }
}
