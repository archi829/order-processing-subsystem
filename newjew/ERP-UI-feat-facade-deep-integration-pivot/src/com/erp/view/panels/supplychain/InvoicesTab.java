package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.model.dto.InvoiceDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Invoice create / verify / pay — amount mismatch triggers
 * {@code INVOICE_MISMATCH}; paying an un-authorised invoice is blocked.
 */
public class InvoicesTab extends JPanel
        implements SupplyChainHomePanel.Refreshable {

    private final SupplyChainController controller;

    private final JTextField invoiceId = new JTextField("INV-001", 10);
    private final JTextField supplierId = new JTextField("SUP-001", 10);
    private final JTextField poId = new JTextField("PO-1001", 10);
    private final JTextField grnId = new JTextField("GRN-001", 10);
    private final JTextField amount = new JTextField("250000", 10);
    private final JTextField verifyAmount = new JTextField("250000", 10);

    public InvoicesTab(SupplyChainController controller) {
        this.controller = controller;

        setLayout(new GridLayout(1, 3, 12, 0));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        add(buildCreatePanel());
        add(buildVerifyPanel());
        add(buildPayPanel());
    }

    private JPanel buildCreatePanel() {
        JPanel p = cardPanel("1. Create Invoice");
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Supplier ID:")); form.add(supplierId);
        form.add(new JLabel("PO ID:"));       form.add(poId);
        form.add(new JLabel("GRN ID:"));      form.add(grnId);
        form.add(new JLabel("Amount:"));      form.add(amount);
        p.add(form, BorderLayout.CENTER);

        JButton submit = UIHelper.createPrimaryButton("Create");
        submit.addActionListener(e -> {
            try {
                InvoiceDTO dto = new InvoiceDTO(
                        "INV-" + System.currentTimeMillis() % 100000,
                        supplierId.getText().trim(), poId.getText().trim(),
                        grnId.getText().trim(), new BigDecimal(amount.getText().trim()),
                        LocalDate.now(), LocalDate.now().plusDays(30), InvoiceDTO.PENDING);
                controller.createInvoice(this, dto, created -> {
                    invoiceId.setText(created.getInvoiceId());
                    UIHelper.showSuccess(this, "Invoice created: " + created.getInvoiceId());
                });
            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Amount must be numeric.");
            }
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        south.setOpaque(false);
        south.add(submit);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildVerifyPanel() {
        JPanel p = cardPanel("2. Verify Invoice");
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Invoice ID:"));       form.add(invoiceId);
        form.add(new JLabel("Expected Amount:"));  form.add(verifyAmount);
        p.add(form, BorderLayout.CENTER);

        JButton verify = UIHelper.createPrimaryButton("Verify");
        verify.addActionListener(e -> {
            try {
                controller.verifyInvoice(this,
                        invoiceId.getText().trim(),
                        new BigDecimal(verifyAmount.getText().trim()),
                        () -> UIHelper.showSuccess(this, "Invoice authorized."));
            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Expected amount must be numeric.");
            }
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        south.setOpaque(false);
        south.add(verify);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildPayPanel() {
        JPanel p = cardPanel("3. Pay Invoice");
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.setOpaque(false);
        form.add(new JLabel("Invoice ID must be AUTHORIZED to pay."));
        p.add(form, BorderLayout.CENTER);

        JButton pay = UIHelper.createPrimaryButton("Pay");
        pay.addActionListener(e ->
                controller.payInvoice(this, invoiceId.getText().trim(),
                        () -> UIHelper.showSuccess(this, "Invoice paid.")));
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        south.setOpaque(false);
        south.add(pay);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel cardPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Constants.BG_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel t = new JLabel(title);
        t.setFont(Constants.FONT_HEADING);
        p.add(t, BorderLayout.NORTH);
        return p;
    }

    @Override public void refresh() { /* form tab */ }
}
