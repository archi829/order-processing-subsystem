package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class ProjectFacadePanel extends FacadePanelBase {

    public ProjectFacadePanel() { super("Project Management"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Active Projects",  "14", "3 critical",    Constants.PRIMARY_COLOR),
                new DashboardCard("Tasks Due Today",  "27", "9 blocked",     Constants.WARNING_COLOR),
                new DashboardCard("Milestones Hit",   "68", "this quarter",  Constants.SUCCESS_COLOR),
                new DashboardCard("Budget Utilized",  "71%","within plan",   Constants.ACCENT_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Project Completion %", FakeChartPanel.Style.BAR,
                        new int[]{82, 64, 45, 91, 38, 70},
                        new String[]{"EV-R&D","Line-D","Paint","QA-Cert","Export","Dealer App"}),
                new FakeChartPanel("Velocity (story pts)", FakeChartPanel.Style.LINE,
                        new int[]{32, 38, 41, 44, 47, 50},
                        new String[]{"S1","S2","S3","S4","S5","S6"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Project"),
                secondaryStubButton("Add Task"),
                secondaryStubButton("Gantt Chart"),
                secondaryStubButton("Resource Plan")
        );

        String[] cols = {"Project", "Owner", "Progress", "Budget", "Deadline", "Status"};
        Object[][] data = {
                {"Model-EV R&D Phase 2",   "Meera Nair",      "82%", "\u20B9 4.5 Cr", "2026-07-30", "ON_TRACK"},
                {"New Line-D Commission",  "Arjun Verma",     "64%", "\u20B9 9.0 Cr", "2026-06-15", "AT_RISK"},
                {"Paint Shop Upgrade",     "Rohan Das",       "45%", "\u20B9 2.1 Cr", "2026-05-20", "ON_TRACK"},
                {"Dealer Mobile App v2",   "Karan Mehta",     "70%", "\u20B9 0.8 Cr", "2026-05-05", "ON_TRACK"},
                {"Export Compliance",      "Latha Menon",     "38%", "\u20B9 1.2 Cr", "2026-08-10", "BLOCKED"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Active Projects", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
