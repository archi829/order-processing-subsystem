package com.erp.model.dto;

import java.time.LocalDate;

/**
 * PATTERN: DTO (data-carrier) — Goods Receipt Note (GRN) against a PO.
 */
public class GoodsReceiptDTO {

    public static final String PASSED  = "PASSED";
    public static final String FAILED  = "FAILED";
    public static final String ON_HOLD = "ON_HOLD";

    private String grnId;
    private String poId;
    private LocalDate receivedDate;
    private int receivedQty;
    private int expectedQty;
    private String inspectionStatus;
    private String discrepancies;

    public GoodsReceiptDTO() {}

    public GoodsReceiptDTO(String grnId, String poId, LocalDate receivedDate,
                           int receivedQty, int expectedQty,
                           String inspectionStatus, String discrepancies) {
        this.grnId = grnId;
        this.poId = poId;
        this.receivedDate = receivedDate;
        this.receivedQty = receivedQty;
        this.expectedQty = expectedQty;
        this.inspectionStatus = inspectionStatus;
        this.discrepancies = discrepancies;
    }

    public String getGrnId() { return grnId; }
    public void setGrnId(String grnId) { this.grnId = grnId; }
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public LocalDate getReceivedDate() { return receivedDate; }
    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
    public int getReceivedQty() { return receivedQty; }
    public void setReceivedQty(int receivedQty) { this.receivedQty = receivedQty; }
    public int getExpectedQty() { return expectedQty; }
    public void setExpectedQty(int expectedQty) { this.expectedQty = expectedQty; }
    public String getInspectionStatus() { return inspectionStatus; }
    public void setInspectionStatus(String inspectionStatus) { this.inspectionStatus = inspectionStatus; }
    public String getDiscrepancies() { return discrepancies; }
    public void setDiscrepancies(String discrepancies) { this.discrepancies = discrepancies; }
}
