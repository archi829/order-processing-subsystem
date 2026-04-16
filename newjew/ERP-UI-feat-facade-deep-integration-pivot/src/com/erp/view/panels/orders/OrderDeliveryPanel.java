package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.integration.MockUIService;
import com.erp.integration.ServiceLocator;
import com.erp.model.dto.OrderDTO;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Delivery & tracking: Approved orders get shipped with courier + tracking;
 * In-transit orders show shipment info and activity timeline.
 */
public class OrderDeliveryPanel extends JPanel
        implements OrderController.OrderListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"Order ID", "Customer", "Model", "Status", "Courier", "Tracking #"};

    private final OrderController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextArea timeline = new JTextArea(8, 30);
    private final JComboBox<String> courier = new JComboBox<>(new String[]{
            "BlueDart Auto", "Delhivery Heavy", "Gati KWE", "DTDC Auto"});
    private final JTextField tracking = new JTextField();

    public OrderDeliveryPanel(OrderController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Constants.TEXT_LIGHT);
        JScrollPane left = new JScrollPane(table);

        timeline.setEditable(false);
        timeline.setFont(new Font("Monospaced", Font.PLAIN, 12));
        timeline.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setBackground(Constants.BG_WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Constants.BG_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4); c.anchor = GridBagConstraints.WEST; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Courier"), c);
        c.gridx = 1; form.add(courier, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Tracking #"), c);
        c.gridx = 1; form.add(tracking, c);
        JButton ship = new JButton("Create Shipment");
        ship.setBackground(Constants.PRIMARY_COLOR); ship.setForeground(Constants.TEXT_LIGHT);
        ship.addActionListener(e -> ship());
        c.gridx = 1; c.gridy = 2; form.add(ship, c);

        JLabel tlTitle = new JLabel(" Activity Log");
        tlTitle.setFont(Constants.FONT_HEADING);

        right.add(form, BorderLayout.NORTH);
        right.add(tlTitle, BorderLayout.CENTER);
        JScrollPane tlScroll = new JScrollPane(timeline);
        tlScroll.setBorder(BorderFactory.createEmptyBorder());
        right.add(tlScroll, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.65);
        split.setBorder(null);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        tools.add(new JLabel("Approved & in-transit orders:"));
        tools.add(refresh);

        add(tools, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    private void ship() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        String id = (String) model.getValueAt(r, 0);
        String trk = tracking.getText().trim();
        if (trk.isEmpty()) trk = "BD" + (100000 + (int)(Math.random() * 9000));
        controller.ship(this, id, (String) courier.getSelectedItem(), trk, this::refresh);
        tracking.setText("");
    }

    @Override
    public void refresh() {
        controller.loadOrders(this, null, null);
        updateTimeline();
    }

    private void updateTimeline() {
        timeline.setText("");
        if (ServiceLocator.getUIService() instanceof MockUIService) {
            for (String[] row : ((MockUIService) ServiceLocator.getUIService()).getActivityLog()) {
                timeline.append("[" + row[0].substring(0, 19) + "]  " + row[1] + "\n");
            }
        }
    }

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        model.setRowCount(0);
        for (OrderDTO o : orders) {
            if (!OrderDTO.APPROVED.equals(o.getStatus()) && !OrderDTO.IN_TRANSIT.equals(o.getStatus())
                    && !OrderDTO.DELIVERED.equals(o.getStatus())) continue;
            model.addRow(new Object[]{
                    o.getOrderId(), o.getCustomerName(), o.getCarModel(),
                    o.getStatus(),
                    o.getCourier() == null ? "-" : o.getCourier(),
                    o.getTrackingNumber() == null ? "-" : o.getTrackingNumber()
            });
        }
    }

    @Override public void onOrderChanged(OrderDTO o) { refresh(); }
}
