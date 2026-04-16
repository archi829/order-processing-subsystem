package com.erp.model.dto;

import java.time.LocalDate;

/**
 * PATTERN: DTO (data-carrier) — Inbound shipment from a supplier.
 */
public class ShipmentDTO {

    public static final String PENDING    = "PENDING";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED  = "DELIVERED";
    public static final String DELAYED    = "DELAYED";

    private String shipmentId;
    private String poId;
    private String trackingNumber;
    private String carrierName;
    private LocalDate estimatedArrival;
    private LocalDate actualArrival;
    private String status;

    public ShipmentDTO() {}

    public ShipmentDTO(String shipmentId, String poId, String trackingNumber, String carrierName,
                       LocalDate estimatedArrival, LocalDate actualArrival, String status) {
        this.shipmentId = shipmentId;
        this.poId = poId;
        this.trackingNumber = trackingNumber;
        this.carrierName = carrierName;
        this.estimatedArrival = estimatedArrival;
        this.actualArrival = actualArrival;
        this.status = status;
    }

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public LocalDate getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(LocalDate estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public LocalDate getActualArrival() { return actualArrival; }
    public void setActualArrival(LocalDate actualArrival) { this.actualArrival = actualArrival; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
