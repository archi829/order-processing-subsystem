package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.OrderDTO;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Payment panel.
 *
 * FIX 1: pay(owner, id, amt, simulateFail, after) → 5-arg form.
 * FIX 2: loadOrders() → 3-arg form.
 * FIX 3: onStatsLoaded(Map<String,Object>).
 */
public class OrderPaymentPanel extends JPanel
        implements OrderController.OrderListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"Order ID", "Customer", "Amount", "Paid", "Outstanding", "Payment"};
    private static final NumberFormat INR = NumberFormat.getInstance(new Locale("en", "IN"));

    private final OrderController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextArea invoice = new JTextArea(10, 30);
    private final JTextField payAmount = new JTextField();
    private final JComboBox<String> payMethod = new JComboBox<>(new String[]{
            "Bank Transfer (NEFT)", "RTGS", "UPI", "Cheque", "Credit"});
    private final JCheckBox simulateFail = new JCheckBox("Simulate payment failure");

    private final Map<String, OrderDTO> cache = new HashMap<>();

    public OrderPaymentPanel(OrderController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Constants.TEXT_LIGHT);
        table.getSelectionModel().addListSelectionListener(
                e -> { if (!e.getValueIsAdjusting()) renderInvoice(); });
        JScrollPane left = new JScrollPane(table);

        invoice.setEditable(false);
        invoice.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setBackground(Constants.BG_WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        JLabel tt = new JLabel(" Invoice Preview");
        tt.setFont(Constants.FONT_HEADING);
        right.add(tt, BorderLayout.NORTH);
        right.add(new JScrollPane(invoice), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Constants.BG_WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6); c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Amount"), c);
        c.gridx = 1; form.add(payAmount, c);
        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Method"), c);
        c.gridx = 1; form.add(payMethod, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; form.add(simulateFail, c);
        JButton pay = new JButton("Record Payment");
        pay.setBackground(Constants.SUCCESS_COLOR); pay.setForeground(Constants.TEXT_LIGHT);
        pay.addActionListener(e -> record());
        c.gridx = 0; c.gridy = 3; form.add(pay, c);
        right.add(form, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        tools.add(new JLabel("Orders with outstanding balance:"));
        tools.add(refreshBtn);

        add(tools, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    private void record() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        String id = (String) model.getValueAt(r, 0);
        BigDecimal amt;
        try { amt = new BigDecimal(payAmount.getText().trim()); }
        catch (Exception ex) {
            ExceptionHandler.handle(this, new ValidationException(
                    ValidationException.INVALID_REORDER_QUANTITY, "Amount must be numeric.", payAmount));
            return;
        }
        if (amt.signum() <= 0) {
            ExceptionHandler.handle(this, new ValidationException(
                    ValidationException.INVALID_REORDER_QUANTITY, "Amount must be positive.", payAmount));
            return;
        }

        // FIX: 5-arg pay(owner, id, amt, simulateFail, after)
        controller.pay(this, id, amt, simulateFail.isSelected(), () -> {
            payAmount.setText("");
            simulateFail.setSelected(false);
            OrderDTO o = cache.get(id);
            if (o != null && OrderDTO.PAY_FAILED.equals(o.getPaymentStatus())) {
                int retry = JOptionPane.showConfirmDialog(this,
                        "Payment failed. Retry with the same amount?",
                        "Payment Failed", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (retry == JOptionPane.YES_OPTION) {
                    // FIX: 5-arg pay (no simulate)
                    controller.pay(this, id, amt, false, this::refresh);
                }
            }
            refresh();
        });
    }

    private void renderInvoice() {
        int r = table.getSelectedRow();
        if (r < 0) { invoice.setText(""); return; }
        String id = (String) model.getValueAt(r, 0);
        OrderDTO o = cache.get(id);
        if (o == null) return;
        BigDecimal amt  = o.getAmount();
        BigDecimal paid = o.getAmountPaid() == null ? BigDecimal.ZERO : o.getAmountPaid();
        BigDecimal tax  = amt.multiply(new BigDecimal("0.18")).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal sub  = amt.subtract(tax);
        BigDecimal due  = amt.subtract(paid);
        String tpl = "  ===== INVOICE =====%n" +
                     "  Order:       %s%n  Customer:    %s%n  Vehicle:     %s (%s)%n  VIN:         %s%n" +
                     "  -----------------------------------%n" +
                     "  Subtotal:    \u20B9 %s%n  GST @ 18%%:   \u20B9 %s%n  Total:       \u20B9 %s%n" +
                     "  Paid:        \u20B9 %s%n  Outstanding: \u20B9 %s%n" +
                     "  -----------------------------------%n  Status:      %s%n";
        invoice.setText(String.format(tpl,
                o.getOrderId(), o.getCustomerName(), o.getCarModel(), o.getChassisType(), o.getCarVIN(),
                INR.format(sub), INR.format(tax), INR.format(amt),
                INR.format(paid), INR.format(due), o.getPaymentStatus()));
    }

    // FIX: 3-arg loadOrders
    @Override
    public void refresh() { controller.loadOrders(this, null, null); }

    @Override
    public void onOrdersLoaded(List<OrderDTO> orders) {
        cache.clear();
        model.setRowCount(0);
        for (OrderDTO o : orders) {
            if (OrderDTO.CANCELLED.equals(o.getStatus())) continue;
            cache.put(o.getOrderId(), o);
            BigDecimal paid = o.getAmountPaid() == null ? BigDecimal.ZERO : o.getAmountPaid();
            BigDecimal due  = o.getAmount().subtract(paid);
            model.addRow(new Object[]{
                    o.getOrderId(), o.getCustomerName(),
                    "\u20B9 " + INR.format(o.getAmount()),
                    "\u20B9 " + INR.format(paid),
                    "\u20B9 " + INR.format(due),
                    o.getPaymentStatus()
            });
        }
    }

    // FIX: Map<String,Object>
    @Override
    public void onStatsLoaded(Map<String, Object> stats) { /* not used */ }

    @Override public void onOrderChanged(OrderDTO o) { refresh(); }
}