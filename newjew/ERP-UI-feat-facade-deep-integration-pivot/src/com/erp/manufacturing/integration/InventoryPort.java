package com.erp.manufacturing.integration;

public interface InventoryPort {
    double availableQuantity(String materialItemId);
    void reserveQuantity(String materialItemId, double quantity);
}
