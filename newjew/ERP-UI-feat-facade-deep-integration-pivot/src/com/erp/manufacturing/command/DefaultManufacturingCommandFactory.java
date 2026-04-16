package com.erp.manufacturing.command;

import com.erp.manufacturing.integration.InventoryPort;
import com.erp.manufacturing.repository.ManufacturingRepository;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;

/**
 * Factory for manufacturing command objects.
 *
 * PATTERN: Factory (Creational)
 */
public class DefaultManufacturingCommandFactory implements ManufacturingCommandFactory {

    private final ManufacturingRepository repository;
    private final InventoryPort inventoryPort;

    public DefaultManufacturingCommandFactory(ManufacturingRepository repository, InventoryPort inventoryPort) {
        this.repository = repository;
        this.inventoryPort = inventoryPort;
    }

    @Override
    public ManufacturingCommand<ProductionOrderDTO> createProductionOrderCommand(ProductionOrderDTO order, BomDTO bom) {
        return new CreateProductionOrderCommand(repository, inventoryPort, order, bom);
    }

    @Override
    public ManufacturingCommand<ProductionOrderDTO> cancelProductionOrderCommand(String orderId) {
        return new CancelProductionOrderCommand(repository, orderId);
    }

    @Override
    public ManufacturingCommand<String> recordExecutionCommand(ExecutionLogDTO log) {
        return new RecordExecutionLogCommand(repository, log);
    }

    @Override
    public ManufacturingCommand<QCCheckDTO> submitQcCommand(QCCheckDTO qcCheck) {
        return new SubmitQcCommand(repository, qcCheck);
    }
}
