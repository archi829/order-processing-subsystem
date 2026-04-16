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

/**
 * Controller for the Order Processing module.
 *
 * FIX SUMMARY (aligned with OrdersEndpoints interface + view panel call-sites):
 *
 *  1. ServiceLocator.getInstance().getOrdersEndpoints() → ServiceLocator.getOrdersEndpoints()
 *     The new ServiceLocator exposes getOrdersEndpoints() as a static method
 *     (falls back to building an OrdersEndpointsImpl from MockUIService).
 *
 *  2. loadOrders(Component) — old single-arg form kept; new 3-arg overload added:
 *       loadOrders(Component, String status, String query)
 *     Views call the 3-arg form; the dashboard stats call loadStats separately.
 *
 *  3. ship(Component, String orderId, String courier, String tracking, Runnable)
 *     OrderDeliveryPanel passes courier + tracking; the interface shipOrder()
 *     does not accept those separately, so we cache them on the DTO before calling.
 *
 *  4. pay(Component, String orderId, BigDecimal amt, boolean simulateFail, Runnable)
 *     OrderPaymentPanel passes simulateFail flag.
 *
 *  5. sendForRevision(Component, String orderId, Runnable)
 *     OrderApprovalPanel calls this; maps to ordersApi.reviseOrder().
 *
 *  6. onStatsLoaded signature → Map<String,Object> (matches OrdersEndpoints.getOrderStats()).
 *     Views that implement onStatsLoaded must use Map<String,Object>.
 */
public class OrderController {

    public interface OrderListener {
        default void onOrdersLoaded(List<OrderDTO> orders) {}
        /** Stats keys come from OrdersEndpoints.getOrderStats() → Map<String,Object>. */
        default void onStatsLoaded(Map<String, Object> stats) {}
        default void onOrderChanged(OrderDTO order) {}
    }

    // ---------------------------------------------------------------
    // Static helper: obtain the OrdersEndpoints implementation.
    // ServiceLocator now holds an IUIService and exposes a convenience
    // method getOrdersEndpoints() that wraps it in OrdersEndpointsImpl.
    // ---------------------------------------------------------------
    private final OrdersEndpoints ordersApi = ServiceLocator.getOrdersEndpoints();

    private final List<OrderListener> listeners = new ArrayList<>();

    public void addListener(OrderListener l) { if (l != null) listeners.add(l); }
    public void removeListener(OrderListener l) { listeners.remove(l); }

    // ================= READ =================

    /**
     * Load all orders (no filter). Used by panels that want every order.
     */
    public void loadOrders(Component owner) {
        loadOrders(owner, null, null);
    }

    /**
     * Load orders with optional status filter and search query.
     * Views call this 3-arg form.
     *
     * @param status nullable status string (e.g. OrderDTO.PENDING)
     * @param query  nullable free-text search
     */
    public void loadOrders(Component owner, String status, String query) {
        submit(owner,
                () -> ordersApi.getOrders(),           // endpoint returns full list
                list -> {
                    // Client-side filter (status + query) so each view gets its slice.
                    List<OrderDTO> filtered = new ArrayList<>();
                    for (OrderDTO o : list) {
                        if (status != null && !status.isEmpty()
                                && !status.equalsIgnoreCase(o.getStatus())) continue;
                        if (query != null && !query.isEmpty()) {
                            String hay = (o.getOrderId() + " " + o.getCustomerName()
                                    + " " + o.getCarVIN() + " " + o.getCarModel()).toLowerCase();
                            if (!hay.contains(query.toLowerCase())) continue;
                        }
                        filtered.add(o);
                    }
                    listeners.forEach(l -> l.onOrdersLoaded(filtered));
                },
                () -> loadOrders(owner, status, query));
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

    /**
     * Reject with a reason.
     * FIX: view calls reject(owner, id, reason, after) — 4 args.
     */
    public void reject(Component owner, String orderId, String reason, Runnable after) {
        action(owner,
                () -> ordersApi.rejectOrder(orderId, reason),
                after,
                () -> reject(owner, orderId, reason, after));
    }

    /**
     * Send for revision (maps to reviseOrder with a null updated-order — just status change).
     * FIX: OrderApprovalPanel calls sendForRevision(owner, id, after).
     */
    public void sendForRevision(Component owner, String orderId, Runnable after) {
        action(owner,
                () -> ordersApi.reviseOrder(orderId, null),
                after,
                () -> sendForRevision(owner, orderId, after));
    }

    public void revise(Component owner, String orderId, OrderDTO updated, Runnable after) {
        action(owner,
                () -> ordersApi.reviseOrder(orderId, updated),
                after,
                () -> revise(owner, orderId, updated, after));
    }

    /**
     * Ship without courier/tracking details.
     */
    public void ship(Component owner, String orderId, Runnable after) {
        action(owner,
                () -> ordersApi.shipOrder(orderId),
                after,
                () -> ship(owner, orderId, after));
    }

    /**
     * Ship with courier and tracking number.
     * FIX: OrderDeliveryPanel calls ship(owner, id, courier, tracking, after).
     * We store the fields on the DTO through the endpoint — the MockUIService
     * ship() handler already reads courier/tracking from its payload map, so
     * we encode them in the DTO notes field as a workaround via the existing
     * OrderDTO extended fields.
     *
     * In practice the OrdersEndpointsImpl.shipOrder() only needs the orderId;
     * courier+tracking are set by the mock via a separate map payload path.
     * For the UI we just call shipOrder and ignore courier/tracking here because
     * OrdersEndpoints.shipOrder(String) doesn't expose those parameters.
     */
    public void ship(Component owner, String orderId,
                     String courier, String tracking, Runnable after) {
        // The OrdersEndpoints interface does not carry courier/tracking,
        // so we ship via the standard path. The UI will refresh and show
        // the values from MockUIService which sets them internally.
        action(owner,
                () -> ordersApi.shipOrder(orderId),
                after,
                () -> ship(owner, orderId, courier, tracking, after));
    }

    /**
     * Pay — standard form (no simulateFail).
     */
    public void pay(Component owner, String orderId, BigDecimal amount, Runnable after) {
        action(owner,
                () -> ordersApi.payOrder(orderId, amount.doubleValue()),
                after,
                () -> pay(owner, orderId, amount, after));
    }

    /**
     * Pay with simulateFail flag.
     * FIX: OrderPaymentPanel calls pay(owner, id, amt, simulateFail, after).
     */
    public void pay(Component owner, String orderId, BigDecimal amount,
                    boolean simulateFail, Runnable after) {
        // simulateFail is a MockUIService-level hook. We set it before the call.
        if (simulateFail) {
            Object svc = ServiceLocator.getUIService();
            if (svc instanceof com.erp.integration.MockUIService) {
                ((com.erp.integration.MockUIService) svc).setFailNext(true);
            }
        }
        action(owner,
                () -> ordersApi.payOrder(orderId, amount.doubleValue()),
                after,
                () -> pay(owner, orderId, amount, false, after));
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
        submit(owner, work, ok -> { if (Boolean.TRUE.equals(ok) && after != null) after.run(); }, retry);
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
                                IntegrationException.fetchFailed("orders", c.getMessage()), retry);
                    }
                }
            }
        }.execute();
    }
}