package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

public class SalesFacadePanel extends FacadePanelBase {

    public SalesFacadePanel() { super("Sales Management"); }

    @Override
    protected JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel stats = statRow(
                new DashboardCard("Units Sold (MTD)",  "1,482", "+8.4% MoM",    Constants.SUCCESS_COLOR),
                new DashboardCard("Quotes Open",       "64",    "24 aging",     Constants.WARNING_COLOR),
                new DashboardCard("Top Dealer",        "Tata",  "\u20B9 3.9 Cr",Constants.PRIMARY_COLOR),
                new DashboardCard("Avg Discount",      "4.8%",  "within band",  Constants.ACCENT_COLOR)
        );

        JPanel charts = chartRow(
                new FakeChartPanel("Units Sold by Model", FakeChartPanel.Style.BAR,
                        new int[]{610, 420, 240, 212},
                        new String[]{"Sedan","SUV","Truck","EV"}),
                new FakeChartPanel("Region Revenue (Cr)", FakeChartPanel.Style.LINE,
                        new int[]{18, 22, 19, 26, 24, 28},
                        new String[]{"N","W","S","E","C","NE"})
        );

        JPanel toolbar = toolbar(
                stubButton("New Quote"),
                secondaryStubButton("Price List"),
                secondaryStubButton("Dealer Allocation"),
                secondaryStubButton("Sales Commission")
        );

        String[] cols = {"Quote #", "Dealer", "Model", "Units", "Value", "Status"};
        Object[][] data = {
                {"QT-9022", "Tata Motors Dealer", "Model-S Sedan",   "20", "\u20B9 4.90 Cr", "SUBMITTED"},
                {"QT-9023", "Gupta Automobiles",  "Model-EV",        "10", "\u20B9 4.20 Cr", "APPROVED"},
                {"QT-9024", "Sharma & Sons",      "Model-X SUV",     "15", "\u20B9 5.85 Cr", "NEGOTIATION"},
                {"QT-9025", "Metro Auto Hub",     "Model-T Truck",   "8",  "\u20B9 4.08 Cr", "DRAFT"},
                {"QT-9026", "Highway Dealers",    "Model-S Sedan",   "25", "\u20B9 6.12 Cr", "SUBMITTED"},
        };

        body.add(stats);
        body.add(Box.createVerticalStrut(12));
        body.add(charts);
        body.add(Box.createVerticalStrut(12));
        body.add(sectionCard("Recent Quotes", fakeTable(cols, data)));
        body.add(Box.createVerticalStrut(10));
        body.add(toolbar);
        return new JScrollPane(body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
}
