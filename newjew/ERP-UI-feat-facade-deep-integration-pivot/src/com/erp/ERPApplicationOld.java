package com.erp;

import com.erp.integration.MockUIService;
import com.erp.integration.ServiceLocator;
import com.erp.manufacturing.ManufacturingModuleFactory;
import com.erp.view.LoginFrame;

import javax.swing.*;

/**
 * ERPApplication is the main entry point for the application.
 *
 * This demonstrates:
 *
 * 1. APPLICATION BOOTSTRAPPING: Setting up the environment before starting.
 *
 * 2. SWING THREADING: Using SwingUtilities.invokeLater() to ensure
 *    UI creation happens on the Event Dispatch Thread (EDT).
 *
 *    Why is this important?
 *    - Swing is NOT thread-safe
 *    - All UI operations must happen on the EDT
 *    - The main() method runs on the "main" thread, not the EDT
 *    - invokeLater() schedules code to run on the EDT
 *
 * 3. LOOK AND FEEL: Setting the system look and feel for native appearance.
 */
public class ERPApplication {

    /**
     * Main method - application entry point.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Set look and feel before creating any UI components
        setupLookAndFeel();

        // Configure service wiring before the UI boots.
        if (Boolean.getBoolean("com.erp.mfg.sql")) {
            ServiceLocator.setUIService(
                    ManufacturingModuleFactory.createSqlBackedUIService(new MockUIService()));
        } else {
            ServiceLocator.setUIService(new MockUIService());
        }

        // Schedule UI creation on the Event Dispatch Thread
        // This is a best practice for Swing applications
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and display the login frame
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);

                System.out.println("ERP Application started successfully.");
            } catch (Exception e) {
                // Handle any startup errors
                handleStartupError(e);
            }
        });
    }

    /**
     * Sets up the Look and Feel for the application.
     *
     * Look and Feel determines how UI components appear:
     * - System: Uses native OS appearance (Windows, Mac, Linux)
     * - Metal: Java's default cross-platform look
     * - Nimbus: Modern Java look (if available)
     *
     * We use System look for best integration with the OS.
     */
    private static void setupLookAndFeel() {
        try {
            // Try to use the system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Additional UI customizations can go here
            // For example, setting default colors, fonts, etc.
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);

        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            // If system L&F fails, Java will use the default Metal L&F
            System.err.println("Could not set system look and feel: " + e.getMessage());
            System.err.println("Using default Java Look and Feel.");
        }
    }

    /**
     * Handles errors that occur during application startup.
     *
     * @param e The exception that occurred
     */
    private static void handleStartupError(Exception e) {
        // Print error to console
        System.err.println("Error starting application: " + e.getMessage());
        e.printStackTrace();

        // Show error dialog to user
        JOptionPane.showMessageDialog(
            null,
            "Error starting application:\n" + e.getMessage(),
            "Startup Error",
            JOptionPane.ERROR_MESSAGE
        );

        // Exit with error code
        System.exit(1);
    }
}
