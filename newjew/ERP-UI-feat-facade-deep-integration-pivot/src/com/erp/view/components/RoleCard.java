package com.erp.view.components;

import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A selectable rectangular role card for the login screen.
 * Clicking toggles its selected state and invokes the supplied callback.
 */
public class RoleCard extends JPanel {

    private static final Border NORMAL = new CompoundBorder(
            new LineBorder(new Color(210, 215, 220), 1, true),
            new EmptyBorder(18, 20, 18, 20));
    private static final Border SELECTED = new CompoundBorder(
            new LineBorder(Constants.PRIMARY_COLOR, 3, true),
            new EmptyBorder(16, 18, 16, 18));

    private final String role;
    private final String defaultUsername;
    private final Consumer<RoleCard> onSelect;
    private boolean selected;

    public RoleCard(String role, String defaultUsername, String description, String icon,
                    Consumer<RoleCard> onSelect) {
        this.role = role;
        this.defaultUsername = defaultUsername;
        this.onSelect = onSelect;
        setLayout(new BorderLayout(12, 0));
        setBackground(Constants.BG_WHITE);
        setBorder(NORMAL);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 28));
        iconLabel.setForeground(Constants.PRIMARY_COLOR);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel title = new JLabel(role);
        title.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 16));
        title.setForeground(Constants.TEXT_PRIMARY);
        JLabel desc = new JLabel(description);
        desc.setFont(Constants.FONT_SMALL);
        desc.setForeground(Constants.TEXT_SECONDARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        desc.setAlignmentX(LEFT_ALIGNMENT);
        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(desc);

        add(iconLabel, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (onSelect != null) onSelect.accept(RoleCard.this);
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (!selected) setBackground(new Color(245, 249, 255));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!selected) setBackground(Constants.BG_WHITE);
            }
        });
    }

    public String getRole() { return role; }
    public String getDefaultUsername() { return defaultUsername; }
    public boolean isSelected() { return selected; }

    public void setSelected(boolean v) {
        this.selected = v;
        setBorder(v ? SELECTED : NORMAL);
        setBackground(v ? new Color(235, 244, 255) : Constants.BG_WHITE);
        repaint();
    }

    @Override public Dimension getPreferredSize() { return new Dimension(300, 84); }
    @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 84); }
}
