package com.erp.model.dto;

/**
 * PATTERN: DTO (data-carrier) — one step in a routing sequence for a BOM/product.
 */
public class RoutingStepDTO {

    private String routingId;
    private String operationId;
    private int sequenceNumber;
    private String operationName;
    private String workCenterId;
    private double setupTime;
    private double runTime;

    public RoutingStepDTO() {}

    public RoutingStepDTO(String routingId, String operationId, int sequenceNumber,
                          String operationName, String workCenterId,
                          double setupTime, double runTime) {
        this.routingId = routingId;
        this.operationId = operationId;
        this.sequenceNumber = sequenceNumber;
        this.operationName = operationName;
        this.workCenterId = workCenterId;
        this.setupTime = setupTime;
        this.runTime = runTime;
    }

    public String getRoutingId() { return routingId; }
    public void setRoutingId(String routingId) { this.routingId = routingId; }
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }
    public String getWorkCenterId() { return workCenterId; }
    public void setWorkCenterId(String workCenterId) { this.workCenterId = workCenterId; }
    public double getSetupTime() { return setupTime; }
    public void setSetupTime(double setupTime) { this.setupTime = setupTime; }
    public double getRunTime() { return runTime; }
    public void setRunTime(double runTime) { this.runTime = runTime; }
}
