package com.erp.integration.endpoints;

import com.erp.model.dto.OrderDTO;
import java.util.List;
import java.util.Map;

/**
 * Endpoint interface for the Order Processing subsystem.
 *
 * SOLID: ISP — per-module endpoint namespace. OrderController depends on this
 * interface only, executing strongly-typed method calls instead 
 * of passing loosely-typed String constants.
 */
public interface OrdersEndpoints {

    // Replaces ORDERS_LIST = "orders/list"
    List<OrderDTO> getOrders() throws Exception;

    // Replaces ORDERS_CREATE = "orders/create"
    boolean createOrder(OrderDTO order) throws Exception;

    // Replaces ORDERS_APPROVE = "orders/approve"
    boolean approveOrder(String orderId) throws Exception;

    // Replaces ORDERS_REJECT = "orders/reject"
    boolean rejectOrder(String orderId, String rejectionReason) throws Exception;

    // Replaces ORDERS_REVISION = "orders/revision"
    boolean reviseOrder(String orderId, OrderDTO updatedOrder) throws Exception;

    // Replaces ORDERS_SHIP = "orders/ship"
    boolean shipOrder(String orderId) throws Exception;

    // Replaces ORDERS_PAY = "orders/pay"
    boolean payOrder(String orderId, double amount) throws Exception;

    // Replaces ORDERS_CANCEL = "orders/cancel"
    boolean cancelOrder(String orderId, String cancellationReason) throws Exception;

    // Replaces ORDERS_STATS = "orders/stats"
    Map<String, Object> getOrderStats() throws Exception;
}