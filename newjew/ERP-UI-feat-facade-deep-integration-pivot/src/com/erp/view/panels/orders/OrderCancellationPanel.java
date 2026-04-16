package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.OrderDTO;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Cancel an order with a reason; refund is automatic for paid/partial orders.
 */
public class OrderCancellationPanel extends JPanel
        implements OrderController.OrderListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"Order ID", "Customer", "Model", "Status", "Payment", "Cancel Reason"};

    private final OrderController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> reason = new JComboBox<>(new String[]{
            "Customer requested cancellation",
            "Stock unavailable",
            "Pricing dispute",
            "Duplicate order",
            "Other"});
    private final JTextField customReason = new JTextField();

    public OrderCancellationPanel(OrderController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Constants.TEXT_LIGHT);
        JScrollPane sp = new JScrollPane(table);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setBackground(Constants.BG_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        form.add(new JLabel("Reason:"));
        form.add(reason);
        form.add(new JLabel("Custom:"));
        customReason.setPreferredSize(new Dimension(260, 28));
        form.add(customReason);
        JButton cancel = new JButton("Cancel Order");
        cancel.setBackground(Constants.DANGER_COLOR); cancel.setForeground(Constants.TEXT_LIGHT);
        cancel.addActionListener(e -> cancel());
        form.add(cancel);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        form.add(refresh);

        add(new JLabel("Cancellable orders (not yet delivered):"), BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        refresh();
    }

    private void cancel() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        String id = (String) model.getValueAt(r, 0);
        String why = customReason.getText().trim();
        if (why.isEmpty()) why = (String) reason.getSelectedItem();
        int ok = JOptionPane.showConfirmDialog(this,
                "Cancel order " + id + "?\nReason: " + why
                        + "\n\nRefund (if any) will be triggered automatically.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            controller.cancel(this, id, why, () -> { customReason.setText(""); refresh(); });
        }
    }

    @Override
    public void refresh() { controller.loadOrders(this, null, null); }

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        model.setRowCount(0);
        for (OrderDTO o : orders) {
            if (OrderDTO.DELIVERED.equals(o.getStatus())) continue;
            model.addRow(new Object[]{
                    o.getOrderId(), o.getCustomerName(), o.getCarModel(),
                    o.getStatus(), o.getPaymentStatus(),
                    o.getCancellationReason() == null ? "-" : o.getCancellationReason()
            });
        }
    }

    @Override public void onOrderChanged(OrderDTO o) { refresh(); }
}
