package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class AnalyticsFacadePanel extends FacadePanelBase {

    public AnalyticsFacadePanel() { super("Data Analytics"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Datasets",         "62",   "3 refreshed today", Constants.PRIMARY_COLOR),
                new DashboardCard("Active Dashboards","24",   "8 shared",          Constants.ACCENT_COLOR),
                new DashboardCard("Anomalies Flagged","5",    "review",            Constants.WARNING_COLOR),
                new DashboardCard("Data Freshness",   "98%",  "on SLA",            Constants.SUCCESS_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Defect Rate by Model (ppm)", FakeChartPanel.Style.BAR,
                        new int[]{320, 245, 412, 180},
                        new String[]{"Sedan","SUV","Truck","EV"}),
                new FakeChartPanel("Production vs Demand (Apr)", FakeChartPanel.Style.LINE,
                        new int[]{420, 445, 460, 452, 475, 480},
                        new String[]{"W1","W2","W3","W4","W5","W6"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Dataset"),
                secondaryStubButton("Run Query"),
                secondaryStubButton("Create Dashboard"),
                secondaryStubButton("Anomaly Detection")
        );

        String[] cols = {"Dataset", "Owner", "Rows", "Last Refresh", "Source"};
        Object[][] data = {
                {"orders_fact",           "Data Platform", "1.2M", "2026-04-12 03:00", "ERP.Orders"},
                {"production_units",      "Manufacturing", "840K", "2026-04-12 04:00", "MES"},
                {"quality_defects",       "Quality",       "92K",  "2026-04-12 02:00", "QMS"},
                {"hr_headcount",          "HR",            "420",  "2026-04-12 05:00", "HRIS"},
                {"finance_transactions",  "Finance",       "3.4M", "2026-04-12 02:30", "GL"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Datasets", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
