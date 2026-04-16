package com.designx.erp.components;

import com.designx.erp.interfaces.IApprovalWorkflow;
import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;

/**
 * GRASP (Controller): Routes orders to the correct approval path based on value.
 * GRASP (Information Expert): Knows approval thresholds and credit limits.
 * SOLID (SRP): Only handles approval / rejection logic.
 * SOLID (OCP): Thresholds can be externalised to config without changing this class.
 *
 * Reads:  Order ID, Order Value, Customer Credit Info
 * Writes: Approval Status, Rejection Reason
 */
public class ApprovalWorkflow implements IApprovalWorkflow {

    /** Orders above this value require senior manager approval (high-value cars / fleet). */
    private static final double HIGH_VALUE_THRESHOLD = 3_000_000.0;

    /** Absolute credit ceiling — orders above this are auto-rejected. */
    private static final double MAX_CREDIT_LIMIT = 5_000_000.0;

    /**
     * Processes the approval decision for a validated order.
     *
     * @param order a VALIDATED order
     * @return true if approved; false if rejected
     */
    @Override
    public boolean process(Order order) {

        // Hard ceiling — always reject
        if (order.getOrderValue() > MAX_CREDIT_LIMIT) {
            order.setRejectionReason("Order value ₹" + order.getOrderValue()
                    + " exceeds maximum credit limit of ₹" + MAX_CREDIT_LIMIT);
            System.out.println("[ApprovalWorkflow] REJECTED (exceeds credit limit): " + order.getOrderId());
            return false;
        }

        // High-value path — log for senior approval, still approve in this flow
        if (order.getOrderValue() > HIGH_VALUE_THRESHOLD) {
            System.out.println("[ApprovalWorkflow] HIGH-VALUE order flagged for senior review: "
                    + order.getOrderId() + " | ₹" + order.getOrderValue());
        }

        System.out.println("[ApprovalWorkflow] Order APPROVED: " + order.getOrderId());
        return true;
    }
}
