package com.erp.manufacturing.rules;

import com.erp.exception.BusinessRuleException;
import com.erp.model.dto.RoutingStepDTO;

public class WorkOrderAssignmentRule extends ProductionOrderRuleHandler {

    @Override
    protected void handle(ProductionOrderRuleContext ctx) {
        for (RoutingStepDTO step : ctx.getRoutingSteps()) {
            if (!ctx.getHrPort().hasAssignableOperator(step.getWorkCenterId())) {
                throw BusinessRuleException.workOrderAssignmentFailed(step.getOperationName());
            }
        }
    }
}
