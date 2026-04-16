package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.OrderDTO;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Approvals: Manager views pending orders, Approves / Rejects / Sends for Revision.
 */
public class OrderApprovalPanel extends JPanel
        implements OrderController.OrderListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"Order ID", "Customer", "Model", "Amount", "Date", "Status"};
    private static final NumberFormat INR = NumberFormat.getInstance(new Locale("en", "IN"));

    private final OrderController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextArea detail = new JTextArea(8, 30);

    public OrderApprovalPanel(OrderController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Constants.TEXT_LIGHT);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetail();
        });
        JScrollPane left = new JScrollPane(table);

        detail.setEditable(false);
        detail.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detail.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(Constants.BG_WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        JLabel t = new JLabel(" Order details");
        t.setFont(Constants.FONT_HEADING);
        right.add(t, BorderLayout.NORTH);
        right.add(new JScrollPane(detail), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        actions.setBackground(Constants.BG_WHITE);
        JButton approve = styled("Approve", Constants.SUCCESS_COLOR, e -> act("approve"));
        JButton revise  = styled("Send for Revision", Constants.WARNING_COLOR, e -> act("revise"));
        JButton reject  = styled("Reject", Constants.DANGER_COLOR, e -> act("reject"));
        actions.add(approve); actions.add(revise); actions.add(reject);
        right.add(actions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        tools.add(new JLabel("Orders awaiting approval:"));
        tools.add(refresh);

        add(tools, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    private JButton styled(String label, Color bg, java.awt.event.ActionListener a) {
        JButton b = new JButton(label);
        b.setBackground(bg); b.setForeground(Constants.TEXT_LIGHT);
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.addActionListener(a);
        return b;
    }

    private void act(String what) {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        String id = (String) model.getValueAt(r, 0);
        switch (what) {
            case "approve": controller.approve(this, id, this::refresh); break;
            case "reject":
                String why = JOptionPane.showInputDialog(this, "Reason for rejection:");
                if (why != null) controller.reject(this, id, this::refresh);
                break;
            case "revise":
                String note = JOptionPane.showInputDialog(this, "Revision notes:");
                if (note != null) controller.sendForRevision(this, id, this::refresh);
                break;
        }
    }

    private void showDetail() {
        int r = table.getSelectedRow();
        if (r < 0) { detail.setText(""); return; }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < model.getColumnCount(); i++) {
            sb.append(String.format("%-12s: %s%n", model.getColumnName(i), model.getValueAt(r, i)));
        }
        detail.setText(sb.toString());
    }

    @Override
    public void refresh() { controller.loadOrders(this, OrderDTO.PENDING, null); }

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        model.setRowCount(0);
        for (OrderDTO o : orders) {
            if (!OrderDTO.PENDING.equals(o.getStatus()) && !OrderDTO.REVISION.equals(o.getStatus()))
                continue;
            model.addRow(new Object[]{
                    o.getOrderId(), o.getCustomerName(), o.getCarModel(),
                    "\u20B9 " + INR.format(o.getAmount()),
                    o.getDate(), o.getStatus()
            });
        }
    }

    @Override public void onOrderChanged(OrderDTO o) { refresh(); }
}
