package com.designx.erp.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a billing invoice for a vehicle order.
 */
public class Invoice {

    public enum InvoiceStatus { GENERATED, SENT, PAID, OVERDUE }

    private final String invoiceId;
    private final String orderId;
    private final String customerDetails;
    private final double invoiceAmount;
    private final double taxDetails;
    private final LocalDate issueDate;
    private InvoiceStatus status;

    public Invoice(String orderId, String customerDetails, double invoiceAmount, double taxDetails) {
        this.invoiceId = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.orderId = orderId;
        this.customerDetails = customerDetails;
        this.invoiceAmount = invoiceAmount;
        this.taxDetails = taxDetails;
        this.issueDate = LocalDate.now();
        this.status = InvoiceStatus.GENERATED;
    }

    public String getInvoiceId()        { return invoiceId; }
    public String getOrderId()          { return orderId; }
    public String getCustomerDetails()  { return customerDetails; }
    public double getInvoiceAmount()    { return invoiceAmount; }
    public double getTaxDetails()       { return taxDetails; }
    public LocalDate getIssueDate()     { return issueDate; }
    public InvoiceStatus getStatus()    { return status; }
    public void setStatus(InvoiceStatus s) { this.status = s; }

    public double getTotalAmount()      { return invoiceAmount + taxDetails; }

    @Override
    public String toString() {
        return String.format("Invoice[id=%s, order=%s, total=%.2f, status=%s]",
                invoiceId, orderId, getTotalAmount(), status);
    }
}
