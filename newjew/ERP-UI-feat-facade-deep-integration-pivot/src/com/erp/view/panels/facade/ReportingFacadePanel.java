package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class ReportingFacadePanel extends FacadePanelBase {

    public ReportingFacadePanel() { super("Reporting"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Report Templates",  "48", "12 favourited",   Constants.PRIMARY_COLOR),
                new DashboardCard("Scheduled Runs",    "31", "next in 2h",      Constants.ACCENT_COLOR),
                new DashboardCard("Failed Runs (7d)",  "2",  "investigate",     Constants.DANGER_COLOR),
                new DashboardCard("Downloads (MTD)",   "612","+18% MoM",        Constants.SUCCESS_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Most Run Reports", FakeChartPanel.Style.BAR,
                        new int[]{144, 98, 77, 52, 38},
                        new String[]{"Orders","Inventory","HR","Finance","QA"}),
                new FakeChartPanel("Run Volume / day", FakeChartPanel.Style.LINE,
                        new int[]{42, 48, 39, 51, 44, 56, 60},
                        new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Report"),
                secondaryStubButton("Schedule Run"),
                secondaryStubButton("Export to PDF"),
                secondaryStubButton("Share with Role")
        );

        String[] cols = {"Template", "Owner", "Last Run", "Schedule", "Format"};
        Object[][] data = {
                {"Daily Orders Summary",        "Karan Mehta", "2026-04-12 07:00", "Daily 07:00",  "PDF"},
                {"Weekly Inventory Audit",      "Suresh Patil","2026-04-11 22:00", "Weekly Sun",   "XLSX"},
                {"HR Payroll Preview",          "Anita Rao",   "2026-04-01 10:30", "Monthly 1st",  "PDF"},
                {"Finance P&L Roll-up",         "Sneha Kapoor","2026-04-12 02:00", "Daily 02:00",  "XLSX"},
                {"Quality Defect Analysis",     "Ritu Sharma", "2026-04-10 15:00", "Weekly Fri",   "PDF"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Report Library", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
