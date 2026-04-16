package com.erp.integration;

/**
 * Singleton service locator. Exposes the configured IUIService implementation.
 * Defaults to MockUIService; can be replaced for testing or live integration.
 */
public final class ServiceLocator {

    private static IUIService uiService = new MockUIService();

    private ServiceLocator() {}

    public static IUIService getUIService() { return uiService; }

    public static void setUIService(IUIService service) {
        if (service == null) throw new IllegalArgumentException("uiService cannot be null");
        uiService = service;
    }
}
