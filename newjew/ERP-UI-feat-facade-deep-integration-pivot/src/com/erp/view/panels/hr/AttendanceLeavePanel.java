package com.erp.view.panels.hr;

import com.erp.controller.HRController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.util.Constants;
import com.erp.view.panels.orders.OrdersHomePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Combined Attendance + Leave management (two inner tabs).
 * Attendance: log daily check-in/out + overtime.
 * Leave: approve/reject pending requests.
 */
public class AttendanceLeavePanel extends JPanel
        implements HRController.HRListener, OrdersHomePanel.Refreshable {

    private final HRController controller;

    private static final String[] ATT_COLS = {"Log ID", "Employee", "Check-In", "Check-Out", "Overtime"};
    private static final String[] LV_COLS  = {"Leave ID", "Employee", "Type", "From", "To", "Status"};

    private final DefaultTableModel attModel = new DefaultTableModel(ATT_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel lvModel = new DefaultTableModel(LV_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable attTable = new JTable(attModel);
    private final JTable lvTable  = new JTable(lvModel);

    private final JTextField empId = new JTextField(10);
    private final JTextField checkIn = new JTextField(6);
    private final JTextField checkOut = new JTextField(6);
    private final JTextField overtime = new JTextField(6);

    public AttendanceLeavePanel(HRController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
        tabs.addTab("Attendance", buildAttendanceTab());
        tabs.addTab("Leave",      buildLeaveTab());
        add(tabs, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildAttendanceTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Constants.BG_LIGHT);
        styleTable(attTable);
        p.add(new JScrollPane(attTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 8));
        form.setBackground(Constants.BG_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        form.add(new JLabel("Employee ID:")); form.add(empId);
        form.add(new JLabel("In:"));          form.add(checkIn);
        form.add(new JLabel("Out:"));         form.add(checkOut);
        form.add(new JLabel("Overtime:"));    form.add(overtime);
        JButton log = new JButton("Log Attendance");
        log.setBackground(Constants.PRIMARY_COLOR); log.setForeground(Constants.TEXT_LIGHT);
        log.addActionListener(e -> logAttendance());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> controller.loadAttendance(this));
        form.add(log); form.add(refresh);
        p.add(form, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildLeaveTab() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Constants.BG_LIGHT);
        styleTable(lvTable);
        p.add(new JScrollPane(lvTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(Constants.BG_WHITE);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JButton approve = new JButton("Approve");
        approve.setBackground(Constants.SUCCESS_COLOR); approve.setForeground(Constants.TEXT_LIGHT);
        approve.addActionListener(e -> leaveAction("APPROVED"));
        JButton reject = new JButton("Reject");
        reject.setBackground(Constants.DANGER_COLOR); reject.setForeground(Constants.TEXT_LIGHT);
        reject.addActionListener(e -> leaveAction("REJECTED"));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> controller.loadLeave(this));
        actions.add(approve); actions.add(reject); actions.add(refresh);
        p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void styleTable(JTable t) {
        t.setFont(Constants.FONT_REGULAR);
        t.setRowHeight(26);
        t.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        t.getTableHeader().setForeground(Constants.TEXT_LIGHT);
    }

    private void logAttendance() {
        String id = empId.getText().trim();
        if (id.isEmpty()) {
            ExceptionHandler.handle(this, ValidationException.requiredField("Employee ID", empId));
            return;
        }
        controller.logAttendance(this, id, checkIn.getText().trim(),
                checkOut.getText().trim(), overtime.getText().trim(), () -> {
            empId.setText(""); checkIn.setText(""); checkOut.setText(""); overtime.setText("");
            controller.loadAttendance(this);
        });
    }

    private void leaveAction(String action) {
        int r = lvTable.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(lvTable));
            return;
        }
        String id = (String) lvModel.getValueAt(r, 0);
        controller.leaveAction(this, id, action, () -> controller.loadLeave(this));
    }

    @Override
    public void refresh() {
        controller.loadAttendance(this);
        controller.loadLeave(this);
    }

    @Override
    public void onAttendanceLoaded(List<String[]> rows) {
        attModel.setRowCount(0);
        for (String[] r : rows) attModel.addRow(r);
    }

    @Override
    public void onLeaveLoaded(List<String[]> rows) {
        lvModel.setRowCount(0);
        for (String[] r : rows) lvModel.addRow(r);
    }
}
