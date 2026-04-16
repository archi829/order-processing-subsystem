package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/** Supply Chain dashboard — stat cards from {@code SCM_STATS}. */
public class SCMDashboardTab extends JPanel
        implements SupplyChainController.SupplyChainListener,
                   SupplyChainHomePanel.Refreshable {

    private final SupplyChainController controller;
    private final DashboardCard suppliers = new DashboardCard("Suppliers",   "-", "approved",        Constants.PRIMARY_COLOR);
    private final DashboardCard posOpen   = new DashboardCard("Open POs",    "-", "awaiting action", Constants.WARNING_COLOR);
    private final DashboardCard inTransit = new DashboardCard("In Transit",  "-", "inbound",         Constants.ACCENT_COLOR);
    private final DashboardCard lowStock  = new DashboardCard("Low Stock",   "-", "reorder needed",  Constants.DANGER_COLOR);
    private final DashboardCard invDue    = new DashboardCard("Invoices Due","-", "pending payment", Constants.SUCCESS_COLOR);

    public SCMDashboardTab(SupplyChainController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.add(suppliers); row.add(posOpen); row.add(inTransit); row.add(lowStock); row.add(invDue);
        add(row, BorderLayout.NORTH);

        refresh();
    }

    @Override public void refresh() { controller.loadStats(this); }

    @Override
    public void onStatsLoaded(Map<String, Integer> s) {
        suppliers.setValue(String.valueOf(s.getOrDefault("suppliers", 0)));
        posOpen.setValue(String.valueOf(s.getOrDefault("openPOs", 0)));
        inTransit.setValue(String.valueOf(s.getOrDefault("inTransit", 0)));
        lowStock.setValue(String.valueOf(s.getOrDefault("lowStock", 0)));
        invDue.setValue(String.valueOf(s.getOrDefault("invoicesDue", 0)));
    }

    @Override public void onSCMEntityChanged(Object e) { refresh(); }
}
