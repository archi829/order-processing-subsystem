package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — tabbed Supply Chain / Purchasing module.
 *
 * All tabs share a single {@link SupplyChainController}.
 */
public class SupplyChainHomePanel extends BasePanel {

    public interface Refreshable { void refresh(); }

    private SupplyChainController controller;
    private JTabbedPane tabs;

    public SupplyChainHomePanel() { super("Supply Chain"); }

    @Override
    protected void initializeComponents() {
        if (controller == null) controller = new SupplyChainController();
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard",        new SCMDashboardTab(controller));
        tabs.addTab("Inventory",        new InventoryTab(controller));
        tabs.addTab("Purchase Orders",  new PurchaseOrdersTab(controller));
        tabs.addTab("Suppliers",        new SuppliersTab(controller));
        tabs.addTab("Goods Receipts",   new GoodsReceiptTab(controller));
        tabs.addTab("Shipments",        new ShipmentsTab(controller));
        tabs.addTab("Invoices",         new InvoicesTab(controller));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        Component sel = tabs.getSelectedComponent();
        if (sel instanceof Refreshable) ((Refreshable) sel).refresh();
    }
}
