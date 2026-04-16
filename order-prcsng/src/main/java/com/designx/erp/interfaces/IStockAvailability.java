package com.designx.erp.interfaces;

/**
 * Interface for the Inventory Module.
 * SOLID (DIP): Validation Engine and Fulfillment Orchestrator depend on this abstraction.
 * GRASP (Low Coupling): Decouples Order Processing from Inventory implementation.
 *
 * External System: Inventory Module (listed in ERP Software features as feature #16 - Supply chain)
 */
public interface IStockAvailability {

    /**
     * Returns the available stock count for a given vehicle model and variant.
     */
    int getStockLevel(String vehicleModel, String vehicleVariant);

    /**
     * Reserves/allocates inventory for a confirmed order.
     * Returns the allocation unit ID on success, or null on failure.
     */
    int allocateInventory(String vehicleModel, String vehicleVariant, String orderId);

    /**
     * Releases a previously held inventory allocation (e.g., if order is rejected).
     */
    void releaseInventory(String orderId);

    /**
     * Confirms final dispatch of the vehicle (reduces actual stock).
     */
    void confirmDispatch(String orderId);
}
