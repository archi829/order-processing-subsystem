package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.BomItemDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * BOM Explorer — pick a BOM from the header list and see its nested items + roll-up cost.
 */
public class BOMExplorerTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private static final String[] HEADER_COLS = {"BOM ID", "Product", "Version", "Active", "Total Cost"};
    private static final String[] ITEM_COLS   = {"Material", "Part Name", "Qty", "Unit Cost", "Line Cost"};

    private final ManufacturingController controller;
    private final DefaultTableModel headerModel = new DefaultTableModel(HEADER_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel itemModel = new DefaultTableModel(ITEM_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable header = new JTable(headerModel);
    private final JTable items = new JTable(itemModel);

    public BOMExplorerTab(ManufacturingController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        UIHelper.styleTable(header);
        UIHelper.styleTable(items);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(header), new JScrollPane(items));
        split.setResizeWeight(0.45);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        header.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int r = header.getSelectedRow();
            if (r >= 0) {
                String bomId = (String) headerModel.getValueAt(r, 0);
                controller.loadBomDetails(this, bomId);
            }
        });

        JButton refresh = UIHelper.createSecondaryButton("Refresh");
        refresh.addActionListener(e -> refresh());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);
        south.add(refresh);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    @Override public void refresh() { controller.loadBomList(this); itemModel.setRowCount(0); }

    @Override
    public void onBomListLoaded(List<BomDTO> list) {
        headerModel.setRowCount(0);
        for (BomDTO b : list) {
            headerModel.addRow(new Object[]{
                    b.getBomId(), b.getProductName(), b.getBomVersion(),
                    b.isActive() ? "Yes" : "No",
                    b.getTotalCost() == null ? "\u20B9 0" : "\u20B9 " + UIHelper.formatINR(b.getTotalCost())
            });
        }
    }

    @Override
    public void onBomDetailsLoaded(BomDTO bom) {
        itemModel.setRowCount(0);
        if (bom == null) return;
        for (BomItemDTO it : bom.getItems()) {
            itemModel.addRow(new Object[]{
                    it.getMaterialItemId(), it.getPartName(), it.getQuantity(),
                    "\u20B9 " + UIHelper.formatINR(it.getUnitCost()),
                    "\u20B9 " + UIHelper.formatINR(it.getLineCost())
            });
        }
    }
}
