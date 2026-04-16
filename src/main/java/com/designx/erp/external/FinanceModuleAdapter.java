package com.designx.erp.external;

import com.designx.erp.interfaces.IGeneralLedger;
import com.designx.erp.model.Invoice;
import com.designx.erp.model.Payment;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub adapter for the Finance Module — General Ledger (external system).
 *
 * SOLID (DIP + LSP): Implements IGeneralLedger; can be replaced with a real
 *                    Finance/Accounting system client without touching any
 *                    internal billing or payment component.
 *
 * External System reference: ERP Software features #1 (Financial Management)
 *                            and #2 (Accounting).
 * Provides: Accounting Entries, Ledger Updates
 * Receives: Invoice Records, Payment Records
 */
public class FinanceModuleAdapter implements IGeneralLedger {

    // In-memory ledger: orderId/invoiceId → accounting entry string
    private final Map<String, String> ledger = new HashMap<>();

    @Override
    public void postInvoice(Invoice invoice) {
        String entry = String.format(
                "AR_ENTRY | Invoice: %s | Base Amount: ₹%.2f | GST: ₹%.2f | Total: ₹%.2f",
                invoice.getInvoiceId(),
                invoice.getInvoiceAmount(),
                invoice.getTaxDetails(),
                invoice.getTotalAmount()
        );
        ledger.put(invoice.getOrderId(), entry);
        System.out.println("[FinanceModule] Invoice posted to General Ledger: " + entry);
    }

    @Override
    public void recordPayment(Payment payment) {
        String existing = ledger.getOrDefault(payment.getInvoiceId(), "");
        String paymentEntry = String.format(
                " | PAYMENT_ENTRY: %s via %s | Status: %s",
                payment.getPaymentId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus()
        );
        ledger.put(payment.getInvoiceId(), existing + paymentEntry);
        System.out.println("[FinanceModule] Payment recorded in ledger: " + payment.getPaymentId());
    }

    @Override
    public String getAccountingEntries(String orderId) {
        return ledger.getOrDefault(orderId, "No accounting entries found for order: " + orderId);
    }
}
