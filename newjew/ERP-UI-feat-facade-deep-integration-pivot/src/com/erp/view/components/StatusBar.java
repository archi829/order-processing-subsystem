package com.erp.view.components;

import com.erp.session.UserSession;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 24px persistent status ribbon at the bottom of the application shell.
 * Shows brand slogan, session identity, plant, fiscal year, last-synced time.
 * Refreshes once a minute.
 */
public class StatusBar extends JPanel {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final JLabel label = new JLabel();
    private final Timer timer;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBackground(Constants.TATA_NAVY);
        setBorder(new EmptyBorder(4, 18, 4, 18));
        setPreferredSize(new Dimension(0, 24));

        label.setFont(Constants.FONT_SMALL);
        label.setForeground(Constants.TATA_WHITE);
        add(label, BorderLayout.CENTER);

        refresh();
        timer = new Timer(60_000, e -> refresh());
        timer.setRepeats(true);
        timer.start();
    }

    public void refresh() {
        UserSession s = UserSession.getInstance();
        String who = s.isValid() ? s.getDisplayName() : "Guest";
        String role = s.isValid() ? s.getRole() : "-";
        String now = LocalTime.now().format(HH_MM);
        label.setText(Constants.APP_SLOGAN
                + "   \u00B7   Logged in as " + who
                + "   \u00B7   " + role
                + "   \u00B7   " + Constants.PLANT_NAME
                + "   \u00B7   " + Constants.FISCAL_YEAR
                + "   \u00B7   Last synced: " + now);
    }
}
