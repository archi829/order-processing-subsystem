package com.erp.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: DTO (data-carrier) — Bill of Materials header + items aggregate.
 */
public class BomDTO {

    private String bomId;
    private String productId;
    private String productName;
    private String bomVersion;
    private boolean active;
    private BigDecimal totalCost;
    private BigDecimal budgetLimit;
    private List<BomItemDTO> items = new ArrayList<>();

    public BomDTO() {}

    public BomDTO(String bomId, String productId, String productName, String bomVersion,
                  boolean active, BigDecimal totalCost, List<BomItemDTO> items) {
        this.bomId = bomId;
        this.productId = productId;
        this.productName = productName;
        this.bomVersion = bomVersion;
        this.active = active;
        this.totalCost = totalCost;
        this.budgetLimit = BigDecimal.ZERO;
        if (items != null) this.items = items;
    }

    public String getBomId() { return bomId; }
    public void setBomId(String bomId) { this.bomId = bomId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBomVersion() { return bomVersion; }
    public void setBomVersion(String bomVersion) { this.bomVersion = bomVersion; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getBudgetLimit() { return budgetLimit; }
    public void setBudgetLimit(BigDecimal budgetLimit) { this.budgetLimit = budgetLimit; }
    public List<BomItemDTO> getItems() { return items; }
    public void setItems(List<BomItemDTO> items) { this.items = items; }
}
