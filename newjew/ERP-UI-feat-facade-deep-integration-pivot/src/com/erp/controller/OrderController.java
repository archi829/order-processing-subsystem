package com.erp.controller;

import com.erp.exception.ERPException;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.IntegrationException;
import com.erp.integration.ServiceLocator;
import com.erp.integration.endpoints.OrdersEndpoints;
import com.erp.model.dto.OrderDTO;

import javax.swing.*;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OrderController {

    public interface OrderListener {
        default void onOrdersLoaded(List<OrderDTO> orders) {}
        default void onStatsLoaded(Map<String, Object> stats) {}
        default void onOrderChanged(OrderDTO order) {}
    }

    private final OrdersEndpoints ordersApi =
            ServiceLocator.getInstance().getOrdersEndpoints();

    private final List<OrderListener> listeners = new ArrayList<>();

    public void addListener(OrderListener l) { if (l != null) listeners.add(l); }
    public void removeListener(OrderListener l) { listeners.remove(l); }

    // ================= READ =================

    public void loadOrders(Component owner) {
        submit(owner,
                () -> ordersApi.getOrders(),
                list -> listeners.forEach(l -> l.onOrdersLoaded(list)),
                () -> loadOrders(owner));
    }

    public void loadStats(Component owner) {
        submit(owner,
                () -> ordersApi.getOrderStats(),
                stats -> listeners.forEach(l -> l.onStatsLoaded(stats)),
                () -> loadStats(owner));
    }

    // ================= WRITE =================

    public void createOrder(Component owner, OrderDTO dto, Consumer<OrderDTO> onSuccess) {
        submit(owner,
                () -> {
                    boolean ok = ordersApi.createOrder(dto);
                    return ok ? dto : null;
                },
                created -> {
                    if (created != null) {
                        listeners.forEach(l -> l.onOrderChanged(created));
                        if (onSuccess != null) onSuccess.accept(created);
                    }
                },
                () -> createOrder(owner, dto, onSuccess));
    }

    public void approve(Component owner, String orderId, Runnable after) {
        action(owner,
                () -> ordersApi.approveOrder(orderId),
                after,
                () -> approve(owner, orderId, after));
    }

    public void reject(Component owner, String orderId, String reason, Runnable after) {
        action(owner,
                () -> ordersApi.rejectOrder(orderId, reason),
                after,
                () -> reject(owner, orderId, reason, after));
    }

    public void revise(Component owner, String orderId, OrderDTO updated, Runnable after) {
        action(owner,
                () -> ordersApi.reviseOrder(orderId, updated),
                after,
                () -> revise(owner, orderId, updated, after));
    }

    public void ship(Component owner, String orderId, Runnable after) {
        action(owner,
                () -> ordersApi.shipOrder(orderId),
                after,
                () -> ship(owner, orderId, after));
    }

    public void pay(Component owner, String orderId, BigDecimal amount, Runnable after) {
        action(owner,
                () -> ordersApi.payOrder(orderId, amount.doubleValue()),
                after,
                () -> pay(owner, orderId, amount, after));
    }

    public void cancel(Component owner, String orderId, String reason, Runnable after) {
        action(owner,
                () -> ordersApi.cancelOrder(orderId, reason),
                after,
                () -> cancel(owner, orderId, reason, after));
    }

    // ================= HELPERS =================

    private void action(Component owner,
                        java.util.concurrent.Callable<Boolean> work,
                        Runnable after,
                        Runnable retry) {
        submit(owner,
                work,
                ok -> {
                    if (ok) {
                        if (after != null) after.run();
                    }
                },
                retry);
    }

    private <T> void submit(Component owner,
                            java.util.concurrent.Callable<T> work,
                            Consumer<T> onOk,
                            Runnable retry) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }

            @Override protected void done() {
                try {
                    onOk.accept(get());
                } catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;

                    if (c instanceof IntegrationException) {
                        ExceptionHandler.handle(owner, (ERPException) c, retry);
                    } else if (c instanceof ERPException) {
                        ExceptionHandler.handle(owner, (ERPException) c);
                    } else {
                        ExceptionHandler.handle(owner,
                                IntegrationException.fetchFailed("orders", c.getMessage()),
                                retry);
                    }
                }
            }
        }.execute();
    }
}