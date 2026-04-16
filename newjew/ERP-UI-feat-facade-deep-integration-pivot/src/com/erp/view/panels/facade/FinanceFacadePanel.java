package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class FinanceFacadePanel extends FacadePanelBase {

    public FinanceFacadePanel() { super("Financial Management"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Revenue (FY26 YTD)", "\u20B9 48.2 Cr",  "+12.4% YoY",  Constants.SUCCESS_COLOR),
                new DashboardCard("Outstanding A/R",    "\u20B9  6.8 Cr",  "34 overdue",  Constants.WARNING_COLOR),
                new DashboardCard("A/P Due This Week",  "\u20B9  2.1 Cr",  "12 vendors",  Constants.PRIMARY_COLOR),
                new DashboardCard("Cash Position",      "\u20B9 14.9 Cr",  "healthy",     Constants.ACCENT_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Monthly Revenue (Cr)", FakeChartPanel.Style.BAR,
                        new int[]{32, 38, 41, 35, 44, 48},
                        new String[]{"Nov","Dec","Jan","Feb","Mar","Apr"}),
                new FakeChartPanel("Cash Flow Trend", FakeChartPanel.Style.LINE,
                        new int[]{11, 12, 10, 13, 14, 15},
                        new String[]{"Nov","Dec","Jan","Feb","Mar","Apr"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Invoice"),
                secondaryStubButton("Record Payment"),
                secondaryStubButton("Export to GL"),
                secondaryStubButton("Budget Forecast")
        );

        String[] cols = {"Invoice #", "Customer", "Amount", "Due Date", "Status"};
        Object[][] data = {
                {"INV-3041", "Tata Motors Dealer",  "\u20B9 39,00,000", "2026-04-18", "DUE"},
                {"INV-3042", "Mahindra Showroom",    "\u20B9 51,00,000", "2026-04-14", "OVERDUE"},
                {"INV-3043", "Pearson Motors",       "\u20B9 24,75,000", "2026-04-22", "PAID"},
                {"INV-3044", "Gupta Automobiles",    "\u20B9 42,00,000", "2026-04-30", "DUE"},
                {"INV-3045", "Orion Logistics",      "\u20B9 52,50,000", "2026-05-05", "PARTIAL"},
                {"INV-3046", "Blue Horizon Cars",    "\u20B9 25,10,000", "2026-04-25", "PAID"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Recent Invoices", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
