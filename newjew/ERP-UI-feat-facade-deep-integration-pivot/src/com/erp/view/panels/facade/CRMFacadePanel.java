package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class CRMFacadePanel extends FacadePanelBase {

    public CRMFacadePanel() { super("Customer Relationship Management"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Active Dealers",    "412", "+18 this quarter", Constants.PRIMARY_COLOR),
                new DashboardCard("Open Leads",        "87",  "22 hot",           Constants.WARNING_COLOR),
                new DashboardCard("Tickets Open",      "14",  "avg 4h response",  Constants.DANGER_COLOR),
                new DashboardCard("NPS Score",         "72",  "industry +18",     Constants.SUCCESS_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Lead Funnel (count)", FakeChartPanel.Style.BAR,
                        new int[]{240, 120, 60, 32, 18},
                        new String[]{"New","Qualified","Proposal","Negotiation","Won"}),
                new FakeChartPanel("Weekly Ticket Load", FakeChartPanel.Style.LINE,
                        new int[]{22, 28, 19, 24, 30, 14},
                        new String[]{"W1","W2","W3","W4","W5","W6"})
        );

        JPanel toolbar = toolbar(
                stubButton("Add Lead"),
                secondaryStubButton("Schedule Follow-up"),
                secondaryStubButton("Convert to Opportunity"),
                secondaryStubButton("Send Campaign")
        );

        String[] cols = {"Dealer", "City", "Segment", "Last Contact", "Stage"};
        Object[][] data = {
                {"Rakesh Industries",  "Mumbai",     "Fleet",   "2026-04-10", "Proposal"},
                {"Tata Motors Dealer", "Pune",       "Premium", "2026-04-11", "Negotiation"},
                {"Gupta Automobiles",  "Delhi",      "Fleet",   "2026-04-08", "Won"},
                {"Sharma & Sons",      "Jaipur",     "Retail",  "2026-04-09", "Qualified"},
                {"Blue Horizon Cars",  "Bangalore",  "Premium", "2026-04-12", "Proposal"},
                {"Metro Auto Hub",     "Hyderabad",  "Retail",  "2026-04-07", "New"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Dealer Pipeline", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
