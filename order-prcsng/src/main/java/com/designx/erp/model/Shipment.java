package com.designx.erp.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a vehicle shipment/delivery record.
 */
public class Shipment {

    public enum DeliveryStatus { PENDING, DISPATCHED, IN_TRANSIT, DELIVERED, FAILED }

    private final String shipmentId;
    private final String orderId;
    private final String vehicleDetails;
    private final int inventoryAllocation;
    private final LocalDate dispatchDate;
    private DeliveryStatus deliveryStatus;

    public Shipment(String orderId, String vehicleDetails, int inventoryAllocation) {
        this.shipmentId = "SHP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.orderId = orderId;
        this.vehicleDetails = vehicleDetails;
        this.inventoryAllocation = inventoryAllocation;
        this.dispatchDate = LocalDate.now().plusDays(3); // estimated dispatch
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    public String getShipmentId()           { return shipmentId; }
    public String getOrderId()              { return orderId; }
    public String getVehicleDetails()       { return vehicleDetails; }
    public int getInventoryAllocation()     { return inventoryAllocation; }
    public LocalDate getDispatchDate()      { return dispatchDate; }
    public DeliveryStatus getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(DeliveryStatus s) { this.deliveryStatus = s; }

    @Override
    public String toString() {
        return String.format("Shipment[id=%s, order=%s, vehicle=%s, status=%s]",
                shipmentId, orderId, vehicleDetails, deliveryStatus);
    }
}
