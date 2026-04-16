package com.erp.model.dto;

import java.math.BigDecimal;

/**
 * PATTERN: DTO (data-carrier) — inventory part / SKU master.
 */
public class PartDTO {

    private String partId;
    private String partName;
    private int stockLevel;
    private int safetyStock;
    private int reorderPoint;
    private String warehouseLocation;
    private BigDecimal unitCost;

    public PartDTO() {}

    public PartDTO(String partId, String partName, int stockLevel, int safetyStock,
                   int reorderPoint, String warehouseLocation, BigDecimal unitCost) {
        this.partId = partId;
        this.partName = partName;
        this.stockLevel = stockLevel;
        this.safetyStock = safetyStock;
        this.reorderPoint = reorderPoint;
        this.warehouseLocation = warehouseLocation;
        this.unitCost = unitCost;
    }

    /** @return true when stock level is at or below the reorder point. */
    public boolean isBelowReorderPoint() { return stockLevel <= reorderPoint; }

    /** @return true when stock level has fallen below the safety stock threshold. */
    public boolean isBelowSafetyStock() { return stockLevel < safetyStock; }

    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
    public int getSafetyStock() { return safetyStock; }
    public void setSafetyStock(int safetyStock) { this.safetyStock = safetyStock; }
    public int getReorderPoint() { return reorderPoint; }
    public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }
    public String getWarehouseLocation() { return warehouseLocation; }
    public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
}
