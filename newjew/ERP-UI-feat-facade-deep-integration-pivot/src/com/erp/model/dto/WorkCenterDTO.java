package com.erp.model.dto;

/**
 * PATTERN: DTO (data-carrier) — a manufacturing work center / shop-floor station.
 */
public class WorkCenterDTO {

    public static final String TYPE_ASSEMBLY  = "ASSEMBLY";
    public static final String TYPE_TESTING   = "TESTING";
    public static final String TYPE_PACKAGING = "PACKAGING";
    public static final String TYPE_WELDING   = "WELDING";
    public static final String TYPE_PAINT     = "PAINT";

    private String wcId;
    private String wcName;
    private String wcType;
    private double capacityHours;
    private double utilizationPct;
    private String location;

    public WorkCenterDTO() {}

    public WorkCenterDTO(String wcId, String wcName, String wcType,
                         double capacityHours, double utilizationPct, String location) {
        this.wcId = wcId;
        this.wcName = wcName;
        this.wcType = wcType;
        this.capacityHours = capacityHours;
        this.utilizationPct = utilizationPct;
        this.location = location;
    }

    public String getWcId() { return wcId; }
    public void setWcId(String wcId) { this.wcId = wcId; }
    public String getWcName() { return wcName; }
    public void setWcName(String wcName) { this.wcName = wcName; }
    public String getWcType() { return wcType; }
    public void setWcType(String wcType) { this.wcType = wcType; }
    public double getCapacityHours() { return capacityHours; }
    public void setCapacityHours(double capacityHours) { this.capacityHours = capacityHours; }
    public double getUtilizationPct() { return utilizationPct; }
    public void setUtilizationPct(double utilizationPct) { this.utilizationPct = utilizationPct; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
