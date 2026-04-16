package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class MarketingFacadePanel extends FacadePanelBase {

    public MarketingFacadePanel() { super("Marketing"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Active Campaigns", "9",   "2 launching",      Constants.PRIMARY_COLOR),
                new DashboardCard("Leads Generated",  "1,248","this month",      Constants.SUCCESS_COLOR),
                new DashboardCard("Avg Cost / Lead",  "\u20B9 410","-6% MoM",    Constants.ACCENT_COLOR),
                new DashboardCard("Budget Used",      "62%", "\u20B9 38L left",  Constants.WARNING_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Leads by Channel", FakeChartPanel.Style.BAR,
                        new int[]{420, 310, 280, 150, 88},
                        new String[]{"Digital","Dealer","Events","Print","Referral"}),
                new FakeChartPanel("Weekly Spend (L)", FakeChartPanel.Style.LINE,
                        new int[]{8, 9, 11, 10, 12, 14},
                        new String[]{"W1","W2","W3","W4","W5","W6"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Campaign"),
                secondaryStubButton("Segment Audience"),
                secondaryStubButton("A/B Test"),
                secondaryStubButton("Publish Creative")
        );

        String[] cols = {"Campaign", "Channel", "Start", "Leads", "Budget", "Status"};
        Object[][] data = {
                {"Model-EV Launch Teaser", "Digital",  "2026-04-01", "412", "\u20B9 18 L",   "ACTIVE"},
                {"Dealer Spring Sale",     "Dealer",   "2026-04-05", "230", "\u20B9  9 L",   "ACTIVE"},
                {"Auto Expo Booth",        "Events",   "2026-04-08", "145", "\u20B9 24 L",   "ACTIVE"},
                {"Print - Regional",       "Print",    "2026-03-20", "88",  "\u20B9  4 L",   "ENDING"},
                {"Referral Boost",         "Referral", "2026-04-10", "54",  "\u20B9  1.5 L", "ACTIVE"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Campaigns", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
