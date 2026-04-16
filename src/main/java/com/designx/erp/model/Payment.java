package com.designx.erp.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a payment transaction for an invoice.
 */
public class Payment {

    public enum PaymentStatus { SUCCESS, FAILED, PENDING }
    public enum PaymentMethod { BANK_TRANSFER, LOAN_FINANCING, CHEQUE, ONLINE }

    private final String paymentId;
    private final String invoiceId;
    private final PaymentMethod paymentMethod;
    private final String transactionDetails;
    private final LocalDateTime transactionTime;
    private PaymentStatus paymentStatus;

    public Payment(String invoiceId, PaymentMethod paymentMethod, String transactionDetails) {
        this.paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.invoiceId = invoiceId;
        this.paymentMethod = paymentMethod;
        this.transactionDetails = transactionDetails;
        this.transactionTime = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public String getPaymentId()            { return paymentId; }
    public String getInvoiceId()            { return invoiceId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getTransactionDetails()   { return transactionDetails; }
    public LocalDateTime getTransactionTime() { return transactionTime; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus s) { this.paymentStatus = s; }

    @Override
    public String toString() {
        return String.format("Payment[id=%s, invoice=%s, method=%s, status=%s]",
                paymentId, invoiceId, paymentMethod, paymentStatus);
    }
}
