package com.erp.manufacturing.rules;

import com.erp.exception.ValidationException;
import com.erp.model.dto.ProductionOrderDTO;

public class RequiredOrderDataRule extends ProductionOrderRuleHandler {

    @Override
    protected void handle(ProductionOrderRuleContext ctx) {
        ProductionOrderDTO o = ctx.getOrder();
        if (o.getProductId() == null || o.getProductId().trim().isEmpty()) {
            throw ValidationException.requiredField("product_id", null);
        }
        if (o.getBomId() == null || o.getBomId().trim().isEmpty()) {
            throw ValidationException.requiredField("bom_id", null);
        }
        if (o.getQtyPlanned() <= 0) {
            throw ValidationException.invalidQuantityInput(null, String.valueOf(o.getQtyPlanned()));
        }
    }
}
