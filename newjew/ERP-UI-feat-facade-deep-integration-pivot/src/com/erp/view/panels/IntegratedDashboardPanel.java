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
 * Cross-module dashboard: surfaces live stats from the two deep-integrated
 * workflows (Orders + HR) plus a greeting card for the active session.
 */
public class IntegratedDashboardPanel extends BasePanel
        implements OrderController.OrderListener, HRController.HRListener {

    private OrderController orderController;
    private HRController hrController;

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

        ordersTotal = new DashboardCard("Orders", "-", "all pipelines", Constants.PRIMARY_COLOR);
        ordersPending = new DashboardCard("Pending Approvals", "-", "needs action", Constants.WARNING_COLOR);
        ordersDelivered = new DashboardCard("Delivered", "-", "fulfilled", Constants.SUCCESS_COLOR);
        workforce = new DashboardCard("Workforce", "-", "active employees", Constants.ACCENT_COLOR);
        newJoiners = new DashboardCard("New Joiners", "-", "onboarding", Constants.PRIMARY_DARK);
        pendingLeave = new DashboardCard("Pending Leave", "-", "HR to review", Constants.DANGER_COLOR);

        ordersChart = new FakeChartPanel("Order Pipeline", FakeChartPanel.Style.BAR);
        hrChart = new FakeChartPanel("Workforce by Department", FakeChartPanel.Style.BAR);

        orderController.addListener(this);
        hrController.addListener(this);
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
        stats.add(workforce); stats.add(newJoiners); stats.add(pendingLeave);

        JPanel charts = new JPanel(new GridLayout(1, 2, 10, 0));
        charts.setOpaque(false);
        charts.add(ordersChart); charts.add(hrChart);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(hero, BorderLayout.NORTH);
        north.add(stats, BorderLayout.CENTER);

        contentPanel.add(north, BorderLayout.NORTH);
        contentPanel.add(charts, BorderLayout.CENTER);
        refreshData();
    }

    @Override
    public void refreshData() {
        UserSession s = UserSession.getInstance();
        String who = s.isValid() ? s.getDisplayName() + " (" + s.getRole() + ")" : "Guest";
        greetingLabel.setText("Welcome back, " + who);

        orderController.loadOrders(this, null, null);
        orderController.loadStats(this);
        hrController.loadStats(this);
        hrController.loadEmployees(this, null, null, null);
    }

    // ===== OrderListener =====

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        Map<String, Integer> byStatus = new HashMap<>();
        for (OrderDTO o : orders) {
            String s = o.getStatus() == null ? "Other" : o.getStatus();
            byStatus.merge(s, 1, Integer::sum);
        }
        ordersChart.setData(byStatus);
    }

    @Override
    public void onStatsLoaded(Map<String, Integer> stats) {
        boolean isHR = stats.containsKey("active") || stats.containsKey("newJoiners");
        if (isHR) {
            workforce.setValue(String.valueOf(stats.getOrDefault("active", 0)));
            newJoiners.setValue(String.valueOf(stats.getOrDefault("newJoiners", 0)));
            pendingLeave.setValue(String.valueOf(stats.getOrDefault("pendingLeave", 0)));
        } else {
            ordersTotal.setValue(String.valueOf(stats.getOrDefault("total", 0)));
            ordersPending.setValue(String.valueOf(stats.getOrDefault("pending", 0)));
            ordersDelivered.setValue(String.valueOf(stats.getOrDefault("delivered", 0)));
        }
    }

    // ===== HRListener =====

    @Override
    public void onEmployeesLoaded(List<EmployeeDTO> list) {
        Map<String, Integer> byDept = new HashMap<>();
        for (EmployeeDTO e : list)
            byDept.merge(e.getDepartment() == null ? "Other" : e.getDepartment(), 1, Integer::sum);
        hrChart.setData(byDept);
    }
}
