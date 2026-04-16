package com.erp.view.panels.orders;

import com.erp.controller.OrderController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.OrderDTO;
import com.erp.util.Constants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Capture a new order (customer + car details + amount).
 * Validates required fields; simulates stock OK / stock-shortage flow.
 */
public class OrderEntryPanel extends JPanel {

    private final OrderController controller;
    private final JTextField customer  = new JTextField();
    private final JTextField vin       = new JTextField();
    private final JComboBox<String> model = new JComboBox<>(new String[]{
            "Model-S Sedan", "Model-X SUV", "Model-T Truck", "Model-EV Electric"});
    private final JComboBox<String> chassis = new JComboBox<>(new String[]{
            "Steel A1", "Alloy B2", "Reinforced", "Aluminum"});
    private final JTextField amount  = new JTextField();
    private final JTextArea  notes   = new JTextArea(3, 20);
    private final JLabel stockLabel = new JLabel(" ");
    private final JComboBox<String> stockMode = new JComboBox<>(new String[]{"Stock OK", "Stock Shortage"});

    public OrderEntryPanel(OrderController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Constants.BG_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int row = 0;
        addRow(form, c, row++, "Customer Name *", customer);
        addRow(form, c, row++, "Car VIN *",       vin);
        addRow(form, c, row++, "Car Model",       model);
        addRow(form, c, row++, "Chassis Type",    chassis);
        addRow(form, c, row++, "Amount (INR) *",  amount);
        addRow(form, c, row++, "Stock Check",     stockMode);

        c.gridx = 0; c.gridy = row; c.gridwidth = 1;
        form.add(new JLabel("Notes"), c);
        c.gridx = 1; c.gridy = row++; c.gridwidth = 2;
        notes.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 228)));
        form.add(new JScrollPane(notes), c);

        c.gridx = 1; c.gridy = row; c.gridwidth = 2;
        form.add(stockLabel, c);
        stockLabel.setFont(Constants.FONT_SMALL);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton reset = new JButton("Reset");
        reset.addActionListener(e -> clear());
        JButton submit = new JButton("Submit Order");
        submit.setBackground(Constants.PRIMARY_COLOR);
        submit.setForeground(Constants.TEXT_LIGHT);
        submit.addActionListener(e -> submit());
        actions.add(reset); actions.add(submit);

        add(new JLabel("Capture a new manufacturing order"), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.gridwidth = 1; c.weightx = 0;
        form.add(new JLabel(label), c);
        c.gridx = 1; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        form.add(field, c);
    }

    private void submit() {
        try {
            if (customer.getText().trim().isEmpty())
                throw ValidationException.requiredField("Customer Name", customer);
            if (vin.getText().trim().isEmpty())
                throw ValidationException.requiredField("Car VIN", vin);

            BigDecimal amt;
            try { amt = new BigDecimal(amount.getText().trim()); }
            catch (NumberFormatException nfe) {
                throw new ValidationException(ValidationException.INVALID_REORDER_QUANTITY,
                        "Amount must be a number.", amount);
            }
            if (amt.signum() <= 0)
                throw new ValidationException(ValidationException.INVALID_REORDER_QUANTITY,
                        "Amount must be greater than zero.", amount);

            if ("Stock Shortage".equals(stockMode.getSelectedItem())) {
                int ok = JOptionPane.showConfirmDialog(this,
                        "Parts inventory is short for this order.\nPlace order on back-order and proceed?",
                        "Stock Shortage", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ok != JOptionPane.YES_OPTION) return;
                stockLabel.setForeground(Constants.WARNING_COLOR);
                stockLabel.setText("Back-order flagged. Purchase requisition will be raised.");
            } else {
                stockLabel.setForeground(Constants.SUCCESS_COLOR);
                stockLabel.setText("Stock OK - parts reserved.");
            }

            OrderDTO dto = new OrderDTO();
            dto.setCustomerName(customer.getText().trim());
            dto.setCarVIN(vin.getText().trim());
            dto.setCarModel((String) model.getSelectedItem());
            dto.setChassisType((String) chassis.getSelectedItem());
            dto.setAmount(amt);
            dto.setDate(LocalDate.now());
            dto.setNotes(notes.getText());

            controller.createOrder(this, dto, created -> {
                JOptionPane.showMessageDialog(this,
                        "Order created: " + created.getOrderId() + "\nStatus: " + created.getStatus(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                clear();
            });
        } catch (ValidationException ve) {
            ExceptionHandler.handle(this, ve);
        }
    }

    private void clear() {
        customer.setText(""); vin.setText(""); amount.setText(""); notes.setText("");
        stockLabel.setText(" ");
    }
}
