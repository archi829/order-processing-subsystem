package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.integration.MockUIService;
import com.erp.integration.ServiceLocator;
import com.erp.model.dto.OrderDTO;
import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Order dashboard panel.
 *
 * FIX 1: loadOrders() → 3-arg form.
 * FIX 2: onStatsLoaded(Map<String,Object>) to match OrderController.OrderListener.
 * FIX 3: @Override removed from the old onStatsLoaded(Map<String,Integer>) override
 *         that caused "does not override" — now there is only one onStatsLoaded.
 */
public class OrderDashboardPanel extends JPanel
        implements OrderController.OrderListener, OrdersHomePanel.Refreshable {

    private static final String[] COLUMNS = {
            "Order ID", "Customer", "VIN", "Model", "Chassis",
            "Amount", "Date", "Status", "Payment"
    };
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat INR = NumberFormat.getInstance(new Locale("en", "IN"));

    private final OrderController controller;
    private final DashboardCard total     = new DashboardCard("Total Orders", "-", "loading",         Constants.PRIMARY_COLOR);
    private final DashboardCard pending   = new DashboardCard("Pending",      "-", "awaiting approval",Constants.WARNING_COLOR);
    private final DashboardCard inTransit = new DashboardCard("In Transit",   "-", "on the road",      Constants.ACCENT_COLOR);
    private final DashboardCard delivered = new DashboardCard("Delivered",    "-", "fulfilled",         Constants.SUCCESS_COLOR);
    private final DashboardCard cancelled = new DashboardCard("Cancelled",    "-", "refunded",          Constants.DANGER_COLOR);

    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{
            "", OrderDTO.PENDING, OrderDTO.APPROVED, OrderDTO.REVISION, OrderDTO.REJECTED,
            OrderDTO.IN_TRANSIT, OrderDTO.DELIVERED, OrderDTO.CANCELLED});

    public OrderDashboardPanel(OrderController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        add(buildStats(),   BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 5, 10, 0));
        row.setOpaque(false);
        row.add(total); row.add(pending); row.add(inTransit);
        row.add(delivered); row.add(cancelled);
        return row;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tools.setOpaque(false);
        tools.add(new JLabel("Search:"));
        tools.add(searchField);
        tools.add(new JLabel("Status:"));
        tools.add(statusFilter);
        JButton apply = new JButton("Apply Filter");
        apply.addActionListener(e -> refresh());
        tools.add(apply);
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            searchField.setText(""); statusFilter.setSelectedIndex(0); refresh();
        });
        tools.add(clear);

        styleTable();
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));

        body.add(tools, BorderLayout.NORTH);
        body.add(sp,    BorderLayout.CENTER);
        return body;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JButton simulateFail = new JButton("Simulate Next Fail (retry demo)");
        simulateFail.addActionListener(e -> {
            if (ServiceLocator.getUIService() instanceof MockUIService) {
                ((MockUIService) ServiceLocator.getUIService()).setFailNext(true);
                JOptionPane.showMessageDialog(this,
                        "The next service call will fail once. Click Refresh to see the retry dialog.",
                        "Demo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton detailsBtn = new JButton("View Details");
        detailsBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
                return;
            }
            int row = table.convertRowIndexToModel(r);
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < model.getColumnCount(); i++) {
                msg.append(model.getColumnName(i)).append(": ").append(model.getValueAt(row, i)).append('\n');
            }
            JOptionPane.showMessageDialog(this, msg.toString(),
                    "Order " + model.getValueAt(row, 0), JOptionPane.INFORMATION_MESSAGE);
        });
        p.add(simulateFail); p.add(detailsBtn); p.add(refreshBtn);
        return p;
    }

    private void styleTable() {
        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setRowSorter(new TableRowSorter<>(model));
        table.setGridColor(new Color(230, 232, 236));
        table.getTableHeader().setFont(Constants.FONT_HEADING);
        table.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Constants.TEXT_LIGHT);
        table.getTableHeader().setReorderingAllowed(false);
    }

    // FIX: 3-arg loadOrders
    @Override
    public void refresh() {
        String status = (String) statusFilter.getSelectedItem();
        String q = searchField.getText().trim();
        controller.loadOrders(this,
                (status == null || status.isEmpty()) ? null : status,
                q.isEmpty() ? null : q);
        controller.loadStats(this);
    }

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        model.setRowCount(0);
        for (OrderDTO o : orders) {
            model.addRow(new Object[]{
                    o.getOrderId(), o.getCustomerName(), o.getCarVIN(), o.getCarModel(),
                    o.getChassisType(),
                    "\u20B9 " + INR.format(o.getAmount() == null ? BigDecimal.ZERO : o.getAmount()),
                    o.getDate() == null ? "" : o.getDate().format(DATE),
                    o.getStatus(), o.getPaymentStatus()
            });
        }
    }

    // FIX: Map<String,Object> — no @Override clash
    @Override
    public void onStatsLoaded(Map<String, Object> stats) {
        total.setValue(    String.valueOf(getInt(stats, "total")));
        pending.setValue(  String.valueOf(getInt(stats, "pending")));
        inTransit.setValue(String.valueOf(getInt(stats, "inTransit")));
        delivered.setValue(String.valueOf(getInt(stats, "delivered")));
        cancelled.setValue(String.valueOf(getInt(stats, "cancelled")));
    }

    @Override public void onOrderChanged(OrderDTO order) { refresh(); }

    private static int getInt(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return 0;
    }
}