package com.designx.erp.interfaces;

import com.designx.erp.model.Order;

/**
 * SOLID (ISP): Separate interface for approval routing responsibility.
 */
public interface IApprovalWorkflow {
    /**
     * Evaluates an order and either approves or rejects it.
     * @return true if approved.
     */
    boolean process(Order order);
}
