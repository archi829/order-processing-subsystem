package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.model.dto.SupplierDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Supplier directory with scorecard. */
public class SuppliersTab extends JPanel
        implements SupplyChainController.SupplyChainListener,
                   SupplyChainHomePanel.Refreshable {

    private static final String[] COLUMNS = {"Supplier ID", "Name", "Approved", "Compliance", "Scorecard", "Email", "Terms"};

    private final SupplyChainController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public SuppliersTab(SupplyChainController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        UIHelper.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = UIHelper.createSecondaryButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);
        south.add(refresh);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    @Override public void refresh() { controller.loadSuppliers(this); }

    @Override
    public void onSuppliersLoaded(List<SupplierDTO> list) {
        model.setRowCount(0);
        for (SupplierDTO s : list) {
            model.addRow(new Object[]{
                    s.getSupplierId(), s.getSupplierName(),
                    s.isApproved() ? "Yes" : "No",
                    s.isComplianceStatus() ? "OK" : "Flag",
                    String.format("%.1f / 5.0", s.getScorecard()),
                    s.getContactEmail(), s.getPaymentTerms()
            });
        }
    }
}
