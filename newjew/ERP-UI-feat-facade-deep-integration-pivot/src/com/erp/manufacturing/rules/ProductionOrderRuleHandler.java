package com.erp.manufacturing.rules;

/**
 * Chain-of-responsibility handler for production-order validation.
 */
public abstract class ProductionOrderRuleHandler {

    private ProductionOrderRuleHandler next;

    public ProductionOrderRuleHandler setNext(ProductionOrderRuleHandler next) {
        this.next = next;
        return next;
    }

    public final void check(ProductionOrderRuleContext ctx) {
        handle(ctx);
        if (next != null) {
            next.check(ctx);
        }
    }

    protected abstract void handle(ProductionOrderRuleContext ctx);
}
