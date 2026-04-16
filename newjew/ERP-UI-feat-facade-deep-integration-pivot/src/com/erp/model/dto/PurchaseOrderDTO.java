package com.erp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: DTO (data-carrier) — Purchase Order header with line items.
 */
public class PurchaseOrderDTO {

    public static final String DRAFT            = "DRAFT";
    public static final String PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String APPROVED         = "APPROVED";
    public static final String DISPATCHED       = "DISPATCHED";
    public static final String RECEIVED         = "RECEIVED";
    public static final String CANCELLED        = "CANCELLED";

    private String poId;
    private String supplierId;
    private String supplierName;
    private List<POLineItemDTO> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private String status;
    private LocalDate createdDate;
    private LocalDate approvalDate;
    private String createdBy;
    private String approvedBy;
    private LocalDate eta;

    public PurchaseOrderDTO() {}

    public PurchaseOrderDTO(String poId, String supplierId, String supplierName,
                            List<POLineItemDTO> items, BigDecimal totalAmount,
                            String status, LocalDate createdDate, String createdBy, LocalDate eta) {
        this.poId = poId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        if (items != null) this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdDate = createdDate;
        this.createdBy = createdBy;
        this.eta = eta;
    }

    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public List<POLineItemDTO> getItems() { return items; }
    public void setItems(List<POLineItemDTO> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDate getEta() { return eta; }
    public void setEta(LocalDate eta) { this.eta = eta; }
}
