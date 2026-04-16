package com.erp.manufacturing.command;

import com.erp.model.dto.BomDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;

public interface ManufacturingCommandFactory {

    ManufacturingCommand<ProductionOrderDTO> createProductionOrderCommand(ProductionOrderDTO order, BomDTO bom);

    ManufacturingCommand<ProductionOrderDTO> cancelProductionOrderCommand(String orderId);

    ManufacturingCommand<String> recordExecutionCommand(ExecutionLogDTO log);

    ManufacturingCommand<QCCheckDTO> submitQcCommand(QCCheckDTO qcCheck);
}
