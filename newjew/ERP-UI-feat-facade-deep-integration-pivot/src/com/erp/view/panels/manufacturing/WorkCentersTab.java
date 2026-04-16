package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.WorkCenterDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Work centers + capacity utilisation overview. */
public class WorkCentersTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private static final String[] COLUMNS = {"ID", "Name", "Type", "Capacity (h)", "Utilisation %", "Location"};

    private final ManufacturingController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public WorkCentersTab(ManufacturingController controller) {
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

    @Override public void refresh() { controller.loadWorkCenters(this); }

    @Override
    public void onWorkCentersLoaded(List<WorkCenterDTO> list) {
        model.setRowCount(0);
        for (WorkCenterDTO w : list) {
            model.addRow(new Object[]{
                    w.getWcId(), w.getWcName(), w.getWcType(),
                    w.getCapacityHours(),
                    String.format("%.1f%%", w.getUtilizationPct()),
                    w.getLocation()
            });
        }
    }
}
