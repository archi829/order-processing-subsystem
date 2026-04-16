package com.designx.erp.external;

import com.designx.erp.model.Order;

/**
 * MEMBER 4 - Tracking & Integration Support
 * ==========================================
 * 
 * ⭐ STRUCTURAL PATTERN: Adapter Pattern
 * 
 * Purpose: Define standard interface for external modules to interact with
 * Order Processing subsystem without direct coupling.
 * 
 * This adapter serves as a bridge between Order Processing and other ERP subsystems:
 *   - Inventory/Fulfillment
 *   - Billing/Finance
 *   - Sales/CRM
 *   - Analytics
 * 
 * GRASP Principle: Low Coupling - adapters decouple order processing from details
 * SOLID Principle: D - Depend on abstractions, not concrete implementations
 *
 * Implementations adapt the generic interface to specific external systems.
 */
public interface IOrderProcessingAdapter {

    /**
     * Allocates inventory for an order.
     * Adapter implementation varies by inventory system.
     *
     * @param order the order requiring inventory allocation
     * @return true if allocation successful
     */
    boolean allocateInventory(Order order);

    /**
     * Releases previously allocated inventory.
     * Called when order is cancelled or modified.
     *
     * @param order the order releasing inventory
     * @return true if release successful
     */
    boolean releaseInventory(Order order);

    /**
     * Confirms order for shipment/dispatch.
     *
     * @param order the order to dispatch
     * @return true if confirmation successful
     */
    boolean confirmDispatch(Order order);

    /**
     * Generates invoice for order.
     *
     * @param order the order to invoice
     */
    void generateInvoice(Order order);

    /**
     * Processes payment for order.
     *
     * @param order the order to pay
     * @return true if payment initiated successfully
     */
    boolean processPayment(Order order);
}
