package com.erp.view.components;

import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Reusable statistic card: accent stripe + title + big value + trend/subtitle line.
 */
public class DashboardCard extends JPanel {

    private final JLabel valueLabel;
    private final JLabel trendLabel;

    public DashboardCard(String title, String value, String trend, Color accent) {
        setLayout(new BorderLayout(0, 0));
        setBackground(Constants.BG_WHITE);
        setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 228, 232), 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel stripe = new JPanel();
        stripe.setPreferredSize(new Dimension(4, 0));
        stripe.setBackground(accent);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Constants.FONT_SMALL);
        titleLabel.setForeground(Constants.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 26));
        valueLabel.setForeground(Constants.TEXT_PRIMARY);

        trendLabel = new JLabel(trend);
        trendLabel.setFont(Constants.FONT_SMALL);
        trendLabel.setForeground(accent);

        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(LEFT_ALIGNMENT);
        trendLabel.setAlignmentX(LEFT_ALIGNMENT);

        body.add(titleLabel);
        body.add(Box.createVerticalStrut(4));
        body.add(valueLabel);
        body.add(Box.createVerticalStrut(4));
        body.add(trendLabel);

        add(stripe, BorderLayout.WEST);
        add(body, BorderLayout.CENTER);
    }

    public void setValue(String v) { valueLabel.setText(v); }
    public void setTrend(String t) { trendLabel.setText(t); }

    @Override public Dimension getPreferredSize() { return new Dimension(220, 100); }
}
