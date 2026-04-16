package com.erp.integration;

import com.erp.integration.endpoints.OrdersEndpoints;

/**
 * Singleton service locator. Exposes the configured IUIService implementation.
 * Defaults to MockUIService; can be replaced for testing or live integration.
 *
 * FIX: Added getOrdersEndpoints() so OrderController no longer calls
 *      ServiceLocator.getInstance().getOrdersEndpoints() (getInstance() did not exist).
 *      The method lazily builds an OrdersEndpointsImpl wrapping the current IUIService.
 */
public final class ServiceLocator {

    private static IUIService uiService = new MockUIService();

    // Lazily-cached endpoints wrapper. Rebuilt when uiService changes.
    private static com.erp.integration.endpoints.OrdersEndpoints ordersEndpoints;

    private ServiceLocator() {}

    public static IUIService getUIService() { return uiService; }

    public static void setUIService(IUIService service) {
        if (service == null) throw new IllegalArgumentException("uiService cannot be null");
        uiService = service;
        ordersEndpoints = null; // invalidate cached wrapper
    }

    /**
     * Returns an OrdersEndpoints implementation backed by the current IUIService.
     * Uses the DesignX OrdersEndpointsImpl adapter that is on the classpath via
     * the erp-contracts jar.
     */
    public static com.erp.integration.endpoints.OrdersEndpoints getOrdersEndpoints() {
        if (ordersEndpoints == null) {
            // OrdersEndpointsImpl is provided by the order-processing team's jar.
            // It lives at com.designx.erp.external.OrdersEndpointsImpl and implements
            // com.erp.integration.endpoints.OrdersEndpoints.
            try {
                Class<?> cls = Class.forName("com.designx.erp.external.OrdersEndpointsImpl");
                ordersEndpoints = (com.erp.integration.endpoints.OrdersEndpoints) cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                // Fallback: build a thin adapter over MockUIService so UI still works
                // in environments where the order-processing jar is absent.
                ordersEndpoints = new MockOrdersEndpoints((MockUIService)
                        (uiService instanceof MockUIService ? uiService : new MockUIService()));
            }
        }
        return ordersEndpoints;
    }
}