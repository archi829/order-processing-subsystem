package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.CarModelDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Cars-in-assembly tab — list vehicles flowing through the line, update build status.
 */
public class AssemblyLinesTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private static final String[] COLUMNS = {"VIN", "Model", "Chassis", "Line", "Status", "Started"};
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ManufacturingController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public AssemblyLinesTab(ManufacturingController controller) {
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

        JComboBox<String> newStatus = new JComboBox<>(new String[]{
                CarModelDTO.PENDING, CarModelDTO.IN_ASSEMBLY, CarModelDTO.IN_QUALITY,
                CarModelDTO.READY, CarModelDTO.SHIPPED});

        JButton updateBtn = UIHelper.createPrimaryButton("Update Status");
        updateBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select a car first."); return; }
            String vin = (String) model.getValueAt(r, 0);
            controller.updateCarStatus(this, vin, (String) newStatus.getSelectedItem(), this::refresh);
        });

        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        p.add(new JLabel("New Status:"));
        p.add(newStatus);
        p.add(updateBtn);
        p.add(refreshBtn);
        return p;
    }

    @Override public void refresh() { controller.loadCars(this); }

    @Override
    public void onCarsLoaded(List<CarModelDTO> list) {
        model.setRowCount(0);
        for (CarModelDTO c : list) {
            model.addRow(new Object[]{
                    c.getVin(), c.getModelName(), c.getChassisType(),
                    c.getAssemblyLineId(), c.getBuildStatus(),
                    c.getStartedAt() == null ? "" : c.getStartedAt().format(TS)
            });
        }
    }

    @Override public void onMfgEntityChanged(Object e) { refresh(); }
}
