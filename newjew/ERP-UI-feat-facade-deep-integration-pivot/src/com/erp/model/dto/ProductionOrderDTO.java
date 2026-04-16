package com.erp.model.dto;

import java.time.LocalDate;

/**
 * PATTERN: DTO (data-carrier) — a shop-floor production order.
 */
public class ProductionOrderDTO {

    public static final String PENDING     = "PENDING";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED   = "COMPLETED";
    public static final String CANCELLED   = "CANCELLED";

    public static final String PRI_LOW    = "LOW";
    public static final String PRI_MEDIUM = "MEDIUM";
    public static final String PRI_HIGH   = "HIGH";
    public static final String PRI_URGENT = "URGENT";

    private String orderId;
    private LocalDate orderDate;
    private String productId;
    private String productName;
    private String bomId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private String status;
    private String priority;
    private int qtyPlanned;
    private int qtyProduced;
    private int scrapQty;

    public ProductionOrderDTO() {}

    public ProductionOrderDTO(String orderId, String productId, String productName, String bomId,
                              LocalDate plannedStartDate, LocalDate plannedEndDate,
                              String status, String priority, int qtyPlanned) {
        this(orderId, LocalDate.now(), productId, productName, bomId,
            plannedStartDate, plannedEndDate, status, priority, qtyPlanned);
        }

        public ProductionOrderDTO(String orderId, LocalDate orderDate, String productId, String productName, String bomId,
                      LocalDate plannedStartDate, LocalDate plannedEndDate,
                      String status, String priority, int qtyPlanned) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.productId = productId;
        this.productName = productName;
        this.bomId = bomId;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.status = status;
        this.priority = priority;
        this.qtyPlanned = qtyPlanned;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBomId() { return bomId; }
    public void setBomId(String bomId) { this.bomId = bomId; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public void setPlannedStartDate(LocalDate d) { this.plannedStartDate = d; }
    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public void setPlannedEndDate(LocalDate d) { this.plannedEndDate = d; }
    public LocalDate getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDate d) { this.actualStartDate = d; }
    public LocalDate getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDate d) { this.actualEndDate = d; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public int getQtyPlanned() { return qtyPlanned; }
    public void setQtyPlanned(int qtyPlanned) { this.qtyPlanned = qtyPlanned; }
    public int getQtyProduced() { return qtyProduced; }
    public void setQtyProduced(int qtyProduced) { this.qtyProduced = qtyProduced; }
    public int getScrapQty() { return scrapQty; }
    public void setScrapQty(int scrapQty) { this.scrapQty = scrapQty; }
}
