package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.model.dto.GoodsReceiptDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * GRN creation form — a mismatch between received qty and expected qty
 * triggers {@code GOODS_RECEIPT_MISMATCH} at the mock layer.
 */
public class GoodsReceiptTab extends JPanel
        implements SupplyChainHomePanel.Refreshable {

    private final SupplyChainController controller;
    private final JTextField poId        = new JTextField("PO-1001", 12);
    private final JTextField expected    = new JTextField("100", 6);
    private final JTextField received    = new JTextField("100", 6);
    private final JTextField discrepancy = new JTextField("", 24);
    private final JComboBox<String> inspect = new JComboBox<>(new String[]{
            GoodsReceiptDTO.PASSED, GoodsReceiptDTO.FAILED, GoodsReceiptDTO.ON_HOLD});

    public GoodsReceiptTab(SupplyChainController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("PO ID:"));           form.add(poId);
        form.add(new JLabel("Expected Qty:"));    form.add(expected);
        form.add(new JLabel("Received Qty:"));    form.add(received);
        form.add(new JLabel("Inspection:"));      form.add(inspect);
        form.add(new JLabel("Discrepancies:"));   form.add(discrepancy);

        JButton submit = UIHelper.createPrimaryButton("Create GRN");
        submit.addActionListener(e -> submit());

        add(form, BorderLayout.NORTH);
        JLabel tip = new JLabel("<html><i>Qty mismatch triggers GOODS_RECEIPT_MISMATCH.</i></html>");
        tip.setForeground(Constants.TEXT_SECONDARY);
        add(tip, BorderLayout.CENTER);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);
        south.add(submit);
        add(south, BorderLayout.SOUTH);
    }

    private void submit() {
        try {
            int exp = Integer.parseInt(expected.getText().trim());
            int rec = Integer.parseInt(received.getText().trim());
            if (rec < 0) { UIHelper.showError(this, "Received quantity cannot be negative."); return; }
            GoodsReceiptDTO dto = new GoodsReceiptDTO(
                    "GRN-" + System.currentTimeMillis() % 100000,
                    poId.getText().trim(), LocalDate.now(),
                    rec, exp, (String) inspect.getSelectedItem(),
                    discrepancy.getText().trim());
            controller.createGoodsReceipt(this, dto, created ->
                    UIHelper.showSuccess(this, "GRN recorded: " + created.getGrnId()));
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Quantities must be integers.");
        }
    }

    @Override public void refresh() { /* form tab */ }
}
