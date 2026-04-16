package com.erp.manufacturing.rules;

import com.erp.manufacturing.integration.FinancePort;
import com.erp.manufacturing.integration.HRPort;
import com.erp.manufacturing.integration.InventoryPort;
import com.erp.manufacturing.integration.ProcurementPort;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.model.dto.WorkCenterDTO;

import java.util.List;

public class ProductionOrderRuleContext {

    private final ProductionOrderDTO order;
    private final BomDTO bom;
    private final List<RoutingStepDTO> routingSteps;
    private final List<WorkCenterDTO> workCenters;
    private final InventoryPort inventoryPort;
    private final ProcurementPort procurementPort;
    private final HRPort hrPort;
    private final FinancePort financePort;

    public ProductionOrderRuleContext(ProductionOrderDTO order,
                                      BomDTO bom,
                                      List<RoutingStepDTO> routingSteps,
                                      List<WorkCenterDTO> workCenters,
                                      InventoryPort inventoryPort,
                                      ProcurementPort procurementPort,
                                      HRPort hrPort,
                                      FinancePort financePort) {
        this.order = order;
        this.bom = bom;
        this.routingSteps = routingSteps;
        this.workCenters = workCenters;
        this.inventoryPort = inventoryPort;
        this.procurementPort = procurementPort;
        this.hrPort = hrPort;
        this.financePort = financePort;
    }

    public ProductionOrderDTO getOrder() { return order; }
    public BomDTO getBom() { return bom; }
    public List<RoutingStepDTO> getRoutingSteps() { return routingSteps; }
    public List<WorkCenterDTO> getWorkCenters() { return workCenters; }
    public InventoryPort getInventoryPort() { return inventoryPort; }
    public ProcurementPort getProcurementPort() { return procurementPort; }
    public HRPort getHrPort() { return hrPort; }
    public FinancePort getFinancePort() { return financePort; }
}
