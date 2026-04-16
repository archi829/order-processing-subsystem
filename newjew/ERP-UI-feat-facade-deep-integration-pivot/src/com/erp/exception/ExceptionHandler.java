package com.erp.exception;

import com.erp.session.UserSession;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized exception handler. Maps ERPException severity to dialogs,
 * UI highlights, and structured logs. Supports retry flows for integration errors.
 */
public final class ExceptionHandler {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Border RED_BORDER = new LineBorder(Color.RED, 2);

    private ExceptionHandler() {}

    /** Handle without a retry action. */
    public static void handle(Component owner, ERPException e) {
        handle(owner, e, null);
    }

    /** Handle with an optional retry runnable (only used for IntegrationException). */
    public static void handle(Component owner, ERPException e, Runnable retry) {
        log(e);

        if (e instanceof ValidationException) {
            highlight(((ValidationException) e).getOffendingComponent());
        }

        switch (e.getSeverity()) {
            case INFO:
                JOptionPane.showMessageDialog(owner, e.getMessage(),
                        "Notice", JOptionPane.INFORMATION_MESSAGE);
                break;
            case WARNING:
                JOptionPane.showMessageDialog(owner, e.getMessage(),
                        "Warning", JOptionPane.WARNING_MESSAGE);
                break;
            case MAJOR:
                if (e instanceof IntegrationException && retry != null) {
                    int choice = JOptionPane.showOptionDialog(owner,
                            e.getMessage() + "\n\nWould you like to retry?",
                            "Error [" + e.getCode() + "]",
                            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
                            null, new Object[]{"Retry", "Cancel"}, "Retry");
                    if (choice == JOptionPane.YES_OPTION) {
                        new SwingWorker<Void, Void>() {
                            @Override protected Void doInBackground() { retry.run(); return null; }
                        }.execute();
                    }
                } else {
                    JOptionPane.showMessageDialog(owner, e.getMessage(),
                            "Error [" + e.getCode() + "]", JOptionPane.ERROR_MESSAGE);
                }
                break;
        }
    }

    private static void highlight(JComponent c) {
        if (c == null) return;
        Border original = c.getBorder();
        c.setBorder(RED_BORDER);
        Timer t = new Timer(2500, ev -> c.setBorder(original));
        t.setRepeats(false);
        t.start();
    }

    private static void log(ERPException e) {
        String user = "<anonymous>";
        try {
            UserSession s = UserSession.getInstance();
            if (s.isValid()) user = s.getUserId() + "/" + s.getRole();
        } catch (Throwable ignored) {}
        System.err.printf("[%s] [%s] [%s] user=%s msg=%s%n",
                LocalDateTime.now().format(TS),
                e.getSeverity(), e.getCode(), user, e.getMessage());
    }
}
