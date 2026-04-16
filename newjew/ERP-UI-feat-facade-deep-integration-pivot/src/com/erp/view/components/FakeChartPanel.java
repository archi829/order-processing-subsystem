package com.erp.view.components;

import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Pure paintComponent mock chart. Supports BAR or LINE style.
 * Purely visual — no data binding, used by facade panels.
 */
public class FakeChartPanel extends JPanel {

    public enum Style { BAR, LINE }

    private final String title;
    private final Style style;
    private int[] values;
    private String[] labels;

    public FakeChartPanel(String title, Style style) {
        this(title, style, new int[]{12, 18, 9, 22, 15, 28}, new String[]{"Jan","Feb","Mar","Apr","May","Jun"});
    }

    public FakeChartPanel(String title, Style style, int[] values, String[] labels) {
        this.title = title;
        this.style = style;
        this.values = values;
        this.labels = labels;
        setBackground(Constants.BG_WHITE);
        setBorder(new CompoundBorder(
                new LineBorder(new Color(225, 228, 232), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
    }

    @Override public Dimension getPreferredSize() { return new Dimension(400, 260); }

    public void setData(java.util.Map<String, Integer> data) {
        if (data == null || data.isEmpty()) { this.values = new int[0]; this.labels = new String[0]; repaint(); return; }
        int n = data.size();
        int[] v = new int[n];
        String[] l = new String[n];
        int i = 0;
        for (java.util.Map.Entry<String, Integer> e : data.entrySet()) {
            l[i] = e.getKey();
            v[i] = e.getValue() == null ? 0 : e.getValue();
            i++;
        }
        this.values = v; this.labels = l;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int pad = 24, top = 36, bottom = h - 40, left = 44, right = w - 24;

        g2.setColor(Constants.TEXT_PRIMARY);
        g2.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 14));
        g2.drawString(title, pad, 22);

        // axes
        g2.setColor(new Color(220, 224, 228));
        g2.drawLine(left, top, left, bottom);
        g2.drawLine(left, bottom, right, bottom);

        // gridlines
        for (int i = 1; i <= 4; i++) {
            int y = bottom - (bottom - top) * i / 4;
            g2.setColor(new Color(240, 242, 245));
            g2.drawLine(left + 1, y, right, y);
        }

        if (values == null || values.length == 0) { g2.dispose(); return; }
        int max = 1;
        for (int v : values) if (v > max) max = v;

        int n = values.length;
        int span = right - left;

        if (style == Style.BAR) {
            int barW = Math.max(12, span / (n * 2));
            int gap = (span - barW * n) / Math.max(1, n + 1);
            for (int i = 0; i < n; i++) {
                int barH = (int) ((bottom - top) * (values[i] / (double) max));
                int x = left + gap + i * (barW + gap);
                int y = bottom - barH;
                g2.setColor(Constants.PRIMARY_COLOR);
                g2.fillRoundRect(x, y, barW, barH, 6, 6);
                g2.setColor(Constants.TEXT_SECONDARY);
                g2.setFont(new Font(Constants.FONT_FAMILY, Font.PLAIN, 11));
                if (labels != null && i < labels.length)
                    g2.drawString(labels[i], x, bottom + 14);
                g2.drawString(String.valueOf(values[i]), x, y - 4);
            }
        } else {
            int step = span / Math.max(1, n - 1);
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = left + i * step;
                ys[i] = bottom - (int) ((bottom - top) * (values[i] / (double) max));
            }
            g2.setColor(Constants.PRIMARY_COLOR);
            g2.setStroke(new BasicStroke(2.5f));
            for (int i = 0; i < n - 1; i++) g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
            for (int i = 0; i < n; i++) {
                g2.setColor(Constants.PRIMARY_DARK);
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2.setColor(Constants.TEXT_SECONDARY);
                g2.setFont(new Font(Constants.FONT_FAMILY, Font.PLAIN, 11));
                if (labels != null && i < labels.length)
                    g2.drawString(labels[i], xs[i] - 10, bottom + 14);
            }
        }
        g2.dispose();
    }
}
