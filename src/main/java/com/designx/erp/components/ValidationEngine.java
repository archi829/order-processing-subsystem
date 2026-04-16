package com.designx.erp.components;

import com.designx.erp.interfaces.IOrderValidator;
import com.designx.erp.interfaces.IStockAvailability;
import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;

/**
 * GRASP (Information Expert): Knows all rules needed to validate an order.
 * SOLID (SRP): Only validates — does not approve, fulfill, or bill.
 * SOLID (OCP): New validation rules can be added without modifying existing ones.
 * SOLID (DIP): Depends on IStockAvailability abstraction, not a concrete class.
 *
 * Reads: Order ID, Vehicle Configuration, Stock Availability, Pricing Details
 * Writes: Validation Status (sets order status to VALIDATED or REJECTED)
 */
public class ValidationEngine implements IOrderValidator {

    private static final double MIN_ORDER_VALUE = 100_000.0; // Minimum vehicle price (INR)

    private final IStockAvailability inventoryModule;

    public ValidationEngine(IStockAvailability inventoryModule) {
        this.inventoryModule = inventoryModule;
    }

    /**
     * Validates the order against required fields, pricing rules, and stock availability.
     *
     * @param order the captured order to validate
     * @return true if order passes all validations; false otherwise
     */
    @Override
    public boolean validate(Order order) {

        // Rule 1: Required fields check
        if (isNullOrBlank(order.getCustomerId())
                || isNullOrBlank(order.getVehicleModel())
                || isNullOrBlank(order.getVehicleVariant())
                || isNullOrBlank(order.getVehicleColor())) {
            return reject(order, "Missing required fields: customer ID, vehicle model, variant, or color.");
        }

        // Rule 2: Pricing threshold
        if (order.getOrderValue() < MIN_ORDER_VALUE) {
            return reject(order, "Order value ₹" + order.getOrderValue()
                    + " is below the minimum threshold of ₹" + MIN_ORDER_VALUE);
        }

        // Rule 3: Stock availability check via Inventory Module
        int stock = inventoryModule.getStockLevel(order.getVehicleModel(), order.getVehicleVariant());
        if (stock <= 0) {
            return reject(order, "Vehicle not in stock: "
                    + order.getVehicleModel() + " " + order.getVehicleVariant());
        }

        System.out.println("[ValidationEngine] Order validated: " + order.getOrderId());
        return true;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private boolean reject(Order order, String reason) {
        order.setRejectionReason(reason);
        System.out.println("[ValidationEngine] Validation FAILED — " + reason);
        return false;
    }

    private boolean isNullOrBlank(String value) {
        return value == null || value.isBlank();
    }
}
