package com.erp.manufacturing.integration;

import com.erp.manufacturing.repository.ManufacturingRepository;

/**
 * Adapter between the manufacturing module and cross-subsystem ports.
 *
 * PATTERN: Adapter (Structural)
 */
public class SqlSubsystemAdapter implements InventoryPort,
        FinancePort,
        HRPort,
        ProcurementPort,
        SalesPort,
        MaintenancePort {

    private final ManufacturingRepository repository;

    public SqlSubsystemAdapter(ManufacturingRepository repository) {
        this.repository = repository;
    }

    @Override
    public double availableQuantity(String materialItemId) {
        return repository.availableStock(materialItemId);
    }

    @Override
    public void reserveQuantity(String materialItemId, double quantity) {
        repository.reserveStock(materialItemId, quantity);
    }

    @Override
    public String findGLAccount(String costCenterCode) {
        return repository.glAccount(costCenterCode);
    }

    @Override
    public boolean hasAssignableOperator(String workCenterId) {
        return repository.hasAvailableOperator(workCenterId);
    }

    @Override
    public boolean isCertified(String operatorId, String workCenterId) {
        return repository.isOperatorCertified(operatorId, workCenterId);
    }

    @Override
    public void raisePurchaseRequest(String materialItemId, double requiredQty, double availableQty) {
        System.out.println("[PROCUREMENT] Purchase request generated for " + materialItemId
                + " required=" + requiredQty + " available=" + availableQty);
    }

    @Override
    public void notifyProductionDelay(String orderId) {
        System.out.println("[SALES] Production delay notification for order " + orderId);
    }

    @Override
    public boolean isMachineOnline(String machineId) {
        return repository.isMachineOnline(machineId);
    }
}
