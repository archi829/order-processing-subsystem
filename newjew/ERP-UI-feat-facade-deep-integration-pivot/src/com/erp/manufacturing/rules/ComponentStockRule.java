package com.erp.manufacturing.rules;

import com.erp.exception.BusinessRuleException;
import com.erp.model.dto.BomItemDTO;

public class ComponentStockRule extends ProductionOrderRuleHandler {

    @Override
    protected void handle(ProductionOrderRuleContext ctx) {
        for (BomItemDTO item : ctx.getBom().getItems()) {
            double required = item.getQuantity() * ctx.getOrder().getQtyPlanned();
            double available = ctx.getInventoryPort().availableQuantity(item.getMaterialItemId());
            if (available < required) {
                ctx.getProcurementPort().raisePurchaseRequest(item.getMaterialItemId(), required, available);
                throw BusinessRuleException.componentStockInsufficient(
                        item.getMaterialItemId(),
                        (int) Math.ceil(required),
                        (int) Math.floor(available));
            }
        }
    }
}
