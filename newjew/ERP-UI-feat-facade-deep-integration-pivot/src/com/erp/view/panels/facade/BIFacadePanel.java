package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class BIFacadePanel extends FacadePanelBase {

    public BIFacadePanel() { super("Business Intelligence"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Executive KPIs",    "18",     "12 on target",   Constants.PRIMARY_COLOR),
                new DashboardCard("Forecast Accuracy", "92.4%",  "rolling 90d",    Constants.SUCCESS_COLOR),
                new DashboardCard("Churn Risk Dealers","7",      "escalate",       Constants.DANGER_COLOR),
                new DashboardCard("YoY Revenue \u0394","+14.2%", "ahead of plan",  Constants.ACCENT_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Revenue Forecast (Cr)", FakeChartPanel.Style.LINE,
                        new int[]{48, 52, 56, 61, 65, 70},
                        new String[]{"May","Jun","Jul","Aug","Sep","Oct"}),
                new FakeChartPanel("Segment Contribution (%)", FakeChartPanel.Style.BAR,
                        new int[]{42, 28, 18, 12},
                        new String[]{"Sedan","SUV","Truck","EV"})
        );

        JPanel toolbar = toolbar(
                stubButton("Executive Scorecard"),
                secondaryStubButton("Forecast Model"),
                secondaryStubButton("Customer Insight"),
                secondaryStubButton("Trend Deep-Dive")
        );

        String[] cols = {"KPI", "Current", "Target", "Trend", "Owner"};
        Object[][] data = {
                {"Revenue (Cr)",         "48.2", "50.0", "\u2197 +4%",  "Finance"},
                {"Units Delivered",       "2412", "2500", "\u2197 +2%",  "Operations"},
                {"Dealer Satisfaction",   "4.6/5","4.5",  "\u2198 -0.1", "CRM"},
                {"Defect Rate (ppm)",     "265",  "250",  "\u2197 +6",   "Quality"},
                {"Headcount",             "420",  "430",  "\u2192 flat", "HR"},
                {"Inventory Turnover",    "6.1x", "6.5x", "\u2197 +0.3", "Supply Chain"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Executive Scorecard", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
