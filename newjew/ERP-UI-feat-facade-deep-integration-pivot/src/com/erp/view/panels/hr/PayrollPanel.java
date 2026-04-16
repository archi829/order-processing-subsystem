package com.erp.view.panels.hr;

import com.erp.controller.HRController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.EmployeeDTO;
import com.erp.util.Constants;
import com.erp.view.panels.orders.OrdersHomePanel;

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
 * Payroll: gross salary, deductions, tax, net pay.
 * Generate payslip + simulate bank transfer.
 */
public class PayrollPanel extends JPanel
        implements HRController.HRListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"ID", "Name", "Dept", "Gross", "Deductions", "Tax", "Net Pay"};
    private static final NumberFormat INR = NumberFormat.getInstance(new Locale("en", "IN"));

    private final HRController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextArea payslip = new JTextArea(14, 34);
    private final Map<String, EmployeeDTO> cache = new HashMap<>();

    public PayrollPanel(HRController controller) {
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
            if (!e.getValueIsAdjusting()) renderPayslip();
        });
        JScrollPane left = new JScrollPane(table);

        payslip.setEditable(false);
        payslip.setFont(new Font("Monospaced", Font.PLAIN, 12));
        payslip.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JPanel right = new JPanel(new BorderLayout(0, 6));
        right.setBackground(Constants.BG_WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        JLabel t = new JLabel(" Payslip Preview");
        t.setFont(Constants.FONT_HEADING);
        right.add(t, BorderLayout.NORTH);
        right.add(new JScrollPane(payslip), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        actions.setBackground(Constants.BG_WHITE);
        JButton transfer = new JButton("Transfer Salary");
        transfer.setBackground(Constants.SUCCESS_COLOR); transfer.setForeground(Constants.TEXT_LIGHT);
        transfer.addActionListener(e -> transfer());
        JButton print = new JButton("Print Payslip");
        print.addActionListener(e -> JOptionPane.showMessageDialog(this,
                payslip.getText().isEmpty() ? "Select an employee first." : "Payslip sent to printer (simulated).",
                "Print", JOptionPane.INFORMATION_MESSAGE));
        actions.add(transfer); actions.add(print);
        right.add(actions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.6);
        split.setBorder(null);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tools.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        tools.add(new JLabel("Payroll register (monthly):"));
        tools.add(refresh);

        add(tools, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        refresh();
    }

    private void transfer() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        String id = (String) model.getValueAt(r, 0);
        int ok = JOptionPane.showConfirmDialog(this,
                "Transfer this month's net pay to " + id + "'s bank account?",
                "Confirm Transfer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) {
            controller.transferSalary(this, id, () -> JOptionPane.showMessageDialog(this,
                    "Salary transferred successfully for " + id,
                    "Success", JOptionPane.INFORMATION_MESSAGE));
        }
    }

    private void renderPayslip() {
        int r = table.getSelectedRow();
        if (r < 0) { payslip.setText(""); return; }
        EmployeeDTO e = cache.get((String) model.getValueAt(r, 0));
        if (e == null) return;
        BigDecimal g = nz(e.getGrossSalary());
        BigDecimal d = nz(e.getDeductions());
        BigDecimal t = nz(e.getTaxRecord());
        BigDecimal n = nz(e.getNetPay());
        String tpl = "  ===== PAYSLIP =====%n" +
                     "  Employee:    %s%n" +
                     "  ID:          %s%n" +
                     "  Role:        %s%n" +
                     "  Department:  %s%n" +
                     "  -----------------------------------%n" +
                     "  Gross:       \u20B9 %s%n" +
                     "  Deductions:  \u20B9 %s%n" +
                     "  Tax:         \u20B9 %s%n" +
                     "  -----------------------------------%n" +
                     "  Net Pay:     \u20B9 %s%n";
        payslip.setText(String.format(tpl,
                e.getName(), e.getEmployeeId(), e.getRole(), e.getDepartment(),
                INR.format(g), INR.format(d), INR.format(t), INR.format(n)));
    }

    private static BigDecimal nz(BigDecimal b) { return b == null ? BigDecimal.ZERO : b; }

    @Override public void refresh() { controller.loadPayroll(this); }

    @Override
    public void onPayrollLoaded(List<EmployeeDTO> list) {
        cache.clear();
        model.setRowCount(0);
        for (EmployeeDTO e : list) {
            cache.put(e.getEmployeeId(), e);
            model.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(), e.getDepartment(),
                    "\u20B9 " + INR.format(nz(e.getGrossSalary())),
                    "\u20B9 " + INR.format(nz(e.getDeductions())),
                    "\u20B9 " + INR.format(nz(e.getTaxRecord())),
                    "\u20B9 " + INR.format(nz(e.getNetPay()))
            });
        }
    }

    @Override public void onEmployeeChanged(EmployeeDTO e) { refresh(); }
}
