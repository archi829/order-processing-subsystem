package com.erp.controller;

import com.erp.exception.ERPException;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.IntegrationException;
import com.erp.integration.IUIService;
import com.erp.integration.ServiceLocator;
import com.erp.integration.endpoints.OrdersEndpoints;
import com.erp.model.dto.OrderDTO;

import javax.swing.*;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for the Order Processing module.
 *
 * Enforces MVC: owns the IUIService, runs every call on a SwingWorker,
 * publishes results to registered OrderListener observers.
 *
 * Central exception routing: all IntegrationException cases flow through
 * ExceptionHandler with a retry runnable bound to the original operation.
 */
public class OrderController {

    /** Observer contract for Order views. */
    public interface OrderListener {
        default void onOrdersLoaded(List<OrderDTO> orders) {}
        default void onStatsLoaded(Map<String, Integer> stats) {}
        default void onOrderChanged(OrderDTO order) {}
    }

    private final IUIService ui = ServiceLocator.getUIService();
    private final List<OrderListener> listeners = new ArrayList<>();

    public void addListener(OrderListener l) { if (l != null) listeners.add(l); }
    public void removeListener(OrderListener l) { listeners.remove(l); }

    // ===== Reads =====

    public void loadOrders(Component owner, String statusFilter, String query) {
        Map<String, Object> params = new HashMap<>();
        if (statusFilter != null) params.put("status", statusFilter);
        if (query != null) params.put("q", query);
        submit(owner,
                () -> OrderController.<List<OrderDTO>>cast(ui.fetchData(OrdersEndpoints.ORDERS_LIST, params, List.class)),
                list -> listeners.forEach(l -> l.onOrdersLoaded(list)),
                () -> loadOrders(owner, statusFilter, query));
    }

    public void loadStats(Component owner) {
        submit(owner,
                () -> ui.fetchData(OrdersEndpoints.ORDERS_STATS, new HashMap<>(), Map.class),
                stats -> listeners.forEach(l -> l.onStatsLoaded(stats)),
                () -> loadStats(owner));
    }

    // ===== Writes =====

    public void createOrder(Component owner, OrderDTO dto, Consumer<OrderDTO> onSuccess) {
        submit(owner,
                () -> ui.sendData(OrdersEndpoints.ORDERS_CREATE, dto, OrderDTO.class),
                created -> {
                    listeners.forEach(l -> l.onOrderChanged(created));
                    if (onSuccess != null) onSuccess.accept(created);
                },
                () -> createOrder(owner, dto, onSuccess));
    }

    public void approve(Component owner, String orderId, Runnable after) {
        act(owner, OrdersEndpoints.ORDERS_APPROVE, orderId, after, () -> approve(owner, orderId, after));
    }

    public void reject(Component owner, String orderId, Runnable after) {
        act(owner, OrdersEndpoints.ORDERS_REJECT, orderId, after, () -> reject(owner, orderId, after));
    }

    public void sendForRevision(Component owner, String orderId, Runnable after) {
        act(owner, OrdersEndpoints.ORDERS_REVISION, orderId, after, () -> sendForRevision(owner, orderId, after));
    }

    public void ship(Component owner, String orderId, String courier, String tracking, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId); p.put("courier", courier); p.put("tracking", tracking);
        submit(owner,
                () -> ui.sendData(OrdersEndpoints.ORDERS_SHIP, p, OrderDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onOrderChanged(updated));
                    if (after != null) after.run();
                },
                () -> ship(owner, orderId, courier, tracking, after));
    }

    public void pay(Component owner, String orderId, BigDecimal amount, boolean simulateFail, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId); p.put("amount", amount); p.put("simulateFail", simulateFail);
        submit(owner,
                () -> ui.sendData(OrdersEndpoints.ORDERS_PAY, p, OrderDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onOrderChanged(updated));
                    if (after != null) after.run();
                },
                () -> pay(owner, orderId, amount, simulateFail, after));
    }

    public void cancel(Component owner, String orderId, String reason, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("orderId", orderId); p.put("reason", reason);
        submit(owner,
                () -> ui.sendData(OrdersEndpoints.ORDERS_CANCEL, p, OrderDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onOrderChanged(updated));
                    if (after != null) after.run();
                },
                () -> cancel(owner, orderId, reason, after));
    }

    // ===== helpers =====

    private void act(Component owner, String endpoint, String orderId, Runnable after, Runnable retry) {
        submit(owner,
                () -> ui.sendData(endpoint, orderId, OrderDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onOrderChanged(updated));
                    if (after != null) after.run();
                },
                retry);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) { return (T) o; }

    private <T> void submit(Component owner,
                            java.util.concurrent.Callable<T> work,
                            Consumer<T> onOk,
                            Runnable retry) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                try { onOk.accept(get()); }
                catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    if (c instanceof IntegrationException) {
                        ExceptionHandler.handle(owner, (ERPException) c, retry);
                    } else if (c instanceof ERPException) {
                        ExceptionHandler.handle(owner, (ERPException) c);
                    } else {
                        ExceptionHandler.handle(owner,
                                IntegrationException.fetchFailed("order", c.getMessage()), retry);
                    }
                }
            }
        }.execute();
    }
}
