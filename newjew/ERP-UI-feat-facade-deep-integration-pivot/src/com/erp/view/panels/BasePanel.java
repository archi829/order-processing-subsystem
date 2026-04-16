package com.erp.view.panels;

import com.erp.util.Constants;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * BasePanel is an abstract class that all module panels extend.
 *
 * This demonstrates key OOP concepts:
 *
 * 1. ABSTRACTION: We define a common interface (abstract methods) that all
 *    panels must implement, without specifying HOW they implement it.
 *
 * 2. INHERITANCE: Child classes (CRMPanel, HRPanel, etc.) will extend this
 *    class and inherit its properties and methods.
 *
 * 3. TEMPLATE METHOD PATTERN: The constructor calls abstract methods
 *    (initializeComponents, layoutComponents) that subclasses must implement.
 *    This defines the "skeleton" of the algorithm while letting subclasses
 *    fill in the details.
 *
 * Why use an abstract class instead of interface?
 * - We want to provide some default implementation (common panel setup)
 * - We have instance fields (panelTitle, etc.)
 * - We want a mix of abstract and concrete methods
 */
public abstract class BasePanel extends JPanel {

    // Protected fields - accessible by subclasses but not external classes
    protected String panelTitle;
    protected JPanel headerPanel;
    protected JPanel contentPanel;

    /**
     * Constructor sets up the common panel structure.
     * Note: Constructor calls abstract methods - this works because
     * the subclass's overridden methods will be called.
     *
     * @param title The title displayed at the top of the panel
     */
    public BasePanel(String title) {
        this.panelTitle = title;

        // Set up the base panel
        setLayout(new BorderLayout());
        setBackground(Constants.BG_LIGHT);

        // Create header
        createHeader();

        // Create main content area
        contentPanel = new JPanel();
        contentPanel.setBackground(Constants.BG_LIGHT);
        contentPanel.setBorder(new EmptyBorder(Constants.PADDING_LARGE,
                Constants.PADDING_LARGE, Constants.PADDING_LARGE, Constants.PADDING_LARGE));

        // Add to this panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        // Call abstract methods that subclasses must implement
        initializeComponents();
        layoutComponents();
    }

    /**
     * Creates the header section with the panel title.
     * This is a concrete (implemented) method that subclasses inherit.
     */
    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Constants.BG_WHITE);
        headerPanel.setBorder(new EmptyBorder(Constants.PADDING_LARGE,
                Constants.PADDING_LARGE, Constants.PADDING_LARGE, Constants.PADDING_LARGE));

        JLabel titleLabel = new JLabel(panelTitle);
        titleLabel.setFont(Constants.FONT_TITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);

        headerPanel.add(titleLabel, BorderLayout.WEST);
    }

    /**
     * ABSTRACT METHOD: Subclasses MUST implement this.
     * Initialize all UI components (buttons, text fields, tables, etc.)
     *
     * Abstract methods have no body - just a signature.
     * They define WHAT must be done, not HOW.
     */
    protected abstract void initializeComponents();

    /**
     * ABSTRACT METHOD: Subclasses MUST implement this.
     * Arrange components using layout managers.
     */
    protected abstract void layoutComponents();

    /**
     * Optional method that subclasses CAN override.
     * Called when the panel is refreshed (e.g., data updated).
     * Default implementation does nothing - subclasses override if needed.
     *
     * This is different from abstract - it provides a default behavior.
     */
    public void refreshData() {
        // Default implementation - subclasses can override
    }

    /**
     * Gets the panel title.
     * @return The panel's title string
     */
    public String getPanelTitle() {
        return panelTitle;
    }

    /**
     * Utility method to add a header action button.
     * Subclasses can call this to add buttons to the header.
     *
     * @param button The button to add
     */
    protected void addHeaderButton(JButton button) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(button);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
    }

    /**
     * Creates a section panel with a title.
     * Utility method for creating consistent sections in content area.
     *
     * @param title The section title
     * @return A JPanel configured as a section
     */
    protected JPanel createSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BorderLayout());
        section.setBackground(Constants.BG_WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(Constants.PADDING_MEDIUM, Constants.PADDING_MEDIUM,
                          Constants.PADDING_MEDIUM, Constants.PADDING_MEDIUM)
        ));

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(Constants.FONT_SUBTITLE);
        sectionTitle.setForeground(Constants.TEXT_PRIMARY);
        sectionTitle.setBorder(new EmptyBorder(0, 0, Constants.PADDING_MEDIUM, 0));

        section.add(sectionTitle, BorderLayout.NORTH);

        return section;
    }
}
