package com.erp.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * UIHelper provides factory methods and utilities for creating UI components.
 *
 * This class demonstrates the Factory Method pattern - it encapsulates the
 * creation of complex objects (styled buttons, text fields, etc.) and provides
 * a simple interface for getting pre-configured components.
 *
 * Benefits:
 * - Consistent styling across the application
 * - Single point of change for UI styling
 * - Reduces code duplication
 */
public final class UIHelper {

    private UIHelper() {
        throw new UnsupportedOperationException("UIHelper cannot be instantiated");
    }

    /**
     * Creates a styled primary button (main action button).
     * @param text The button text
     * @return A styled JButton
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Constants.FONT_BUTTON);
        button.setForeground(Constants.TEXT_LIGHT);
        button.setBackground(Constants.PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(Constants.PRIMARY_DARK);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Constants.PRIMARY_COLOR);
            }
        });

        return button;
    }

    /**
     * Creates a styled secondary button (less prominent actions).
     * @param text The button text
     * @return A styled JButton
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Constants.FONT_BUTTON);
        button.setForeground(Constants.PRIMARY_COLOR);
        button.setBackground(Constants.BG_WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Constants.PRIMARY_COLOR, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));

        return button;
    }

    /**
     * Creates a styled text field with consistent appearance.
     * @param columns Number of columns for the text field
     * @return A styled JTextField
     */
    public static JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(Constants.FONT_REGULAR);
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setBorder(createTextFieldBorder());
        return textField;
    }

    /**
     * Creates a styled password field.
     * @param columns Number of columns
     * @return A styled JPasswordField
     */
    public static JPasswordField createPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        passwordField.setFont(Constants.FONT_REGULAR);
        passwordField.setPreferredSize(new Dimension(200, 35));
        passwordField.setBorder(createTextFieldBorder());
        return passwordField;
    }

    /**
     * Creates a consistent border for text fields.
     * Uses CompoundBorder to combine line border with padding.
     * @return A compound border
     */
    public static Border createTextFieldBorder() {
        Border lineBorder = BorderFactory.createLineBorder(Constants.TEXT_SECONDARY, 1);
        Border paddingBorder = new EmptyBorder(5, 10, 5, 10);
        return new CompoundBorder(lineBorder, paddingBorder);
    }

    /**
     * Creates a styled label.
     * @param text The label text
     * @param font The font to use
     * @param color The text color
     * @return A styled JLabel
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Creates a panel with padding.
     * @param padding The padding amount
     * @return A JPanel with empty border padding
     */
    public static JPanel createPaddedPanel(int padding) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return panel;
    }

    /**
     * Creates a card-style panel with shadow effect.
     * @return A styled panel that looks like a card
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Constants.BG_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(Constants.PADDING_LARGE, Constants.PADDING_LARGE,
                          Constants.PADDING_LARGE, Constants.PADDING_LARGE)
        ));
        return panel;
    }

    /**
     * Centers a window on the screen.
     * @param window The window to center
     */
    public static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - window.getWidth()) / 2;
        int y = (screenSize.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    /**
     * Shows an error dialog with consistent styling.
     * @param parent The parent component
     * @param message The error message
     */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Shows a success dialog.
     * @param parent The parent component
     * @param message The success message
     */
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows a confirmation dialog.
     * @param parent The parent component
     * @param message The confirmation message
     * @return true if user confirms, false otherwise
     */
    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /** Standard dialog for facade modules / unfinished features. */
    public static void showNotAvailable(Component parent, String feature) {
        JOptionPane.showMessageDialog(parent,
                "\"" + feature + "\" has not been developed yet.\n\n"
                        + "This module is part of the visual mockup showcase.",
                "Feature unavailable",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Danger-styled confirmation for irreversible actions. */
    public static boolean confirmDanger(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent,
                message + "\n\nThis action cannot be undone.",
                "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /** Small pill badge showing a user role, colored by role. */
    public static JComponent roleBadge(String role) {
        final String label = role == null ? "" : role.toUpperCase();
        final Color bg, fg;
        switch (role == null ? "" : role) {
            case "Admin":    bg = Constants.TATA_GOLD; fg = Constants.TATA_NAVY; break;
            case "Manager":  bg = Constants.TATA_BLUE; fg = Constants.TATA_WHITE; break;
            case "Employee": bg = new Color(39, 174, 96); fg = Constants.TATA_WHITE; break;
            case "HR":       bg = new Color(142, 68, 173); fg = Constants.TATA_WHITE; break;
            case "Sales":    bg = new Color(192, 57, 43);  fg = Constants.TATA_WHITE; break;
            case "Manufacturing": bg = new Color(211, 84, 0); fg = Constants.TATA_WHITE; break;
            case "SupplyChain":   bg = new Color(22, 160, 133); fg = Constants.TATA_WHITE; break;
            default:         bg = Constants.TATA_MUTED;    fg = Constants.TATA_WHITE; break;
        }
        JLabel pill = new JLabel(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pill.setOpaque(false);
        pill.setForeground(fg);
        pill.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 10));
        pill.setHorizontalAlignment(SwingConstants.CENTER);
        pill.setBorder(new EmptyBorder(4, 12, 4, 12));
        return pill;
    }

    /** Indian-locale currency (₹18,42,500.00). */
    public static String formatINR(java.math.BigDecimal amount) {
        if (amount == null) return "₹0";
        java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "IN"));
        return nf.format(amount);
    }

    /**
     * Styles a JTable with consistent header colors.
     * Uses a custom renderer to ensure header text is visible.
     * @param table The table to style
     */
    public static void styleTable(JTable table) {
        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(28);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowGrid(true);
        table.getTableHeader().setFont(Constants.FONT_REGULAR);
        table.getTableHeader().setReorderingAllowed(false);

        // Custom header renderer for proper colors
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value != null ? value.toString() : "");
                label.setFont(Constants.FONT_REGULAR);
                label.setForeground(Constants.TEXT_LIGHT);
                label.setBackground(Constants.PRIMARY_COLOR);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(30, 100, 150)),
                    new EmptyBorder(8, 8, 8, 8)
                ));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                return label;
            }
        });
    }
}
