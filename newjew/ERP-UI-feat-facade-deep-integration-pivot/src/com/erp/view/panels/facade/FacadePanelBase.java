package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.util.UIHelper;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Base for "facade" modules. Every interactive element ultimately routes to
 * {@link #stubAction(String)} which shows the standard under-construction dialog.
 *
 * Provides helpers to build rich-looking mockups (stat rows, fake tables, charts)
 * so each concrete facade panel stays short.
 */
public abstract class FacadePanelBase extends BasePanel {

    protected FacadePanelBase(String title) { super(title); }

    @Override
    protected void initializeComponents() { /* subclasses build in layoutComponents */ }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout(Constants.PADDING_MEDIUM, Constants.PADDING_MEDIUM));
        contentPanel.add(buildBody(), BorderLayout.CENTER);
    }

    /** Subclass builds the module's body. */
    protected abstract JComponent buildBody();

    // ============ helpers ============

    /** Standard "under construction" response for every interactive element. */
    protected void stubAction(String feature) {
        UIHelper.showNotAvailable(this, feature);
    }

    protected ActionListener stub(String feature) { return e -> stubAction(feature); }

    protected JButton stubButton(String label) {
        JButton b = new JButton(label);
        b.setFont(Constants.FONT_BUTTON);
        b.setForeground(Constants.TEXT_LIGHT);
        b.setBackground(Constants.PRIMARY_COLOR);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(stub(label));
        return b;
    }

    protected JButton secondaryStubButton(String label) {
        JButton b = new JButton(label);
        b.setFont(Constants.FONT_BUTTON);
        b.setForeground(Constants.PRIMARY_COLOR);
        b.setBackground(Constants.BG_WHITE);
        b.setBorder(BorderFactory.createLineBorder(Constants.PRIMARY_COLOR, 1));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(stub(label));
        return b;
    }

    protected JPanel statRow(DashboardCard... cards) {
        JPanel row = new JPanel(new GridLayout(1, cards.length, 12, 0));
        row.setOpaque(false);
        for (DashboardCard c : cards) row.add(c);
        return row;
    }

    protected JPanel chartRow(FakeChartPanel... charts) {
        JPanel row = new JPanel(new GridLayout(1, charts.length, 12, 0));
        row.setOpaque(false);
        for (FakeChartPanel c : charts) row.add(c);
        return row;
    }

    /** Read-only fake table; any click yields the stub dialog. */
    protected JComponent fakeTable(String[] columns, Object[][] data) {
        DefaultTableModel m = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setFont(Constants.FONT_REGULAR);
        t.setRowHeight(26);
        t.setGridColor(new Color(230, 232, 236));
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(Constants.FONT_HEADING);
        h.setBackground(Constants.PRIMARY_COLOR);
        h.setForeground(Constants.TEXT_LIGHT);
        h.setReorderingAllowed(false);
        t.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && t.getSelectedRow() >= 0) {
                stubAction("Row details for " + t.getValueAt(t.getSelectedRow(), 0));
                t.clearSelection();
            }
        });
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(new Color(225, 228, 232)));
        return sp;
    }

    protected JPanel sectionCard(String title, JComponent body) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Constants.BG_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));
        JLabel t = new JLabel(title);
        t.setFont(Constants.FONT_SUBTITLE);
        t.setForeground(Constants.TEXT_PRIMARY);
        p.add(t, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    protected JPanel toolbar(JComponent... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        for (JComponent c : comps) p.add(c);
        return p;
    }
}
