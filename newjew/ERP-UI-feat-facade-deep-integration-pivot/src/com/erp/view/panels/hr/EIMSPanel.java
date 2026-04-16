package com.erp.view.panels.hr;

import com.erp.controller.HRController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.EmployeeDTO;
import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.panels.orders.OrdersHomePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Employee Information Management System (EIMS):
 * stats + filters + editable employee details.
 */
public class EIMSPanel extends JPanel
        implements HRController.HRListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {
            "Employee ID", "Name", "Role", "Department",
            "Assembly Line", "Shift", "Hire Date", "Status"
    };

    private final HRController controller;
    private final DashboardCard total    = new DashboardCard("Total Workforce", "-", "all employees", Constants.PRIMARY_COLOR);
    private final DashboardCard active   = new DashboardCard("Active",          "-", "on duty",       Constants.SUCCESS_COLOR);
    private final DashboardCard newJoiners = new DashboardCard("New Joiners",   "-", "onboarding",    Constants.ACCENT_COLOR);
    private final DashboardCard onLeave  = new DashboardCard("On Leave",        "-", "away",          Constants.WARNING_COLOR);

    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField search = new JTextField(18);
    private final JComboBox<String> dept = new JComboBox<>(new String[]{
            "", "Manufacturing", "Quality", "Assembly", "R&D", "HR", "Sales", "Finance"});
    private final JComboBox<String> status = new JComboBox<>(new String[]{
            "", EmployeeDTO.STATUS_ACTIVE, EmployeeDTO.STATUS_NEW,
            EmployeeDTO.STATUS_ON_LEAVE, EmployeeDTO.STATUS_TERMINATED});

    public EIMSPanel(HRController controller) {
        this.controller = controller;
        controller.addListener(this);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        add(buildStats(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.add(total); row.add(active); row.add(newJoiners); row.add(onLeave);
        return row;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tools.setOpaque(false);
        tools.add(new JLabel("Search:")); tools.add(search);
        tools.add(new JLabel("Dept:"));   tools.add(dept);
        tools.add(new JLabel("Status:")); tools.add(status);
        JButton apply = new JButton("Apply"); apply.addActionListener(e -> refresh());
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> { search.setText(""); dept.setSelectedIndex(0); status.setSelectedIndex(0); refresh(); });
        tools.add(apply); tools.add(clear);

        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.setRowSorter(new TableRowSorter<>(model));
        table.getTableHeader().setBackground(Constants.PRIMARY_COLOR);
        table.getTableHeader().setForeground(Constants.TEXT_LIGHT);
        table.getTableHeader().setFont(Constants.FONT_HEADING);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        body.add(tools, BorderLayout.NORTH);
        body.add(sp, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);
        JButton edit = new JButton("Edit Selected");
        edit.setBackground(Constants.PRIMARY_COLOR); edit.setForeground(Constants.TEXT_LIGHT);
        edit.addActionListener(e -> editSelected());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        p.add(edit); p.add(refresh);
        return p;
    }

    private void editSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        int row = table.convertRowIndexToModel(r);
        String id = (String) model.getValueAt(row, 0);

        JTextField roleF = new JTextField((String) model.getValueAt(row, 2));
        JTextField deptF = new JTextField((String) model.getValueAt(row, 3));
        JTextField lineF = new JTextField((String) model.getValueAt(row, 4));
        JTextField shiftF = new JTextField((String) model.getValueAt(row, 5));
        JComboBox<String> statusF = new JComboBox<>(new String[]{
                EmployeeDTO.STATUS_ACTIVE, EmployeeDTO.STATUS_ON_LEAVE,
                EmployeeDTO.STATUS_NEW, EmployeeDTO.STATUS_TERMINATED});
        statusF.setSelectedItem(model.getValueAt(row, 7));

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Role"));         form.add(roleF);
        form.add(new JLabel("Department"));   form.add(deptF);
        form.add(new JLabel("Assembly Line")); form.add(lineF);
        form.add(new JLabel("Shift"));        form.add(shiftF);
        form.add(new JLabel("Status"));       form.add(statusF);

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Edit " + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(id);
        dto.setName((String) model.getValueAt(row, 1));
        dto.setRole(roleF.getText().trim());
        dto.setDepartment(deptF.getText().trim());
        dto.setAssignedAssemblyLine(lineF.getText().trim());
        dto.setShiftSchedule(shiftF.getText().trim());
        dto.setStatus((String) statusF.getSelectedItem());
        Object hd = model.getValueAt(row, 6);
        try { dto.setHireDate(LocalDate.parse(String.valueOf(hd))); } catch (Exception ignored) {}
        controller.updateEmployee(this, dto, updated -> refresh());
    }

    @Override
    public void refresh() {
        String d = (String) dept.getSelectedItem();
        String s = (String) status.getSelectedItem();
        String q = search.getText().trim();
        controller.loadEmployees(this,
                d == null || d.isEmpty() ? null : d,
                s == null || s.isEmpty() ? null : s,
                q.isEmpty() ? null : q);
        controller.loadStats(this);
    }

    @Override
    public void onEmployeesLoaded(List<EmployeeDTO> list) {
        model.setRowCount(0);
        for (EmployeeDTO e : list) {
            model.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(), e.getRole(), e.getDepartment(),
                    e.getAssignedAssemblyLine(), e.getShiftSchedule(),
                    e.getHireDate() == null ? "" : e.getHireDate().toString(),
                    e.getStatus()
            });
        }
    }

    @Override
    public void onStatsLoaded(Map<String, Integer> s) {
        total.setValue(String.valueOf(s.getOrDefault("total", 0)));
        active.setValue(String.valueOf(s.getOrDefault("active", 0)));
        newJoiners.setValue(String.valueOf(s.getOrDefault("newJoiners", 0)));
        onLeave.setValue(String.valueOf(s.getOrDefault("onLeave", 0)));
    }

    @Override public void onEmployeeChanged(EmployeeDTO e) { refresh(); }
}
