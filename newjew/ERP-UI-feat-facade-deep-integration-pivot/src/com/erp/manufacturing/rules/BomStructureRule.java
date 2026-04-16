package com.erp.manufacturing.rules;

import com.erp.exception.BusinessRuleException;
import com.erp.model.dto.BomItemDTO;

import java.util.HashSet;
import java.util.Set;

public class BomStructureRule extends ProductionOrderRuleHandler {

    @Override
    protected void handle(ProductionOrderRuleContext ctx) {
        if (ctx.getBom() == null || ctx.getBom().getItems() == null || ctx.getBom().getItems().isEmpty()) {
            String bomId = ctx.getOrder().getBomId();
            throw BusinessRuleException.invalidBomStructure(bomId, "missing required BOM components");
        }

        Set<String> seen = new HashSet<>();
        for (BomItemDTO item : ctx.getBom().getItems()) {
            String mid = item.getMaterialItemId();
            if (mid == null || mid.trim().isEmpty()) {
                throw BusinessRuleException.invalidBomStructure(ctx.getBom().getBomId(), "material_item_id cannot be blank");
            }
            if (!seen.add(mid)) {
                throw BusinessRuleException.invalidBomStructure(ctx.getBom().getBomId(),
                        "duplicate component detected: " + mid);
            }
        }
    }
}
