package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — a tabbed tree of Manufacturing sub-panels.
 *
 * All tabs share a single {@link ManufacturingController} so updates in one
 * tab propagate to others via the Observer contract.
 */
public class ManufacturingHomePanel extends BasePanel {

    public interface Refreshable { void refresh(); }

    private ManufacturingController controller;
    private JTabbedPane tabs;

    public ManufacturingHomePanel() { super("Manufacturing"); }

    @Override
    protected void initializeComponents() {
        if (controller == null) controller = new ManufacturingController();
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard",         new ManufacturingDashboardTab(controller));
        tabs.addTab("Assembly Lines",    new AssemblyLinesTab(controller));
        tabs.addTab("Production Orders", new ProductionOrdersTab(controller));
        tabs.addTab("BOM Explorer",      new BOMExplorerTab(controller));
        tabs.addTab("BOM Creator",       new BOMCreatorTab(controller));
        tabs.addTab("Routing",           new RoutingTab(controller));
        tabs.addTab("Work Centers",      new WorkCentersTab(controller));
        tabs.addTab("Quality Control",   new QualityControlTab(controller));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        Component sel = tabs.getSelectedComponent();
        if (sel instanceof Refreshable) ((Refreshable) sel).refresh();
    }
}
