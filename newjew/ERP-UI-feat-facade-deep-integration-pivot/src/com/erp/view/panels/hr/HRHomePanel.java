package com.erp.view.panels.hr;

import com.erp.controller.HRController;
import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.orders.OrdersHomePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Tabbed container for the HR Management module (Team Jazz Girls).
 * All tabs share a single {@link HRController} so changes in one tab
 * propagate everywhere through the observer contract.
 */
public class HRHomePanel extends BasePanel {

    private HRController controller;
    private JTabbedPane tabs;

    public HRHomePanel() { super("HR Management"); }

    @Override
    protected void initializeComponents() {
        if (controller == null) controller = new HRController();
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Employees",    new EIMSPanel(controller));
        tabs.addTab("Recruitment",  new RecruitmentPanel(controller));
        tabs.addTab("Onboarding",   new OnboardingPanel(controller));
        tabs.addTab("Payroll",      new PayrollPanel(controller));
        tabs.addTab("Attendance & Leave", new AttendanceLeavePanel(controller));
        tabs.addTab("Performance",  new PerformancePanel(controller));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        Component sel = tabs.getSelectedComponent();
        if (sel instanceof OrdersHomePanel.Refreshable) ((OrdersHomePanel.Refreshable) sel).refresh();
    }
}
