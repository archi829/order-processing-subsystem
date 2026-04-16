package com.erp.model.dto;

import java.math.BigDecimal;

/**
 * PATTERN: DTO (data-carrier) — single line inside a Bill of Materials.
 */
public class BomItemDTO {

    private String materialItemId;
    private String partName;
    private double quantity;
    private BigDecimal unitCost;
    private BigDecimal lineCost;

    public BomItemDTO() {}

    public BomItemDTO(String materialItemId, String partName, double quantity,
                      BigDecimal unitCost, BigDecimal lineCost) {
        this.materialItemId = materialItemId;
        this.partName = partName;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.lineCost = lineCost;
    }

    public String getMaterialItemId() { return materialItemId; }
    public void setMaterialItemId(String materialItemId) { this.materialItemId = materialItemId; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getLineCost() { return lineCost; }
    public void setLineCost(BigDecimal lineCost) { this.lineCost = lineCost; }
}
