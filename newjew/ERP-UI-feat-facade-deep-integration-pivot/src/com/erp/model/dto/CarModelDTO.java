package com.erp.model.dto;

import java.time.LocalDateTime;

/**
 * PATTERN: DTO (data-carrier) — an in-assembly vehicle rolling through the line.
 */
public class CarModelDTO {

    public static final String PENDING       = "PENDING";
    public static final String IN_ASSEMBLY   = "IN_ASSEMBLY";
    public static final String IN_QUALITY    = "IN_QUALITY";
    public static final String READY         = "READY";
    public static final String SHIPPED       = "SHIPPED";

    private String vin;
    private String modelName;
    private String chassisType;
    private String buildStatus;
    private String assemblyLineId;
    private LocalDateTime startedAt;

    public CarModelDTO() {}

    public CarModelDTO(String vin, String modelName, String chassisType,
                       String buildStatus, String assemblyLineId, LocalDateTime startedAt) {
        this.vin = vin;
        this.modelName = modelName;
        this.chassisType = chassisType;
        this.buildStatus = buildStatus;
        this.assemblyLineId = assemblyLineId;
        this.startedAt = startedAt;
    }

    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getChassisType() { return chassisType; }
    public void setChassisType(String chassisType) { this.chassisType = chassisType; }
    public String getBuildStatus() { return buildStatus; }
    public void setBuildStatus(String buildStatus) { this.buildStatus = buildStatus; }
    public String getAssemblyLineId() { return assemblyLineId; }
    public void setAssemblyLineId(String assemblyLineId) { this.assemblyLineId = assemblyLineId; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
