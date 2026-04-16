package com.erp.integration;

import com.erp.integration.endpoints.OrdersEndpoints;
import com.erp.model.dto.OrderDTO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fallback implementation of {@link OrdersEndpoints} that delegates directly
 * to {@link MockUIService} using the existing "orders/*" endpoint strings.
 *
 * This is used by {@link ServiceLocator#getOrdersEndpoints()} when
 * {@code OrdersEndpointsImpl} (from the order-processing jar) is not on
 * the classpath.
 */
@SuppressWarnings("unchecked")
class MockOrdersEndpoints implements OrdersEndpoints {

    private final MockUIService mock;

    MockOrdersEndpoints(MockUIService mock) {
        this.mock = mock;
    }

    @Override
    public List<OrderDTO> getOrders() throws Exception {
        return (List<OrderDTO>) mock.fetchData("orders/list", new HashMap<>(), List.class);
    }

    @Override
    public boolean createOrder(OrderDTO order) throws Exception {
        mock.sendData("orders/create", order, OrderDTO.class);
        return true;
    }

    @Override
    public boolean approveOrder(String orderId) throws Exception {
        mock.sendData("orders/approve", orderId, OrderDTO.class);
        return true;
    }

    @Override
    public boolean rejectOrder(String orderId, String rejectionReason) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId);
        p.put("reason", rejectionReason);
        mock.sendData("orders/reject", orderId, OrderDTO.class);
        return true;
    }

    @Override
    public boolean reviseOrder(String orderId, OrderDTO updatedOrder) throws Exception {
        mock.sendData("orders/revision", orderId, OrderDTO.class);
        return true;
    }

    @Override
    public boolean shipOrder(String orderId) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId);
        p.put("courier", "Default Courier");
        p.put("tracking", "TRK-" + orderId);
        mock.sendData("orders/ship", p, OrderDTO.class);
        return true;
    }

    @Override
    public boolean payOrder(String orderId, double amount) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId);
        p.put("amount", BigDecimal.valueOf(amount));
        p.put("simulateFail", false);
        mock.sendData("orders/pay", p, OrderDTO.class);
        return true;
    }

    @Override
    public boolean cancelOrder(String orderId, String cancellationReason) throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId);
        p.put("reason", cancellationReason);
        mock.sendData("orders/cancel", p, OrderDTO.class);
        return true;
    }

    @Override
    public Map<String, Object> getOrderStats() throws Exception {
        Map<String, Integer> raw = (Map<String, Integer>)
                mock.fetchData("orders/stats", new HashMap<>(), Map.class);
        Map<String, Object> out = new HashMap<>();
        if (raw != null) out.putAll(raw);
        return out;
    }
}