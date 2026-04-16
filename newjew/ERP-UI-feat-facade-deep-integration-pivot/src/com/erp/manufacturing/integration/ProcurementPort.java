package com.erp.manufacturing.integration;

public interface ProcurementPort {
    void raisePurchaseRequest(String materialItemId, double requiredQty, double availableQty);
}
