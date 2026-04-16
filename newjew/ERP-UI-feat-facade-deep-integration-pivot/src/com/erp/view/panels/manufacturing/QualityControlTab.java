package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.QCCheckDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * QC submission form — posts a {@link QCCheckDTO}; if defect rate exceeds
 * the mock's 5% threshold, backend raises {@code QC_DEFECT_THRESHOLD_EXCEEDED}.
 */
public class QualityControlTab extends JPanel
        implements ManufacturingHomePanel.Refreshable {

    private final ManufacturingController controller;

    private final JTextField orderId    = new JTextField("PO-1001", 12);
    private final JTextField sampleSize = new JTextField("100", 6);
    private final JTextField defects    = new JTextField("2", 6);
    private final JTextField inspector  = new JTextField("INSP-01", 8);

    public QualityControlTab(ManufacturingController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Production Order ID:")); form.add(orderId);
        form.add(new JLabel("Sample Size:"));         form.add(sampleSize);
        form.add(new JLabel("Defects Found:"));       form.add(defects);
        form.add(new JLabel("Inspector ID:"));        form.add(inspector);

        JButton submit = UIHelper.createPrimaryButton("Submit QC Check");
        submit.addActionListener(e -> submit());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);
        south.add(submit);

        add(form, BorderLayout.NORTH);
        JLabel tip = new JLabel("<html><i>Submitting > 5% defect rate triggers "
                + "QC_DEFECT_THRESHOLD_EXCEEDED.</i></html>");
        tip.setForeground(Constants.TEXT_SECONDARY);
        add(tip, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void submit() {
        try {
            int sample = Integer.parseInt(sampleSize.getText().trim());
            int def = Integer.parseInt(defects.getText().trim());
            QCCheckDTO dto = new QCCheckDTO(
                    "QC-" + System.currentTimeMillis() % 100000,
                    orderId.getText().trim(),
                    LocalDate.now(),
                    sample, def, def == 0, inspector.getText().trim());
            controller.submitQCCheck(this, dto, saved ->
                    UIHelper.showSuccess(this, "QC recorded: " + saved.getQcCheckId()));
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Sample and defects must be integers.");
        }
    }

    @Override public void refresh() { /* pure form tab */ }
}
