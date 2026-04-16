package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.model.dto.POLineItemDTO;
import com.erp.model.dto.PurchaseOrderDTO;
import com.erp.session.UserSession;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PO tab — create, approve (four-eyes rule enforced by mock), cancel.
 */
public class PurchaseOrdersTab extends JPanel
        implements SupplyChainController.SupplyChainListener,
                   SupplyChainHomePanel.Refreshable {

    private static final String[] COLUMNS = {"PO ID", "Supplier", "Total", "Status", "Created By", "Approved By", "ETA"};

    private final SupplyChainController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public PurchaseOrdersTab(SupplyChainController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        JButton create = UIHelper.createPrimaryButton("New PO");
        create.addActionListener(e -> openCreateDialog());

        JButton approve = UIHelper.createSecondaryButton("Approve Selected");
        approve.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select a PO first."); return; }
            String poId = (String) model.getValueAt(r, 0);
            String approver = UserSession.getInstance().isValid()
                    ? UserSession.getInstance().getUserId() : "unknown";
            controller.approvePurchaseOrder(this, poId, approver, this::refresh);
        });

        JButton refresh = UIHelper.createSecondaryButton("Refresh");
        refresh.addActionListener(e -> refresh());

        p.add(create); p.add(approve); p.add(refresh);
        return p;
    }

    private void openCreateDialog() {
        JTextField supplierId = new JTextField("SUP-001", 10);
        JTextField partName   = new JTextField("Alloy Wheels", 16);
        JTextField qty        = new JTextField("25", 6);
        JTextField unitPrice  = new JTextField("4500", 8);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Supplier ID:"));  form.add(supplierId);
        form.add(new JLabel("Part Name:"));    form.add(partName);
        form.add(new JLabel("Quantity:"));     form.add(qty);
        form.add(new JLabel("Unit Price:"));   form.add(unitPrice);
        JLabel tip = new JLabel("<html><i>Use SUP-008 to trigger SUPPLIER_NOT_FOUND.</i></html>");
        tip.setForeground(Constants.TEXT_SECONDARY);
        form.add(new JLabel()); form.add(tip);

        int ok = JOptionPane.showConfirmDialog(this, form, "New Purchase Order",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            int q = Integer.parseInt(qty.getText().trim());
            BigDecimal up = new BigDecimal(unitPrice.getText().trim());
            BigDecimal line = up.multiply(BigDecimal.valueOf(q));
            POLineItemDTO item = new POLineItemDTO(1, "PART-XX",
                    partName.getText().trim(), q, up, line);
            String creator = UserSession.getInstance().isValid()
                    ? UserSession.getInstance().getUserId() : "unknown";
            List<POLineItemDTO> items = new ArrayList<>(Collections.singletonList(item));
            PurchaseOrderDTO dto = new PurchaseOrderDTO(
                    "PO-" + System.currentTimeMillis() % 100000,
                    supplierId.getText().trim(), null,
                    items, line, PurchaseOrderDTO.PENDING_APPROVAL,
                    LocalDate.now(), creator, LocalDate.now().plusDays(14));
            controller.createPurchaseOrder(this, dto, created -> refresh());
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Quantity and price must be numeric.");
        }
    }

    @Override public void refresh() { controller.loadPurchaseOrders(this, null); }

    @Override
    public void onPurchaseOrdersLoaded(List<PurchaseOrderDTO> list) {
        model.setRowCount(0);
        for (PurchaseOrderDTO po : list) {
            model.addRow(new Object[]{
                    po.getPoId(), po.getSupplierName(),
                    po.getTotalAmount() == null ? "\u20B9 0" : "\u20B9 " + UIHelper.formatINR(po.getTotalAmount()),
                    po.getStatus(), po.getCreatedBy(),
                    po.getApprovedBy() == null ? "-" : po.getApprovedBy(),
                    po.getEta() == null ? "-" : po.getEta().toString()
            });
        }
    }

    @Override public void onSCMEntityChanged(Object e) { refresh(); }
}
