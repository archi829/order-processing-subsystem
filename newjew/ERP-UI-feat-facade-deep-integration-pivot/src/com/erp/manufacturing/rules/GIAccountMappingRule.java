package com.erp.manufacturing.rules;

import com.erp.exception.BusinessRuleException;
import com.erp.model.dto.RoutingStepDTO;

public class GIAccountMappingRule extends ProductionOrderRuleHandler {

    @Override
    protected void handle(ProductionOrderRuleContext ctx) {
        for (RoutingStepDTO step : ctx.getRoutingSteps()) {
            String account = ctx.getFinancePort().findGLAccount(step.getWorkCenterId());
            if (account == null || account.trim().isEmpty()) {
                throw BusinessRuleException.giAccountMapping(step.getWorkCenterId());
            }
        }
    }
}
