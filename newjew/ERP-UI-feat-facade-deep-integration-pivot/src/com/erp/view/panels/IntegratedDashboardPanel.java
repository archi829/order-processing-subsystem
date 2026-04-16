package com.erp.view.panels;

import com.erp.controller.HRController;
import com.erp.controller.OrderController;
import com.erp.model.dto.EmployeeDTO;
import com.erp.model.dto.OrderDTO;
import com.erp.session.UserSession;
import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cross-module dashboard.
 *
 * FIX 1: loadOrders() now called with 3 args: (Component, status, query).
 * FIX 2: onStatsLoaded(Map<String,Object>) — matches OrderController.OrderListener.
 *         HRController.HRListener still uses Map<String,Integer>; the two listeners
 *         are kept separate to avoid the erasure clash.
 */
public class IntegratedDashboardPanel extends BasePanel
    implements OrderController.OrderListener {

    private OrderController orderController;
    private HRController hrController;
    private HRController.HRListener hrListener;

    private DashboardCard ordersTotal;
    private DashboardCard ordersPending;
    private DashboardCard ordersDelivered;
    private DashboardCard workforce;
    private DashboardCard newJoiners;
    private DashboardCard pendingLeave;

    private FakeChartPanel ordersChart;
    private FakeChartPanel hrChart;

    private JLabel greetingLabel;

    public IntegratedDashboardPanel() { super("Executive Dashboard"); }

    @Override
    protected void initializeComponents() {
        if (orderController == null) orderController = new OrderController();
        if (hrController == null) hrController = new HRController();

        hrListener = new HRController.HRListener() {
            @Override
            public void onStatsLoaded(Map<String, Integer> stats) {
                workforce.setValue(String.valueOf(stats.getOrDefault("active", 0)));
                newJoiners.setValue(String.valueOf(stats.getOrDefault("newJoiners", 0)));
                pendingLeave.setValue(String.valueOf(stats.getOrDefault("pendingLeave", 0)));
            }

            @Override
            public void onEmployeesLoaded(List<EmployeeDTO> list) {
                Map<String, Integer> byDept = new HashMap<>();
                for (EmployeeDTO e : list) {
                    byDept.merge(e.getDepartment() == null ? "Other" : e.getDepartment(), 1, Integer::sum);
                }
                hrChart.setData(byDept);
            }
        };

        ordersTotal   = new DashboardCard("Orders",           "-", "all pipelines",    Constants.PRIMARY_COLOR);
        ordersPending = new DashboardCard("Pending Approvals","-", "needs action",     Constants.WARNING_COLOR);
        ordersDelivered = new DashboardCard("Delivered",      "-", "fulfilled",        Constants.SUCCESS_COLOR);
        workforce     = new DashboardCard("Workforce",        "-", "active employees", Constants.ACCENT_COLOR);
        newJoiners    = new DashboardCard("New Joiners",      "-", "onboarding",       Constants.PRIMARY_DARK);
        pendingLeave  = new DashboardCard("Pending Leave",    "-", "HR to review",     Constants.DANGER_COLOR);

        ordersChart = new FakeChartPanel("Order Pipeline",           FakeChartPanel.Style.BAR);
        hrChart     = new FakeChartPanel("Workforce by Department",  FakeChartPanel.Style.BAR);

        orderController.addListener(this);
        hrController.addListener(hrListener);
        greetingLabel = new JLabel();
        greetingLabel.setFont(Constants.FONT_SUBTITLE);
        greetingLabel.setForeground(Constants.TEXT_PRIMARY);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout(0, 10));

        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(Constants.BG_WHITE);
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        hero.add(greetingLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("Live view across Order Processing and HR Management");
        hint.setFont(Constants.FONT_SMALL);
        hint.setForeground(Constants.TEXT_SECONDARY);
        hero.add(hint, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(2, 3, 10, 10));
        stats.setOpaque(false);
        stats.add(ordersTotal); stats.add(ordersPending); stats.add(ordersDelivered);
        stats.add(workforce);   stats.add(newJoiners);    stats.add(pendingLeave);

        JPanel charts = new JPanel(new GridLayout(1, 2, 10, 0));
        charts.setOpaque(false);
        charts.add(ordersChart); charts.add(hrChart);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(hero,   BorderLayout.NORTH);
        north.add(stats,  BorderLayout.CENTER);

        contentPanel.add(north,  BorderLayout.NORTH);
        contentPanel.add(charts, BorderLayout.CENTER);
        refreshData();
    }

    @Override
    public void refreshData() {
        UserSession s = UserSession.getInstance();
        String who = s.isValid() ? s.getDisplayName() + " (" + s.getRole() + ")" : "Guest";
        greetingLabel.setText("Welcome back, " + who);

        // FIX: 3-arg loadOrders
        orderController.loadOrders(this, null, null);
        orderController.loadStats(this);
        hrController.loadStats(this);
        hrController.loadEmployees(this, null, null, null);
    }

    // ===== OrderController.OrderListener =====

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        Map<String, Integer> byStatus = new HashMap<>();
        for (OrderDTO o : orders) {
            String st = o.getStatus() == null ? "Other" : o.getStatus();
            byStatus.merge(st, 1, Integer::sum);
        }
        ordersChart.setData(byStatus);
    }

    /**
     * FIX: OrderController.OrderListener uses Map<String,Object>.
     * We handle both order stats and HR stats here by inspecting the keys.
     */
    @Override
    public void onStatsLoaded(Map<String, Object> stats) {
        ordersTotal.setValue(String.valueOf(getInt(stats, "total")));
        ordersPending.setValue(String.valueOf(getInt(stats, "pending")));
        ordersDelivered.setValue(String.valueOf(getInt(stats, "delivered")));
    }

    // ===== helper =====

    private static int getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }
}