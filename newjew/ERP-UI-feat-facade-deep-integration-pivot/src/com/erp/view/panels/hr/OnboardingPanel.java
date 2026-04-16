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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Onboarding checklist: background check, document verification,
 * orientation completion. Activates a new employee when all three are green.
 */
public class OnboardingPanel extends JPanel
        implements HRController.HRListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"ID", "Name", "Role", "Department",
            "Background", "Documents", "Orientation", "Status"};

    private final HRController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final Map<String, EmployeeDTO> cache = new HashMap<>();

    public OnboardingPanel(HRController controller) {
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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(Constants.BG_WHITE);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        actions.add(styled("Mark Background OK",   Constants.SUCCESS_COLOR, e -> mark("bg")));
        actions.add(styled("Mark Documents OK",    Constants.SUCCESS_COLOR, e -> mark("doc")));
        actions.add(styled("Complete Orientation", Constants.PRIMARY_COLOR, e -> mark("orient")));
        actions.add(styled("Activate Employee",    Constants.ACCENT_COLOR,  e -> activate()));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        actions.add(refresh);

        add(new JLabel("Employees in onboarding pipeline:"), BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        refresh();
    }

    private JButton styled(String label, Color bg, java.awt.event.ActionListener a) {
        JButton b = new JButton(label);
        b.setBackground(bg); b.setForeground(Constants.TEXT_LIGHT);
        b.setFocusPainted(false); b.addActionListener(a);
        return b;
    }

    private EmployeeDTO selected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return null;
        }
        return cache.get((String) model.getValueAt(r, 0));
    }

    private void mark(String what) {
        EmployeeDTO e = selected();
        if (e == null) return;
        switch (what) {
            case "bg":     e.setBackgroundCheckPassed(true); break;
            case "doc":    e.setDocumentsVerified(true); break;
            case "orient": e.setOnboardingVerified(true); break;
        }
        controller.updateOnboarding(this, e, this::refresh);
    }

    private void activate() {
        EmployeeDTO e = selected();
        if (e == null) return;
        if (!(e.isBackgroundCheckPassed() && e.isDocumentsVerified() && e.isOnboardingVerified())) {
            JOptionPane.showMessageDialog(this,
                    "All three checks must be green before activation.",
                    "Incomplete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        e.setStatus(EmployeeDTO.STATUS_ACTIVE);
        controller.updateOnboarding(this, e, this::refresh);
    }

    @Override public void refresh() { controller.loadOnboarding(this); }

    @Override
    public void onOnboardingLoaded(List<EmployeeDTO> list) {
        model.setRowCount(0);
        cache.clear();
        for (EmployeeDTO e : list) {
            cache.put(e.getEmployeeId(), e);
            model.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(), e.getRole(), e.getDepartment(),
                    mark(e.isBackgroundCheckPassed()),
                    mark(e.isDocumentsVerified()),
                    mark(e.isOnboardingVerified()),
                    e.getStatus()
            });
        }
    }

    private static String mark(boolean v) { return v ? "OK" : "Pending"; }

    @Override public void onEmployeeChanged(EmployeeDTO e) { refresh(); }
}
