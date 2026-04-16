package com.erp.view.panels.hr;

import com.erp.controller.HRController;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.ValidationException;
import com.erp.model.dto.EmployeeDTO;
import com.erp.util.Constants;
import com.erp.view.components.FakeChartPanel;
import com.erp.view.panels.orders.OrdersHomePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performance reviews + workforce planning.
 * Rate 1-5, write feedback, track promotion status, visualise department distribution.
 */
public class PerformancePanel extends JPanel
        implements HRController.HRListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"ID", "Name", "Dept", "Role", "Rating", "Feedback", "Promotion"};

    private final HRController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final FakeChartPanel chart = new FakeChartPanel(
            "Workforce by Department", FakeChartPanel.Style.BAR);
    private final Map<String, EmployeeDTO> cache = new HashMap<>();

    public PerformancePanel(HRController controller) {
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

        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Constants.BG_WHITE);
        right.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        JLabel t = new JLabel(" Workforce Planning");
        t.setFont(Constants.FONT_HEADING);
        right.add(t, BorderLayout.NORTH);
        right.add(chart, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.65);
        split.setBorder(null);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setOpaque(false);
        JButton review = new JButton("Appraise Selected");
        review.setBackground(Constants.PRIMARY_COLOR); review.setForeground(Constants.TEXT_LIGHT);
        review.addActionListener(e -> appraise());
        JButton promote = new JButton("Recommend Promotion");
        promote.setBackground(Constants.SUCCESS_COLOR); promote.setForeground(Constants.TEXT_LIGHT);
        promote.addActionListener(e -> setPromotion("Recommended"));
        JButton hold = new JButton("Hold Review");
        hold.setBackground(Constants.WARNING_COLOR); hold.setForeground(Constants.TEXT_LIGHT);
        hold.addActionListener(e -> setPromotion("On Hold"));
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        actions.add(review); actions.add(promote); actions.add(hold); actions.add(refresh);

        add(new JLabel("Active employees eligible for appraisals:"), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        refresh();
    }

    private EmployeeDTO selected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return null;
        }
        return cache.get((String) model.getValueAt(r, 0));
    }

    private void appraise() {
        EmployeeDTO e = selected();
        if (e == null) return;
        JComboBox<Integer> rating = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        rating.setSelectedItem(e.getPerformanceRating() == null ? 3 : e.getPerformanceRating());
        JTextArea fb = new JTextArea(e.getPerformanceFeedback() == null ? "" : e.getPerformanceFeedback(), 5, 28);

        JPanel form = new JPanel(new BorderLayout(6, 6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Rating (1-5):")); top.add(rating);
        form.add(top, BorderLayout.NORTH);
        form.add(new JScrollPane(fb), BorderLayout.CENTER);

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Appraise " + e.getEmployeeId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        e.setPerformanceRating((Integer) rating.getSelectedItem());
        e.setPerformanceFeedback(fb.getText());
        controller.updateEmployee(this, e, u -> refresh());
    }

    private void setPromotion(String status) {
        EmployeeDTO e = selected();
        if (e == null) return;
        e.setPromotionStatus(status);
        controller.updateEmployee(this, e, u -> refresh());
    }

    @Override public void refresh() { controller.loadPerformance(this); }

    @Override
    public void onPerformanceLoaded(List<EmployeeDTO> list) {
        cache.clear();
        model.setRowCount(0);
        Map<String, Integer> byDept = new HashMap<>();
        for (EmployeeDTO e : list) {
            cache.put(e.getEmployeeId(), e);
            byDept.merge(e.getDepartment() == null ? "Other" : e.getDepartment(), 1, Integer::sum);
            model.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(), e.getDepartment(), e.getRole(),
                    e.getPerformanceRating() == null ? "-" : e.getPerformanceRating(),
                    e.getPerformanceFeedback() == null ? "" : e.getPerformanceFeedback(),
                    e.getPromotionStatus() == null ? "Stable" : e.getPromotionStatus()
            });
        }
        chart.setData(byDept);
    }

    @Override public void onEmployeeChanged(EmployeeDTO e) { refresh(); }
}
