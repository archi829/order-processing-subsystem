package com.erp.model.dto;

import java.math.BigDecimal;

/**
 * PATTERN: DTO (data-carrier) — single line inside a Purchase Order.
 */
public class POLineItemDTO {

    private int lineNumber;
    private String partId;
    private String partName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public POLineItemDTO() {}

    public POLineItemDTO(int lineNumber, String partId, String partName,
                         int quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.lineNumber = lineNumber;
        this.partId = partId;
        this.partName = partName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public int getLineNumber() { return lineNumber; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
    public String getPartId() { return partId; }
    public void setPartId(String partId) { this.partId = partId; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
