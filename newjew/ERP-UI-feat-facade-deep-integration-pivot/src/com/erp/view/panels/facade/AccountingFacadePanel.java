package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class AccountingFacadePanel extends FacadePanelBase {

    public AccountingFacadePanel() { super("Accounting"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Journal Entries (MTD)", "1,248", "reconciled", Constants.PRIMARY_COLOR),
                new DashboardCard("Unposted Entries",      "12",    "review due",  Constants.WARNING_COLOR),
                new DashboardCard("Closing Balance",       "\u20B9 42.6 Cr", "+3.1% MoM", Constants.SUCCESS_COLOR),
                new DashboardCard("Audit Flags",           "3",     "low risk",    Constants.DANGER_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Expense by Category", FakeChartPanel.Style.BAR,
                        new int[]{180, 145, 92, 68, 54},
                        new String[]{"Materials","Labor","Utilities","Logistics","Admin"}),
                new FakeChartPanel("Net Income Trend", FakeChartPanel.Style.LINE,
                        new int[]{14, 15, 13, 17, 18, 20},
                        new String[]{"Nov","Dec","Jan","Feb","Mar","Apr"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Journal Entry"),
                secondaryStubButton("Post Entries"),
                secondaryStubButton("Trial Balance"),
                secondaryStubButton("Close Period")
        );

        String[] cols = {"JE #", "Date", "Description", "Debit", "Credit", "Status"};
        Object[][] data = {
                {"JE-22001", "2026-04-10", "Raw Material Purchase", "\u20B9 48,00,000", "",           "POSTED"},
                {"JE-22002", "2026-04-10", "Vendor Payment - Bosch","",           "\u20B9 12,00,000", "POSTED"},
                {"JE-22003", "2026-04-11", "Payroll Accrual",       "\u20B9 36,50,000", "",           "DRAFT"},
                {"JE-22004", "2026-04-11", "Customer Invoice - Tata","",          "\u20B9 39,00,000", "POSTED"},
                {"JE-22005", "2026-04-12", "Electricity Bill",      "\u20B9  4,20,000", "",           "DRAFT"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("General Ledger - Recent Entries", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
