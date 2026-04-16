package com.erp.integration.endpoints;

/**
 * Endpoint namespace for the Order Processing subsystem.
 *
 * SOLID: ISP — per-module endpoint namespace. OrderController depends on this
 *              interface only, not on a global "all endpoints" contract.
 */
public interface OrdersEndpoints {
    String ORDERS_LIST     = "orders/list";
    String ORDERS_CREATE   = "orders/create";
    String ORDERS_APPROVE  = "orders/approve";
    String ORDERS_REJECT   = "orders/reject";
    String ORDERS_REVISION = "orders/revision";
    String ORDERS_SHIP     = "orders/ship";
    String ORDERS_PAY      = "orders/pay";
    String ORDERS_CANCEL   = "orders/cancel";
    String ORDERS_STATS    = "orders/stats";
}
