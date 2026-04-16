package com.erp.manufacturing.rules;

import com.erp.exception.BusinessRuleException;
import com.erp.model.dto.WorkCenterDTO;

public class CapacityOverloadRule extends ProductionOrderRuleHandler {

    @Override
    protected void handle(ProductionOrderRuleContext ctx) {
        for (WorkCenterDTO wc : ctx.getWorkCenters()) {
            if (wc.getUtilizationPct() > 100.0) {
                throw BusinessRuleException.capacityOverload(wc.getWcId(), wc.getUtilizationPct());
            }
        }
    }
}
