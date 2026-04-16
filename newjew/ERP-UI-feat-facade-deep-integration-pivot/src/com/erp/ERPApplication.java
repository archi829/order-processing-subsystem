package com.erp;

import com.erp.integration.MockUIService;
import com.erp.integration.ServiceLocator;
import com.erp.manufacturing.ManufacturingModuleFactory;
import com.erp.view.LoginFrame;

import javax.swing.*;

/**
 * FIX: This file replaces the empty stub ERPApplication.java.
 * Also delete ERPApplication(old).java — it causes a duplicate class error.
 */
public class ERPApplication {

    public static void main(String[] args) {
        setupLookAndFeel();

        if (Boolean.getBoolean("com.erp.mfg.sql")) {
            ServiceLocator.setUIService(
                    ManufacturingModuleFactory.createSqlBackedUIService(new MockUIService()));
        } else {
            ServiceLocator.setUIService(new MockUIService());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                System.out.println("ERP Application started successfully.");
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }

    private static void handleStartupError(Exception e) {
        System.err.println("Error starting application: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
                "Error starting application:\n" + e.getMessage(),
                "Startup Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}