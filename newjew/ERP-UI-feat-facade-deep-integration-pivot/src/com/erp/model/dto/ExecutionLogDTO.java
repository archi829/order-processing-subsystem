package com.erp.model.dto;

import java.time.LocalDateTime;

/**
 * DTO representing a manufacturing execution log entry.
 * SQL-mapped fields:
 * log_id, start_time, end_time, operator_id, qty_produced, scrap_qty.
 */
public class ExecutionLogDTO {

    private String logId;
    private String orderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String operatorId;
    private double qtyProduced;
    private double scrapQty;
    private String machineId;
    private String note;

    public ExecutionLogDTO() {}

    public ExecutionLogDTO(String logId,
                           String orderId,
                           LocalDateTime startTime,
                           LocalDateTime endTime,
                           String operatorId,
                           double qtyProduced,
                           double scrapQty,
                           String machineId,
                           String note) {
        this.logId = logId;
        this.orderId = orderId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.operatorId = operatorId;
        this.qtyProduced = qtyProduced;
        this.scrapQty = scrapQty;
        this.machineId = machineId;
        this.note = note;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }

    public double getQtyProduced() { return qtyProduced; }
    public void setQtyProduced(double qtyProduced) { this.qtyProduced = qtyProduced; }

    public double getScrapQty() { return scrapQty; }
    public void setScrapQty(double scrapQty) { this.scrapQty = scrapQty; }

    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
