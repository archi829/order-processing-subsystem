package com.erp.view.panels.supplychain;

import com.erp.controller.SupplyChainController;
import com.erp.model.dto.ShipmentDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Shipment tracking updates — minimal form that drives
 * {@code SCM_SHIPMENT_UPDATE} for a shipment id.
 */
public class ShipmentsTab extends JPanel
        implements SupplyChainHomePanel.Refreshable {

    private final SupplyChainController controller;
    private final JTextField shipmentId = new JTextField("SHP-001", 12);
    private final JComboBox<String> status = new JComboBox<>(new String[]{
            ShipmentDTO.PENDING, ShipmentDTO.IN_TRANSIT, ShipmentDTO.DELIVERED, ShipmentDTO.DELAYED});

    public ShipmentsTab(SupplyChainController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setOpaque(false);
        form.add(new JLabel("Shipment ID:")); form.add(shipmentId);
        form.add(new JLabel("New Status:"));  form.add(status);

        JButton submit = UIHelper.createPrimaryButton("Update Status");
        submit.addActionListener(e ->
                controller.updateShipmentStatus(this,
                        shipmentId.getText().trim(),
                        (String) status.getSelectedItem(),
                        () -> UIHelper.showSuccess(this, "Shipment updated.")));

        add(form, BorderLayout.NORTH);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        south.setOpaque(false);
        south.add(submit);
        add(south, BorderLayout.SOUTH);
    }

    @Override public void refresh() { /* form tab */ }
}
