package com.erp.view;

import com.erp.view.panels.BasePanel;
import com.erp.view.panels.IntegratedDashboardPanel;
import com.erp.view.panels.facade.AccountingFacadePanel;
import com.erp.view.panels.facade.AnalyticsFacadePanel;
import com.erp.view.panels.facade.AutomationFacadePanel;
import com.erp.view.panels.facade.BIFacadePanel;
import com.erp.view.panels.facade.CRMFacadePanel;
import com.erp.view.panels.facade.FinanceFacadePanel;
import com.erp.view.panels.facade.MarketingFacadePanel;
import com.erp.view.panels.facade.ProjectFacadePanel;
import com.erp.view.panels.facade.ReportingFacadePanel;
import com.erp.view.panels.facade.SalesFacadePanel;
import com.erp.view.panels.hr.HRHomePanel;
import com.erp.view.panels.manufacturing.ManufacturingHomePanel;
import com.erp.view.panels.orders.OrdersHomePanel;
import com.erp.view.panels.supplychain.SupplyChainHomePanel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * PATTERN: Factory Method + Registry (Creational)
 * SOLID: OCP — adding a new module requires only a new {@link #register} call
 *              (or a static-init entry here); {@link MainFrame} itself does
 *              not change. The switch it used to carry has been deleted.
 */
public final class PanelRegistry {

    private static final Map<String, Supplier<BasePanel>> factories = new HashMap<>();

    static {
        register("dashboard",     IntegratedDashboardPanel::new);
        register("order",         OrdersHomePanel::new);
        register("hr",            HRHomePanel::new);
        register("manufacturing", ManufacturingHomePanel::new);
        register("inventory",     SupplyChainHomePanel::new);
        register("crm",           CRMFacadePanel::new);
        register("sales",         SalesFacadePanel::new);
        register("finance",       FinanceFacadePanel::new);
        register("accounting",    AccountingFacadePanel::new);
        register("project",       ProjectFacadePanel::new);
        register("reporting",     ReportingFacadePanel::new);
        register("analytics",     AnalyticsFacadePanel::new);
        register("bi",            BIFacadePanel::new);
        register("marketing",     MarketingFacadePanel::new);
        register("automation",    AutomationFacadePanel::new);
    }

    private PanelRegistry() {}

    public static void register(String command, Supplier<BasePanel> factory) {
        factories.put(command, factory);
    }

    public static boolean isRegistered(String command) { return factories.containsKey(command); }

    public static BasePanel create(String command) {
        return factories.getOrDefault(command, IntegratedDashboardPanel::new).get();
    }
}
