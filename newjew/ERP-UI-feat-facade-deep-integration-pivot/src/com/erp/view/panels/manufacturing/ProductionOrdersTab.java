package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Production orders tab — list + create + cancel (cancel honours the
 * {@code PRODUCTION_ORDER_CANCELLATION_BLOCKED} business rule).
 */
public class ProductionOrdersTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private static final String[] COLUMNS = {"Order", "Order Date", "Product", "BOM", "Status", "Priority", "Planned", "Qty"};

    private final ManufacturingController controller;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public ProductionOrdersTab(ManufacturingController controller) {
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

        JButton create = UIHelper.createPrimaryButton("New Order");
        create.addActionListener(e -> openCreateDialog());

        JButton cancel = UIHelper.createSecondaryButton("Cancel Selected");
        cancel.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.showError(this, "Select an order first."); return; }
            String orderId = (String) model.getValueAt(r, 0);
            if (UIHelper.confirmDanger(this, "Cancel production order " + orderId + "?")) {
                controller.cancelProductionOrder(this, orderId, this::refresh);
            }
        });

        JButton execution = UIHelper.createSecondaryButton("Record Execution");
        execution.addActionListener(e -> openExecutionDialog());

        JButton refresh = UIHelper.createSecondaryButton("Refresh");
        refresh.addActionListener(e -> refresh());

        p.add(create); p.add(cancel); p.add(execution); p.add(refresh);
        return p;
    }

    private void openExecutionDialog() {
        int r = table.getSelectedRow();
        if (r < 0) { UIHelper.showError(this, "Select an order first."); return; }
        String orderId = (String) model.getValueAt(r, 0);

        JTextField operatorId = new JTextField("EMP-001", 10);
        JTextField machineId = new JTextField("MC-01", 10);
        JTextField qtyProduced = new JTextField("1", 6);
        JTextField scrapQty = new JTextField("0", 6);
        JTextField note = new JTextField("Regular shift", 18);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Order ID:"));     form.add(new JLabel(orderId));
        form.add(new JLabel("Operator ID:"));  form.add(operatorId);
        form.add(new JLabel("Machine ID:"));   form.add(machineId);
        form.add(new JLabel("Qty Produced:")); form.add(qtyProduced);
        form.add(new JLabel("Scrap Qty:"));    form.add(scrapQty);
        form.add(new JLabel("Note:"));         form.add(note);

        int ok = JOptionPane.showConfirmDialog(this, form, "Record Execution Log",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            ExecutionLogDTO log = new ExecutionLogDTO();
            log.setOrderId(orderId);
            log.setOperatorId(operatorId.getText().trim());
            log.setMachineId(machineId.getText().trim());
            log.setQtyProduced(Double.parseDouble(qtyProduced.getText().trim()));
            log.setScrapQty(Double.parseDouble(scrapQty.getText().trim()));
            log.setNote(note.getText().trim());
            log.setStartTime(LocalDateTime.now());
            controller.recordExecutionLog(this, log, this::refresh);
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Produced and scrap quantities must be numeric.");
        }
    }

    private void openCreateDialog() {
        JTextField productId = new JTextField(12);
        JTextField productName = new JTextField(14);
        JTextField bomId = new JTextField(10);
        JTextField qty = new JTextField("10", 6);
        JComboBox<String> pri = new JComboBox<>(new String[]{
                ProductionOrderDTO.PRI_LOW, ProductionOrderDTO.PRI_MEDIUM,
                ProductionOrderDTO.PRI_HIGH, ProductionOrderDTO.PRI_URGENT});

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Product ID:"));   form.add(productId);
        form.add(new JLabel("Product Name:")); form.add(productName);
        form.add(new JLabel("BOM ID:"));       form.add(bomId);
        form.add(new JLabel("Qty Planned:"));  form.add(qty);
        form.add(new JLabel("Priority:"));     form.add(pri);

        int ok = JOptionPane.showConfirmDialog(this, form, "New Production Order",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            ProductionOrderDTO dto = new ProductionOrderDTO(
                    "PO-" + System.currentTimeMillis() % 100000,
                    productId.getText().trim(),
                    productName.getText().trim(),
                    bomId.getText().trim(),
                    LocalDate.now(), LocalDate.now().plusDays(14),
                    ProductionOrderDTO.PENDING,
                    (String) pri.getSelectedItem(),
                    Integer.parseInt(qty.getText().trim()));
            controller.createProductionOrder(this, dto, created -> refresh());
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Quantity must be a number.");
        }
    }

    @Override public void refresh() { controller.loadProductionOrders(this); }

    @Override
    public void onProductionOrdersLoaded(List<ProductionOrderDTO> list) {
        model.setRowCount(0);
        for (ProductionOrderDTO o : list) {
            model.addRow(new Object[]{
                    o.getOrderId(),
                    o.getOrderDate() == null ? "" : o.getOrderDate().toString(),
                    o.getProductName(), o.getBomId(),
                    o.getStatus(), o.getPriority(),
                    o.getPlannedStartDate() + " \u2192 " + o.getPlannedEndDate(),
                    o.getQtyProduced() + "/" + o.getQtyPlanned()
            });
        }
    }

    @Override public void onMfgEntityChanged(Object e) { refresh(); }
}
