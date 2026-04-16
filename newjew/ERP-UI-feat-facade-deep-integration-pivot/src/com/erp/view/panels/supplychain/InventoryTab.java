package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.model.dto.PartDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Inventory tab — part list, low-stock rows highlighted, reorder CTA. */
public class InventoryTab extends JPanel
        implements SupplyChainController.SupplyChainListener,
                   SupplyChainHomePanel.Refreshable {

    private static final String[] COLUMNS = {"Part ID", "Part Name", "Stock", "Safety", "Reorder Pt", "Location", "Unit Cost"};

    private final SupplyChainController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<PartDTO> current = java.util.Collections.emptyList();

    public InventoryTab(SupplyChainController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        UIHelper.styleTable(table);
        table.setDefaultRenderer(Object.class, new LowStockRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        JButton reorder = UIHelper.createPrimaryButton("Reorder Selected");
        reorder.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select a part first."); return; }
            String qtyStr = JOptionPane.showInputDialog(this, "Reorder quantity:", "50");
            if (qtyStr == null) return;
            try {
                int qty = Integer.parseInt(qtyStr.trim());
                if (qty <= 0) { UIHelper.showError(this, "Quantity must be positive."); return; }
                String partId = (String) model.getValueAt(r, 0);
                controller.reorderPart(this, partId, qty, this::refresh);
            } catch (NumberFormatException ex) {
                UIHelper.showError(this, "Enter a valid integer.");
            }
        });

        JButton refresh = UIHelper.createSecondaryButton("Refresh");
        refresh.addActionListener(e -> refresh());

        p.add(reorder); p.add(refresh);
        return p;
    }

    @Override public void refresh() { controller.loadInventory(this); }

    @Override
    public void onInventoryLoaded(List<PartDTO> list) {
        this.current = list;
        model.setRowCount(0);
        for (PartDTO p : list) {
            model.addRow(new Object[]{
                    p.getPartId(), p.getPartName(), p.getStockLevel(),
                    p.getSafetyStock(), p.getReorderPoint(), p.getWarehouseLocation(),
                    "\u20B9 " + UIHelper.formatINR(p.getUnitCost())
            });
        }
    }

    @Override public void onSCMEntityChanged(Object e) { refresh(); }

    private class LowStockRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            if (!sel && row < current.size()) {
                PartDTO p = current.get(row);
                if (p.isBelowSafetyStock()) c.setBackground(new Color(255, 230, 230));
                else if (p.isBelowReorderPoint()) c.setBackground(new Color(255, 248, 220));
                else c.setBackground(Constants.BG_WHITE);
            }
            return c;
        }
    }
}
