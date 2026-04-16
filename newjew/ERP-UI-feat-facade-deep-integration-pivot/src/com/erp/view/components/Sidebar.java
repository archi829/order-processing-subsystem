package com.erp.view.components;

import com.erp.session.RoleAccess;
import com.erp.session.UserSession;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Sidebar is a navigation component for the main application.
 *
 * This class demonstrates:
 *
 * 1. COMPOSITION: The sidebar HAS menu items (contains a list of MenuItems).
 *    This is different from inheritance (IS-A relationship).
 *    Composition is often preferred as it's more flexible.
 *
 * 2. INNER CLASS: MenuItem is a nested class within Sidebar.
 *    It's tightly coupled to Sidebar and not used elsewhere.
 *
 * 3. CALLBACK PATTERN: Uses ActionListener to notify when items are clicked.
 *    The sidebar doesn't handle the navigation itself - it delegates to
 *    whoever is listening (separation of concerns).
 */
public class Sidebar extends JPanel {

    // List of menu items - demonstrates composition
    private List<MenuItem> menuItems;

    // Track currently selected item for highlighting
    private MenuItem selectedItem;

    // Listener for menu item clicks (callback pattern)
    private ActionListener menuActionListener;

    private final String role;

    public Sidebar() { this(null); }

    /**
     * Role-aware constructor — only modules the role may access are rendered.
     * A null role shows everything (used during boot before login).
     */
    public Sidebar(String role) {
        this.role = role;
        menuItems = new ArrayList<>();
        setupPanel();
        createMenuItems();
        layoutMenuItems();
    }

    /**
     * Sets up the panel properties.
     */
    private void setupPanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.BG_DARK);
        setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
    }

    /**
     * Creates all the menu items for ERP modules.
     */
    private void createMenuItems() {
        addMenuItem(Constants.MODULE_DASHBOARD, "dashboard");
        addMenuItem(Constants.MODULE_ORDER, "order");
        addMenuItem(Constants.MODULE_CRM, "crm");
        addMenuItem(Constants.MODULE_SALES, "sales");
        addMenuItem(Constants.MODULE_INVENTORY, "inventory");
        addMenuItem(Constants.MODULE_MANUFACTURING, "manufacturing");
        addMenuItem(Constants.MODULE_FINANCE, "finance");
        addMenuItem(Constants.MODULE_ACCOUNTING, "accounting");
        addMenuItem(Constants.MODULE_HR, "hr");
        addMenuItem(Constants.MODULE_PROJECT, "project");
        addMenuItem(Constants.MODULE_REPORTING, "reporting");
        addMenuItem(Constants.MODULE_ANALYTICS, "analytics");
        addMenuItem(Constants.MODULE_BI, "bi");
        addMenuItem(Constants.MODULE_MARKETING, "marketing");
        addMenuItem(Constants.MODULE_AUTOMATION, "automation");
    }

    /**
     * Helper method to add a menu item.
     *
     * @param title The display title
     * @param actionCommand The command string for identification
     */
    private void addMenuItem(String title, String actionCommand) {
        String effectiveRole = role;
        if (effectiveRole == null) {
            UserSession s = UserSession.getInstance();
            if (s.isValid()) effectiveRole = s.getRole();
        }
        if (effectiveRole != null && !RoleAccess.canAccess(actionCommand, effectiveRole)) return;
        MenuItem item = new MenuItem(title, actionCommand);
        menuItems.add(item);
    }

    /**
     * Lays out the menu items in the sidebar.
     */
    private void layoutMenuItems() {
        // Logo/Brand section at top
        JPanel brandPanel = createBrandPanel();
        add(brandPanel, BorderLayout.NORTH);

        // Scrollable menu items section
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Constants.BG_DARK);
        menuPanel.setBorder(new EmptyBorder(Constants.PADDING_MEDIUM, 0, Constants.PADDING_MEDIUM, 0));

        for (MenuItem item : menuItems) {
            menuPanel.add(item);
            menuPanel.add(Box.createVerticalStrut(2)); // Small gap between items
        }

        // Add glue to push items to top
        menuPanel.add(Box.createVerticalGlue());

        // Make scrollable for many items
        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Constants.BG_DARK);
        scrollPane.getViewport().setBackground(Constants.BG_DARK);

        add(scrollPane, BorderLayout.CENTER);

        // Select dashboard by default
        if (!menuItems.isEmpty()) {
            selectItem(menuItems.get(0));
        }
    }

    /**
     * Creates the brand/logo panel at the top of the sidebar.
     */
    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Constants.SECONDARY_COLOR);
        panel.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 80));
        panel.setBorder(new EmptyBorder(Constants.PADDING_LARGE, Constants.PADDING_MEDIUM,
                                        Constants.PADDING_LARGE, Constants.PADDING_MEDIUM));

        JLabel brandLabel = new JLabel("TATA MOTORS");
        brandLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 20));
        brandLabel.setForeground(Constants.TATA_GOLD);
        brandLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel versionLabel = new JLabel("ERP v" + Constants.APP_VERSION);
        versionLabel.setFont(Constants.FONT_SMALL);
        versionLabel.setForeground(new Color(200, 210, 230));
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(brandLabel, BorderLayout.CENTER);
        panel.add(versionLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Sets the action listener for menu item clicks.
     * This is the callback pattern - external code can listen for selections.
     *
     * @param listener The listener to receive click events
     */
    public void setMenuActionListener(ActionListener listener) {
        this.menuActionListener = listener;
    }

    /**
     * Selects a menu item and updates highlighting.
     *
     * @param item The item to select
     */
    private void selectItem(MenuItem item) {
        // Deselect previous
        if (selectedItem != null) {
            selectedItem.setSelected(false);
        }

        // Select new
        selectedItem = item;
        selectedItem.setSelected(true);
    }

    /**
     * Selects a menu item by its action command.
     * Useful for programmatic navigation.
     *
     * @param actionCommand The command to select
     */
    public void selectByCommand(String actionCommand) {
        for (MenuItem item : menuItems) {
            if (item.getActionCommand().equals(actionCommand)) {
                selectItem(item);
                break;
            }
        }
    }

    // ============================================================
    // INNER CLASS: MenuItem
    // ============================================================

    /**
     * MenuItem is an inner class representing a single navigation item.
     *
     * Why inner class?
     * - MenuItem is only used within Sidebar
     * - It needs access to Sidebar's methods (selectItem, menuActionListener)
     * - It logically belongs to Sidebar
     *
     * Non-static inner class has access to outer class's members.
     */
    private class MenuItem extends JPanel {

        private String title;
        private String actionCommand;
        private JLabel titleLabel;
        private boolean isSelected;

        /**
         * Creates a new menu item.
         *
         * @param title The display text
         * @param actionCommand The command for identification
         */
        public MenuItem(String title, String actionCommand) {
            this.title = title;
            this.actionCommand = actionCommand;
            this.isSelected = false;

            setupItem();
        }

        /**
         * Sets up the menu item appearance and behavior.
         */
        private void setupItem() {
            setLayout(new BorderLayout());
            setBackground(Constants.BG_DARK);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 45));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, Constants.PADDING_LARGE, 0, Constants.PADDING_MEDIUM));

            // Title label
            titleLabel = new JLabel(title);
            titleLabel.setFont(Constants.FONT_REGULAR);
            titleLabel.setForeground(Constants.TEXT_LIGHT);

            add(titleLabel, BorderLayout.WEST);

            // Click handler
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    // Select this item
                    selectItem(MenuItem.this);

                    // Notify listener if set
                    if (menuActionListener != null) {
                        java.awt.event.ActionEvent event = new java.awt.event.ActionEvent(
                            MenuItem.this,
                            java.awt.event.ActionEvent.ACTION_PERFORMED,
                            actionCommand
                        );
                        menuActionListener.actionPerformed(event);
                    }
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isSelected) {
                        setBackground(new Color(60, 80, 100));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isSelected) {
                        setBackground(Constants.BG_DARK);
                    }
                }
            });
        }

        /**
         * Sets the selection state and updates appearance.
         *
         * @param selected True if selected
         */
        public void setSelected(boolean selected) {
            this.isSelected = selected;
            if (selected) {
                setBackground(Constants.PRIMARY_COLOR);
                titleLabel.setForeground(Constants.TEXT_LIGHT);
            } else {
                setBackground(Constants.BG_DARK);
                titleLabel.setForeground(Constants.TEXT_LIGHT);
            }
        }

        /**
         * Gets the action command for this item.
         * @return The action command string
         */
        public String getActionCommand() {
            return actionCommand;
        }
    }
}
