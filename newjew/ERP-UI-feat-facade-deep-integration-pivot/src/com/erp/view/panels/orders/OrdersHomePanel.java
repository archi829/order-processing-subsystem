package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Tabbed container for all Order Processing sub-panels.
 * Shares a single OrderController so state stays consistent across tabs.
 */
public class OrdersHomePanel extends BasePanel {

    private OrderController controller;
    private JTabbedPane tabs;

    public OrdersHomePanel() { super("Order Processing"); }

    @Override
    protected void initializeComponents() {
        if (controller == null) controller = new OrderController();
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard",    new OrderDashboardPanel(controller));
        tabs.addTab("New Order",    new OrderEntryPanel(controller));
        tabs.addTab("Approvals",    new OrderApprovalPanel(controller));
        tabs.addTab("Delivery",     new OrderDeliveryPanel(controller));
        tabs.addTab("Payment",      new OrderPaymentPanel(controller));
        tabs.addTab("Cancellation", new OrderCancellationPanel(controller));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        Component sel = tabs.getSelectedComponent();
        if (sel instanceof Refreshable) ((Refreshable) sel).refresh();
    }

    public interface Refreshable { void refresh(); }
}
