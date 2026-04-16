package com.erp.model.dto;

import java.math.BigDecimal;

/**
 * DTO for a manufacturing material used in BOM creation.
 */
public class MaterialDTO {

    private String materialItemId;
    private String partName;
    private BigDecimal unitCost;
    private double availableQty;

    public MaterialDTO() {}

    public MaterialDTO(String materialItemId, String partName, BigDecimal unitCost, double availableQty) {
        this.materialItemId = materialItemId;
        this.partName = partName;
        this.unitCost = unitCost;
        this.availableQty = availableQty;
    }

    public String getMaterialItemId() { return materialItemId; }
    public void setMaterialItemId(String materialItemId) { this.materialItemId = materialItemId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public double getAvailableQty() { return availableQty; }
    public void setAvailableQty(double availableQty) { this.availableQty = availableQty; }

    @Override
    public String toString() {
        return materialItemId + " - " + partName;
    }
}
