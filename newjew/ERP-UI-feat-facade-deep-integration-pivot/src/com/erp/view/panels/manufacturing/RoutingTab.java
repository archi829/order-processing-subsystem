package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Routing tab — loads a routing sequence. If the mock detects a gap in
 * sequence numbers (e.g. 1,2,4) it throws {@code ROUTING_STEP_GAP}; the
 * controller's retry path surfaces it via the exception dialog.
 */
public class RoutingTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private static final String[] COLUMNS = {"Seq", "Operation", "Work Center", "Setup (h)", "Run (h)"};

    private final ManufacturingController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JComboBox<String> productIds = new JComboBox<>();

    public RoutingTab(ManufacturingController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        top.add(new JLabel("Product ID:"));
        productIds.setPreferredSize(new Dimension(180, 30));
        productIds.setFont(Constants.FONT_REGULAR);
        productIds.addItem("All");
        top.add(productIds);
        JButton load = UIHelper.createPrimaryButton("Load Routing");
        load.addActionListener(e -> loadSelectedRouting());
        top.add(load);
        JLabel tip = new JLabel("<html><i>Try PRD-NEXON-EV to trigger ROUTING_STEP_GAP.</i></html>");
        tip.setForeground(Constants.TEXT_SECONDARY);
        top.add(tip);

        UIHelper.styleTable(table);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    @Override
    public void refresh() {
        controller.loadBomList(this);
        loadSelectedRouting();
    }

    private void loadSelectedRouting() {
        String selected = (String) productIds.getSelectedItem();
        if (selected == null || "All".equalsIgnoreCase(selected)) {
            controller.loadRouting(this, null);
            return;
        }
        controller.loadRouting(this, selected);
    }

    @Override
    public void onBomListLoaded(List<BomDTO> list) {
        Object keep = productIds.getSelectedItem();
        productIds.removeAllItems();
        productIds.addItem("All");

        Set<String> ids = new LinkedHashSet<>();
        for (BomDTO b : list) {
            if (b.getProductId() != null && !b.getProductId().trim().isEmpty()) {
                ids.add(b.getProductId());
            }
        }
        for (String id : ids) productIds.addItem(id);

        if (keep != null) productIds.setSelectedItem(keep);
        if (productIds.getSelectedItem() == null) productIds.setSelectedIndex(0);
    }

    @Override
    public void onRoutingLoaded(List<RoutingStepDTO> list) {
        model.setRowCount(0);
        for (RoutingStepDTO s : list) {
            model.addRow(new Object[]{
                    s.getSequenceNumber(), s.getOperationName(), s.getWorkCenterId(),
                    s.getSetupTime(), s.getRunTime()
            });
        }
    }
}
