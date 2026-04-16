package com.erp.manufacturing.command;

import com.erp.manufacturing.integration.InventoryPort;
import com.erp.manufacturing.repository.ManufacturingRepository;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.BomItemDTO;
import com.erp.model.dto.ProductionOrderDTO;

import java.time.LocalDate;

class CreateProductionOrderCommand implements ManufacturingCommand<ProductionOrderDTO> {

    private final ManufacturingRepository repository;
    private final InventoryPort inventoryPort;
    private final ProductionOrderDTO order;
    private final BomDTO bom;

    CreateProductionOrderCommand(ManufacturingRepository repository,
                                 InventoryPort inventoryPort,
                                 ProductionOrderDTO order,
                                 BomDTO bom) {
        this.repository = repository;
        this.inventoryPort = inventoryPort;
        this.order = order;
        this.bom = bom;
    }

    @Override
    public ProductionOrderDTO execute() {
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            order.setOrderId(repository.nextId("mfg_production_order", "order_id", "PO-"));
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        if (order.getStatus() == null || order.getStatus().trim().isEmpty()) {
            order.setStatus(ProductionOrderDTO.PENDING);
        }

        ProductionOrderDTO created = repository.insertProductionOrder(order);
        for (BomItemDTO item : bom.getItems()) {
            double required = item.getQuantity() * created.getQtyPlanned();
            inventoryPort.reserveQuantity(item.getMaterialItemId(), required);
        }
        return created;
    }
}
