package com.designx.erp.external;

import com.designx.erp.interfaces.IStockAvailability;

import java.util.HashMap;
import java.util.Map;

/**
 * Stub adapter for the Inventory Module (external system).
 *
 * SOLID (DIP + LSP): Implements IStockAvailability; can be replaced with a real
 *                    inventory system client without changing any internal component.
 *
 * External System reference: ERP Software feature #16 — Supply chain and purchasing.
 * Provides: Stock Availability, Inventory Updates
 */
public class InventoryModuleAdapter implements IStockAvailability {

    // Key format: "VehicleModel|VehicleVariant"
    private final Map<String, Integer> stockLevels = new HashMap<>(Map.of(
            "Tata Nexon|XZ+",          12,
            "Tata Harrier|XZA",         5,
            "Tata Safari|Adventure",    3,
            "Tata Altroz|XZ",           8
    ));

    // Track allocated stock by order (for potential re-allocation on modification/cancellation)
    private final Map<String, String> orderToStockKey = new HashMap<>();

    @Override
    public int getStockLevel(String vehicleModel, String vehicleVariant) {
        return stockLevels.getOrDefault(key(vehicleModel, vehicleVariant), 0);
    }

    @Override
    public int allocateInventory(String vehicleModel, String vehicleVariant, String orderId) {
        String k = key(vehicleModel, vehicleVariant);
        int current = stockLevels.getOrDefault(k, 0);
        if (current <= 0) {
            throw new IllegalStateException(
                    "No stock available for allocation: " + vehicleModel + " " + vehicleVariant);
        }
        stockLevels.put(k, current - 1);
        orderToStockKey.put(orderId, k);  // Track which stock key this order allocated
        System.out.println("[InventoryModule] Allocated 1 unit of " + k
                + " for order " + orderId + ". Remaining stock: " + (current - 1));
        return current; // simplified allocation unit ID
    }

    @Override
    public void releaseInventory(String orderId) {
        // In a real system, look up reservation by orderId and release it
        System.out.println("[InventoryModule] Inventory reservation released for order: " + orderId);
    }

    @Override
    public void confirmDispatch(String orderId) {
        System.out.println("[InventoryModule] Dispatch confirmed and stock decremented for order: " + orderId);
    }

    /**
     * Releases allocated stock back to inventory (for order cancellation or modification).
     * @param orderId the order ID that previously allocated stock
     */
    public void releaseStock(String orderId) {
        String stockKey = orderToStockKey.remove(orderId);
        if (stockKey != null) {
            int current = stockLevels.getOrDefault(stockKey, 0);
            stockLevels.put(stockKey, current + 1);
            System.out.println("[InventoryModule] Released 1 unit of " + stockKey
                    + " from order " + orderId + ". Restored stock: " + (current + 1));
        } else {
            System.out.println("[InventoryModule] No stock allocation found for order: " + orderId);
        }
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private String key(String model, String variant) {
        return model + "|" + variant;
    }
}
