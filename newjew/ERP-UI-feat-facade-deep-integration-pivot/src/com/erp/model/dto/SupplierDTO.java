package com.erp.model.dto;

/**
 * PATTERN: DTO (data-carrier) — a vendor master record.
 */
public class SupplierDTO {

    private String supplierId;
    private String supplierName;
    private boolean complianceStatus;
    private double scorecard;
    private boolean approved;
    private String contactEmail;
    private String paymentTerms;

    public SupplierDTO() {}

    public SupplierDTO(String supplierId, String supplierName, boolean complianceStatus,
                       double scorecard, boolean approved, String contactEmail, String paymentTerms) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.complianceStatus = complianceStatus;
        this.scorecard = scorecard;
        this.approved = approved;
        this.contactEmail = contactEmail;
        this.paymentTerms = paymentTerms;
    }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public boolean isComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(boolean complianceStatus) { this.complianceStatus = complianceStatus; }
    public double getScorecard() { return scorecard; }
    public void setScorecard(double scorecard) { this.scorecard = scorecard; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    @Override public String toString() { return supplierId + " - " + supplierName; }
}
