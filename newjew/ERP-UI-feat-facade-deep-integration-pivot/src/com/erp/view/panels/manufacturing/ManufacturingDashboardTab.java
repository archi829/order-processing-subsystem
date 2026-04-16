package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Manufacturing dashboard tab — stat cards summarising shop-floor state.
 * Listens to {@link ManufacturingController} via the Observer contract.
 */
public class ManufacturingDashboardTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private final ManufacturingController controller;
    private final DashboardCard cars      = new DashboardCard("Cars In Line",   "-", "active builds",         Constants.PRIMARY_COLOR);
    private final DashboardCard pending   = new DashboardCard("Pending POs",    "-", "awaiting start",        Constants.WARNING_COLOR);
    private final DashboardCard inProg    = new DashboardCard("In Progress",    "-", "production running",    Constants.ACCENT_COLOR);
    private final DashboardCard completed = new DashboardCard("Completed",      "-", "delivered this month",  Constants.SUCCESS_COLOR);
    private final DashboardCard qcFail    = new DashboardCard("QC Fails",       "-", "defect threshold breaches", Constants.DANGER_COLOR);

    public ManufacturingDashboardTab(ManufacturingController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.add(cars); row.add(pending); row.add(inProg); row.add(completed); row.add(qcFail);
        add(row, BorderLayout.NORTH);

        JLabel hint = new JLabel("<html><i>Live shop-floor metrics — refreshed on tab focus.</i></html>");
        hint.setFont(Constants.FONT_SMALL);
        hint.setForeground(Constants.TEXT_SECONDARY);
        add(hint, BorderLayout.SOUTH);

        refresh();
    }

    @Override public void refresh() { controller.loadStats(this); }

    @Override public void onStatsLoaded(Map<String, Integer> s) {
        cars.setValue(String.valueOf(s.getOrDefault("cars", s.getOrDefault("carsInLine", 0))));
        pending.setValue(String.valueOf(s.getOrDefault("pendingOrders", 0)));
        inProg.setValue(String.valueOf(s.getOrDefault("inProgress", s.getOrDefault("activeOrders", 0))));
        completed.setValue(String.valueOf(s.getOrDefault("completed", s.getOrDefault("completedOrders", 0))));
        qcFail.setValue(String.valueOf(s.getOrDefault("qcFails", s.getOrDefault("carsInQuality", 0))));
    }

    @Override public void onMfgEntityChanged(Object e) { refresh(); }
}
