package com.designx.erp.components;

import com.designx.erp.interfaces.IStockAvailability;
import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;
import com.designx.erp.model.Shipment;

/**
 * GRASP (Creator): Creates Shipment objects — has all the needed data.
 * GRASP (Low Coupling): Only depends on IStockAvailability abstraction.
 * SOLID (SRP): Coordinates dispatch only; does not bill, validate, or approve.
 * SOLID (DIP): Depends on IStockAvailability interface, not a concrete class.
 *
 * Reads:  Order ID, Vehicle Details, Inventory Allocation
 * Writes: Shipment ID, Dispatch Date, Delivery Status
 */
public class FulfillmentOrchestrator {

    private final IStockAvailability inventoryModule;

    public FulfillmentOrchestrator(IStockAvailability inventoryModule) {
        this.inventoryModule = inventoryModule;
    }

    /**
     * Allocates inventory and creates a shipment for an approved order.
     *
     * @param order an APPROVED order
     * @return a Shipment in DISPATCHED status
     * @throws IllegalStateException if order is not in APPROVED status
     */
    public Shipment fulfill(Order order) {
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new IllegalStateException(
                    "Cannot fulfill order that is not APPROVED. Current status: " + order.getStatus());
        }

        String vehicleDetails = order.getVehicleModel() + " "
                + order.getVehicleVariant() + " ["
                + order.getVehicleColor() + "]"
                + (order.getCustomFeaturesOrAddOns() != null
                   && !order.getCustomFeaturesOrAddOns().isBlank()
                        ? " | Add-ons: " + order.getCustomFeaturesOrAddOns() : "");

        // Allocate from Inventory Module
        int allocationId = inventoryModule.allocateInventory(
                order.getVehicleModel(), order.getVehicleVariant(), order.getOrderId());

        Shipment shipment = new Shipment(order.getOrderId(), vehicleDetails, allocationId);
        shipment.setDeliveryStatus(Shipment.DeliveryStatus.DISPATCHED);

        // Confirm dispatch back to Inventory Module
        inventoryModule.confirmDispatch(order.getOrderId());

        System.out.println("[FulfillmentOrchestrator] Shipment created: " + shipment);
        return shipment;
    }
}
