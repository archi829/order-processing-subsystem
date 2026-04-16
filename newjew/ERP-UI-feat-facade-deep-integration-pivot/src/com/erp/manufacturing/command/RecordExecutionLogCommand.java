package com.erp.manufacturing.command;

import com.erp.manufacturing.repository.ManufacturingRepository;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.ProductionOrderDTO;

class RecordExecutionLogCommand implements ManufacturingCommand<String> {

    private final ManufacturingRepository repository;
    private final ExecutionLogDTO log;

    RecordExecutionLogCommand(ManufacturingRepository repository, ExecutionLogDTO log) {
        this.repository = repository;
        this.log = log;
    }

    @Override
    public String execute() {
        repository.insertExecutionLog(log);
        repository.applyExecutionToOrder(log.getOrderId(), log.getQtyProduced(), log.getScrapQty(),
                log.getStartTime(), log.getEndTime());

        ProductionOrderDTO order = repository.findProductionOrder(log.getOrderId());
        if (order != null && order.getQtyProduced() >= order.getQtyPlanned()) {
            repository.updateProductionOrderStatus(order.getOrderId(), ProductionOrderDTO.COMPLETED,
                    log.getEndTime() == null ? null : log.getEndTime().toLocalDate());
        }
        return "ACK-" + log.getLogId();
    }
}
