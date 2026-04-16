package com.erp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PATTERN: DTO (data-carrier) — Supplier invoice tied to a PO/GRN.
 */
public class InvoiceDTO {

    public static final String PENDING    = "PENDING";
    public static final String AUTHORIZED = "AUTHORIZED";
    public static final String PAID       = "PAID";
    public static final String DISPUTED   = "DISPUTED";

    private String invoiceId;
    private String supplierId;
    private String poId;
    private String grnId;
    private BigDecimal invoiceAmount;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String paymentStatus;

    public InvoiceDTO() {}

    public InvoiceDTO(String invoiceId, String supplierId, String poId, String grnId,
                      BigDecimal invoiceAmount, LocalDate invoiceDate, LocalDate dueDate,
                      String paymentStatus) {
        this.invoiceId = invoiceId;
        this.supplierId = supplierId;
        this.poId = poId;
        this.grnId = grnId;
        this.invoiceAmount = invoiceAmount;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.paymentStatus = paymentStatus;
    }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getGrnId() { return grnId; }
    public void setGrnId(String grnId) { this.grnId = grnId; }
    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
