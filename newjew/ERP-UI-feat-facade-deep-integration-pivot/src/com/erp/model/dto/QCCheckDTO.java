package com.erp.model.dto;

import java.time.LocalDate;

/**
 * PATTERN: DTO (data-carrier) — a quality-control inspection record.
 */
public class QCCheckDTO {

    private String qcCheckId;
    private String productionOrderId;
    private LocalDate inspectionDate;
    private int sampleSize;
    private int defectsCount;
    private boolean passFailStatus;
    private String inspectorId;

    public QCCheckDTO() {}

    public QCCheckDTO(String qcCheckId, String productionOrderId, LocalDate inspectionDate,
                      int sampleSize, int defectsCount, boolean passFailStatus, String inspectorId) {
        this.qcCheckId = qcCheckId;
        this.productionOrderId = productionOrderId;
        this.inspectionDate = inspectionDate;
        this.sampleSize = sampleSize;
        this.defectsCount = defectsCount;
        this.passFailStatus = passFailStatus;
        this.inspectorId = inspectorId;
    }

    /** @return defect rate as a fraction (0.0 — 1.0); 0 when sample size is zero. */
    public double defectRate() {
        return sampleSize == 0 ? 0.0 : (double) defectsCount / (double) sampleSize;
    }

    public String getQcCheckId() { return qcCheckId; }
    public void setQcCheckId(String qcCheckId) { this.qcCheckId = qcCheckId; }
    public String getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(String productionOrderId) { this.productionOrderId = productionOrderId; }
    public LocalDate getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDate inspectionDate) { this.inspectionDate = inspectionDate; }
    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }
    public int getDefectsCount() { return defectsCount; }
    public void setDefectsCount(int defectsCount) { this.defectsCount = defectsCount; }
    public boolean isPassFailStatus() { return passFailStatus; }
    public void setPassFailStatus(boolean passFailStatus) { this.passFailStatus = passFailStatus; }
    public String getInspectorId() { return inspectorId; }
    public void setInspectorId(String inspectorId) { this.inspectorId = inspectorId; }
}
