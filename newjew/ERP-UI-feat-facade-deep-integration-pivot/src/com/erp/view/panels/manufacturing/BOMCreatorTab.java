package com.erp.view.panels.manufacturing;

import com.erp.controller.ManufacturingController;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.BomItemDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * BOM creator tab for creating BOM headers and BOM items with dropdown-based selection.
 */
public class BOMCreatorTab extends JPanel
        implements ManufacturingController.ManufacturingListener,
                   ManufacturingHomePanel.Refreshable {

    private static final String[] ITEM_COLS = {"Material ID", "Part Name", "Qty", "Unit Cost", "Line Cost"};

    private final ManufacturingController controller;

    private final JComboBox<String> productIdCombo = new JComboBox<>();
    private final JTextField productNameField = UIHelper.createTextField(14);
    private final JTextField bomIdField = UIHelper.createTextField(10);
    private final JTextField versionField = UIHelper.createTextField(8);
    private final JTextField budgetField = UIHelper.createTextField(10);
    private final JCheckBox activeBox = new JCheckBox("Active", true);

    private final JComboBox<MaterialDTO> materialCombo = new JComboBox<>();
    private final JTextField qtyField = UIHelper.createTextField(6);

    private final DefaultTableModel itemModel = new DefaultTableModel(ITEM_COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable itemTable = new JTable(itemModel);

    private final List<BomItemDTO> draftItems = new ArrayList<>();

    public BOMCreatorTab(ManufacturingController controller) {
        this.controller = controller;
        controller.addListener(this);

        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Constants.BG_LIGHT);

        UIHelper.styleTable(itemTable);

        add(buildHeaderForm(), BorderLayout.NORTH);
        add(new JScrollPane(itemTable), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildHeaderForm() {
        JPanel root = new JPanel(new GridLayout(2, 1, 0, 8));
        root.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);
        row1.add(new JLabel("BOM ID:"));
        row1.add(bomIdField);
        row1.add(new JLabel("Version:"));
        row1.add(versionField);
        row1.add(new JLabel("Product ID:"));

        productIdCombo.setEditable(true);
        productIdCombo.setPreferredSize(new Dimension(170, 30));
        productIdCombo.setFont(Constants.FONT_REGULAR);
        row1.add(productIdCombo);

        row1.add(new JLabel("Product Name:"));
        row1.add(productNameField);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);
        row2.add(new JLabel("Material:"));
        materialCombo.setPreferredSize(new Dimension(290, 30));
        materialCombo.setFont(Constants.FONT_REGULAR);
        row2.add(materialCombo);

        row2.add(new JLabel("Quantity:"));
        qtyField.setText("1");
        row2.add(qtyField);

        row2.add(new JLabel("Budget Limit:"));
        budgetField.setText("0");
        row2.add(budgetField);

        row2.add(activeBox);

        JButton addItem = UIHelper.createSecondaryButton("Add Item");
        addItem.addActionListener(e -> addItemRow());
        row2.add(addItem);

        JButton addMaterial = UIHelper.createSecondaryButton("New Material");
        addMaterial.addActionListener(e -> openMaterialDialog());
        row2.add(addMaterial);

        root.add(row1);
        root.add(row2);
        return root;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton remove = UIHelper.createSecondaryButton("Remove Selected");
        remove.addActionListener(e -> removeSelectedItem());

        JButton clear = UIHelper.createSecondaryButton("Clear Draft");
        clear.addActionListener(e -> clearDraft());

        JButton save = UIHelper.createPrimaryButton("Save BOM");
        save.addActionListener(e -> saveBom());

        actions.add(remove);
        actions.add(clear);
        actions.add(save);
        return actions;
    }

    private void addItemRow() {
        MaterialDTO material = (MaterialDTO) materialCombo.getSelectedItem();
        if (material == null) {
            UIHelper.showError(this, "Select a material from dropdown.");
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyField.getText().trim());
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Quantity must be numeric.");
            return;
        }
        if (qty <= 0) {
            UIHelper.showError(this, "Quantity must be greater than zero.");
            return;
        }

        BigDecimal lineCost = material.getUnitCost().multiply(BigDecimal.valueOf(qty));
        BomItemDTO item = new BomItemDTO(
                material.getMaterialItemId(),
                material.getPartName(),
                qty,
                material.getUnitCost(),
                lineCost);
        draftItems.add(item);

        itemModel.addRow(new Object[]{
                item.getMaterialItemId(),
                item.getPartName(),
                item.getQuantity(),
                UIHelper.formatINR(item.getUnitCost()),
                UIHelper.formatINR(item.getLineCost())
        });
    }

    private void removeSelectedItem() {
        int row = itemTable.getSelectedRow();
        if (row < 0) return;
        draftItems.remove(row);
        itemModel.removeRow(row);
    }

    private void clearDraft() {
        draftItems.clear();
        itemModel.setRowCount(0);
    }

    private void saveBom() {
        if (draftItems.isEmpty()) {
            UIHelper.showError(this, "Add at least one BOM item.");
            return;
        }

        String bomId = bomIdField.getText().trim();
        String version = versionField.getText().trim();
        String productId = productIdCombo.getEditor().getItem().toString().trim();
        String productName = productNameField.getText().trim();

        if (bomId.isEmpty() || version.isEmpty() || productId.isEmpty() || productName.isEmpty()) {
            UIHelper.showError(this, "BOM ID, Version, Product ID, and Product Name are required.");
            return;
        }

        BigDecimal budget;
        try {
            budget = new BigDecimal(budgetField.getText().trim());
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Budget limit must be numeric.");
            return;
        }

        List<BomItemDTO> items = new ArrayList<>(draftItems);
        BomDTO dto = new BomDTO();
        dto.setBomId(bomId);
        dto.setBomVersion(version);
        dto.setProductId(productId);
        dto.setProductName(productName);
        dto.setActive(activeBox.isSelected());
        dto.setBudgetLimit(budget);
        dto.setItems(items);

        controller.createBom(this, dto, created -> {
            UIHelper.showSuccess(this, "BOM saved: " + created.getBomId());
            clearDraft();
            refresh();
        });
    }

    private void openMaterialDialog() {
        JTextField materialId = UIHelper.createTextField(14);
        JTextField partName = UIHelper.createTextField(16);
        JTextField unitCost = UIHelper.createTextField(10);
        JTextField availableQty = UIHelper.createTextField(8);

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Material ID:")); form.add(materialId);
        form.add(new JLabel("Part Name:")); form.add(partName);
        form.add(new JLabel("Unit Cost:")); form.add(unitCost);
        form.add(new JLabel("Available Qty:")); form.add(availableQty);

        int ok = JOptionPane.showConfirmDialog(this, form, "Create Material",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        BigDecimal cost;
        double qty;
        try {
            cost = new BigDecimal(unitCost.getText().trim());
            qty = Double.parseDouble(availableQty.getText().trim());
        } catch (NumberFormatException ex) {
            UIHelper.showError(this, "Unit cost and available qty must be numeric.");
            return;
        }

        MaterialDTO dto = new MaterialDTO(
                materialId.getText().trim(),
                partName.getText().trim(),
                cost,
                qty);

        controller.createMaterial(this, dto, created -> {
            UIHelper.showSuccess(this, "Material created: " + created.getMaterialItemId());
            controller.loadMaterials(this);
        });
    }

    @Override
    public void refresh() {
        controller.loadBomList(this);
        controller.loadMaterials(this);
    }

    @Override
    public void onBomListLoaded(List<BomDTO> list) {
        Object keep = productIdCombo.getEditor().getItem();
        productIdCombo.removeAllItems();

        Set<String> ids = new LinkedHashSet<>();
        for (BomDTO b : list) {
            if (b.getProductId() != null && !b.getProductId().trim().isEmpty()) {
                ids.add(b.getProductId());
            }
        }
        for (String id : ids) productIdCombo.addItem(id);

        if (keep != null) productIdCombo.getEditor().setItem(keep);
    }

    @Override
    public void onMaterialsLoaded(List<MaterialDTO> list) {
        Object keep = materialCombo.getSelectedItem();
        materialCombo.removeAllItems();
        for (MaterialDTO m : list) materialCombo.addItem(m);
        if (keep instanceof MaterialDTO) materialCombo.setSelectedItem(keep);
        if (materialCombo.getSelectedItem() == null && materialCombo.getItemCount() > 0) {
            materialCombo.setSelectedIndex(0);
        }
    }
}
