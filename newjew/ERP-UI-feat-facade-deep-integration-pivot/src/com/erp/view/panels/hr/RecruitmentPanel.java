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
import java.util.List;

/**
 * Applicant Tracking System (ATS): move candidates through the pipeline
 * APPLIED -> SHORTLISTED -> INTERVIEW -> SELECTED / REJECTED.
 */
public class RecruitmentPanel extends JPanel
        implements HRController.HRListener, OrdersHomePanel.Refreshable {

    private static final String[] COLS = {"ID", "Name", "Role", "Department", "Stage", "Score"};
    private static final String[] STAGES = {"APPLIED", "SHORTLISTED", "INTERVIEW", "SELECTED", "REJECTED"};

    private final HRController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> stage = new JComboBox<>(STAGES);
    private final JTextField scoreField = new JTextField(6);

    public RecruitmentPanel(HRController controller) {
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

        JPanel funnel = buildFunnel();

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        form.setBackground(Constants.BG_WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        form.add(new JLabel("Move to stage:"));
        form.add(stage);
        form.add(new JLabel("Interview score:"));
        form.add(scoreField);
        JButton move = new JButton("Move Candidate");
        move.setBackground(Constants.PRIMARY_COLOR); move.setForeground(Constants.TEXT_LIGHT);
        move.addActionListener(e -> moveStage());
        form.add(move);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());
        form.add(refresh);

        add(funnel, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);
        refresh();
    }

    private final JLabel[] funnelCounts = new JLabel[STAGES.length];

    private JPanel buildFunnel() {
        JPanel p = new JPanel(new GridLayout(1, STAGES.length, 8, 0));
        p.setOpaque(false);
        Color[] tints = {Constants.PRIMARY_COLOR, new Color(52, 152, 219),
                Constants.WARNING_COLOR, Constants.SUCCESS_COLOR, Constants.DANGER_COLOR};
        for (int i = 0; i < STAGES.length; i++) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Constants.BG_WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, tints[i]),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            JLabel title = new JLabel(STAGES[i]);
            title.setFont(Constants.FONT_HEADING);
            JLabel value = new JLabel("0");
            value.setFont(new Font(Constants.FONT_REGULAR.getFamily(), Font.BOLD, 22));
            value.setForeground(tints[i]);
            card.add(title, BorderLayout.NORTH);
            card.add(value, BorderLayout.CENTER);
            funnelCounts[i] = value;
            p.add(card);
        }
        return p;
    }

    private void moveStage() {
        int r = table.getSelectedRow();
        if (r < 0) {
            ExceptionHandler.handle(this, ValidationException.noVinSelected(table));
            return;
        }
        String id = (String) model.getValueAt(r, 0);
        Integer score = null;
        String sc = scoreField.getText().trim();
        if (!sc.isEmpty()) {
            try { score = Integer.parseInt(sc); }
            catch (NumberFormatException nfe) {
                ExceptionHandler.handle(this,
                        new ValidationException(ValidationException.INVALID_REORDER_QUANTITY,
                                "Score must be a number (0-100).", scoreField));
                return;
            }
        }
        controller.moveRecruitmentStage(this, id, (String) stage.getSelectedItem(), score, () -> {
            scoreField.setText("");
            refresh();
        });
    }

    @Override
    public void refresh() { controller.loadRecruitment(this); }

    @Override
    public void onRecruitmentLoaded(List<EmployeeDTO> list) {
        model.setRowCount(0);
        int[] counts = new int[STAGES.length];
        for (EmployeeDTO e : list) {
            model.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(), e.getRole(), e.getDepartment(),
                    e.getRecruitmentStage(),
                    e.getInterviewScore() == null ? "-" : e.getInterviewScore()
            });
            for (int i = 0; i < STAGES.length; i++)
                if (STAGES[i].equals(e.getRecruitmentStage())) counts[i]++;
        }
        for (int i = 0; i < STAGES.length; i++) funnelCounts[i].setText(String.valueOf(counts[i]));
    }

    @Override public void onEmployeeChanged(EmployeeDTO e) { refresh(); }
}
