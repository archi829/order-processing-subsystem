package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class AutomationFacadePanel extends FacadePanelBase {

    public AutomationFacadePanel() { super("Automation"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Workflows Deployed", "34",  "6 paused",     Constants.PRIMARY_COLOR),
                new DashboardCard("Runs Today",         "412", "8 failed",     Constants.WARNING_COLOR),
                new DashboardCard("Time Saved (hrs)",   "284", "this week",    Constants.SUCCESS_COLOR),
                new DashboardCard("Avg Run Time",       "42s", "\u2198 -3s",   Constants.ACCENT_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Runs by Workflow", FakeChartPanel.Style.BAR,
                        new int[]{120, 95, 68, 54, 42},
                        new String[]{"Order-Close","HR-Onboard","PO-Alert","Payroll","Quality"}),
                new FakeChartPanel("Daily Run Volume", FakeChartPanel.Style.LINE,
                        new int[]{380, 410, 395, 430, 412, 450, 470},
                        new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Workflow"),
                secondaryStubButton("Trigger Manually"),
                secondaryStubButton("View Audit Log"),
                secondaryStubButton("Pause All")
        );

        String[] cols = {"Workflow", "Trigger", "Last Run", "Success %", "Owner"};
        Object[][] data = {
                {"Auto-Close Delivered Orders", "Event: ORDER_DELIVERED", "2026-04-12 06:30", "99%", "Ops Eng"},
                {"Onboarding Task Generator",   "Event: EMP_CREATED",     "2026-04-11 11:20", "100%","HR Ops"},
                {"Low-Stock Purchase Alert",    "Schedule: hourly",       "2026-04-12 07:00", "97%", "Supply Chain"},
                {"Monthly Payroll Prep",        "Schedule: 25th",         "2026-03-25 23:00", "100%","Finance Ops"},
                {"Defect Hold Auto-Escalate",   "Event: DEFECT_REPORTED", "2026-04-12 04:15", "95%", "Quality"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Workflows", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
