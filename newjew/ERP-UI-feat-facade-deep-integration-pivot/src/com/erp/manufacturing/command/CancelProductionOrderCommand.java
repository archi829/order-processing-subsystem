package com.erp.manufacturing.command;

import com.erp.exception.IntegrationException;
import com.erp.manufacturing.repository.ManufacturingRepository;
import com.erp.model.dto.ProductionOrderDTO;

import java.time.LocalDate;

class CancelProductionOrderCommand implements ManufacturingCommand<ProductionOrderDTO> {

    private final ManufacturingRepository repository;
    private final String orderId;

    CancelProductionOrderCommand(ManufacturingRepository repository, String orderId) {
        this.repository = repository;
        this.orderId = orderId;
    }

    @Override
    public ProductionOrderDTO execute() {
        ProductionOrderDTO found = repository.findProductionOrder(orderId);
        if (found == null) {
            throw IntegrationException.sendFailed("mfg/production-orders/cancel", "Production order not found: " + orderId);
        }
        return repository.updateProductionOrderStatus(orderId, ProductionOrderDTO.CANCELLED, LocalDate.now());
    }
}
