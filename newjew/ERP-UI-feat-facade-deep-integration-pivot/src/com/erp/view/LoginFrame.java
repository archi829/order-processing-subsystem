package com.erp.view;

import com.erp.exception.AuthException;
import com.erp.exception.ERPException;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.IntegrationException;
import com.erp.integration.IUIService;
import com.erp.integration.ServiceLocator;
import com.erp.integration.endpoints.AuthEndpoints;
import com.erp.model.dto.UserSessionDTO;
import com.erp.session.UserSession;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Split-screen login frame.
 *
 *  - Left:  credentials form (username, password, login button, status)
 *           Lockout after 3 invalid attempts for 30 seconds.
 *  - Right: clickable role cards (Admin, Manager, Employee, HR, Sales).
 *
 * Authentication flows through IUIService.sendData("auth/login", ...).
 */
public class LoginFrame extends JFrame {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCKOUT_SECONDS = 30;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JLabel lockoutLabel;
    private final List<RoleSelectable> roleCards = new ArrayList<>();
    private RoleSelectable selectedRole;

    private int failedAttempts = 0;
    private Timer lockoutTimer;
    private int lockoutRemaining;

    private final IUIService ui = ServiceLocator.getUIService();

    public LoginFrame() {
        setTitle(Constants.APP_NAME + " - Login");
        setSize(980, 580);
        setMinimumSize(new Dimension(900, 540));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIHelper.centerWindow(this);
        setContentPane(buildSplit());
    }

    private JPanel buildSplit() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(Constants.BG_LIGHT);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        c.gridx = 0; c.weightx = 0.42;
        root.add(buildLeftForm(), c);

        c.gridx = 1; c.weightx = 0.58;
        root.add(buildRightRoles(), c);
        return root;
    }

    // -------------------- LEFT: credentials --------------------

    private JPanel buildLeftForm() {
        JPanel p = new JPanel();
        p.setBackground(Constants.BG_WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(50, 50, 40, 50));

        JLabel brand = new JLabel("TATA MOTORS");
        brand.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 30));
        brand.setForeground(Constants.TATA_NAVY);
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Enterprise Resource Planning  \u00B7  " + Constants.APP_SLOGAN);
        tagline.setFont(Constants.FONT_REGULAR);
        tagline.setForeground(Constants.TATA_GOLD.darker());
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel signIn = new JLabel("Sign in to continue");
        signIn.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 20));
        signIn.setForeground(Constants.TEXT_PRIMARY);
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = UIHelper.createTextField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = UIHelper.createPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginButton = UIHelper.createPrimaryButton("Login");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> attemptLogin());

        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) attemptLogin();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocus();
            }
        });

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Constants.FONT_SMALL);
        statusLabel.setForeground(Constants.DANGER_COLOR);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lockoutLabel = new JLabel(" ");
        lockoutLabel.setFont(Constants.FONT_SMALL);
        lockoutLabel.setForeground(Constants.DANGER_COLOR);
        lockoutLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("<html><span style='color:#7F8C8D'>Credentials: admin/admin123, "
                + "manager/manager123, emp001/emp123, hr_admin/hr123, sales01/sales123, "
                + "mfg_admin/mfg123, scm_admin/scm123</span></html>");
        hint.setFont(Constants.FONT_SMALL);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(brand);
        p.add(Box.createVerticalStrut(4));
        p.add(tagline);
        p.add(Box.createVerticalStrut(36));
        p.add(signIn);
        p.add(Box.createVerticalStrut(18));
        p.add(label("Username"));
        p.add(Box.createVerticalStrut(4));
        p.add(usernameField);
        p.add(Box.createVerticalStrut(14));
        p.add(label("Password"));
        p.add(Box.createVerticalStrut(4));
        p.add(passwordField);
        p.add(Box.createVerticalStrut(10));
        p.add(statusLabel);
        p.add(lockoutLabel);
        p.add(Box.createVerticalStrut(12));
        p.add(loginButton);
        p.add(Box.createVerticalGlue());
        p.add(hint);
        return p;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setFont(Constants.FONT_REGULAR);
        l.setForeground(Constants.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // -------------------- RIGHT: role cards --------------------

    private JPanel buildRightRoles() {
        JPanel p = new JPanel();
        p.setBackground(new Color(247, 250, 254));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(50, 40, 40, 50));

        JLabel title = new JLabel("Choose your role");
        title.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 22));
        title.setForeground(Constants.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Your selection shapes what modules you can access.");
        subtitle.setFont(Constants.FONT_SMALL);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(subtitle);
        p.add(Box.createVerticalStrut(20));

        String[][] roles = {
                {UserSession.ROLE_ADMIN,    "admin",     "Full access across every module",   "A"},
                {UserSession.ROLE_MANAGER,  "manager",   "Approve orders, view reports",      "M"},
                {UserSession.ROLE_EMPLOYEE, "emp001",    "Assembly-line & daily operations",  "E"},
                {UserSession.ROLE_HR,       "hr_admin",  "People, payroll, onboarding",       "H"},
                {UserSession.ROLE_SALES,    "sales01",   "Dealers, orders and customers",     "S"},
                {UserSession.ROLE_MFG,      "mfg_admin", "Shop floor, BOMs, production runs", "F"},
                {UserSession.ROLE_SCM,      "scm_admin", "Suppliers, POs, GRNs, invoices",    "C"},
        };

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String[] r : roles) {
            RoleSelectable card = new RoleSelectable(r[0], r[1], r[2], r[3]);
            roleCards.add(card);
            grid.add(card);
        }
        p.add(grid);
        p.add(Box.createVerticalGlue());

        JLabel tip = new JLabel("<html><i style='color:#7F8C8D'>Tip: selecting a role pre-fills "
                + "the username field. Click Login to continue.</i></html>");
        tip.setFont(Constants.FONT_SMALL);
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(tip);
        return p;
    }

    // -------------------- auth --------------------

    private void attemptLogin() {
        if (!loginButton.isEnabled()) return;
        statusLabel.setText(" ");

        String u = usernameField.getText().trim();
        String pw = new String(passwordField.getPassword());
        if (u.isEmpty()) { statusLabel.setText("Please enter your username."); return; }
        if (pw.isEmpty()) { statusLabel.setText("Please enter your password."); return; }

        setFormEnabled(false);
        SwingWorker<UserSessionDTO, Void> worker = new SwingWorker<UserSessionDTO, Void>() {
            @Override protected UserSessionDTO doInBackground() {
                Map<String, Object> payload = new HashMap<>();
                payload.put("username", u);
                payload.put("password", pw);
                payload.put("role", selectedRole != null ? selectedRole.role : null);
                return ui.sendData(AuthEndpoints.AUTH_LOGIN, payload, UserSessionDTO.class);
            }
            @Override protected void done() {
                try {
                    UserSessionDTO dto = get();
                    if (dto != null && dto.isValid()) {
                        failedAttempts = 0;
                        UserSession.getInstance().begin(dto.getUserId(), dto.getDisplayName(), dto.getRole());
                        openMainFrame();
                    } else {
                        onInvalidCredentials();
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof IntegrationException) {
                        ExceptionHandler.handle(LoginFrame.this, (ERPException) cause, LoginFrame.this::attemptLogin);
                    } else {
                        statusLabel.setText("Unexpected error: " + cause.getMessage());
                    }
                } finally {
                    setFormEnabled(lockoutTimer == null);
                }
            }
        };
        worker.execute();
    }

    private void onInvalidCredentials() {
        failedAttempts++;
        AuthException e = AuthException.invalidCredentials();
        if (failedAttempts >= MAX_ATTEMPTS) {
            startLockout();
        } else {
            statusLabel.setText(e.getMessage() + "  (attempt " + failedAttempts + "/" + MAX_ATTEMPTS + ")");
        }
        passwordField.setText("");
        passwordField.requestFocus();
    }

    private void startLockout() {
        lockoutRemaining = LOCKOUT_SECONDS;
        statusLabel.setText("Too many failed attempts.");
        updateLockoutLabel();
        setFormEnabled(false);
        lockoutTimer = new Timer(1000, e -> {
            lockoutRemaining--;
            if (lockoutRemaining <= 0) {
                lockoutTimer.stop();
                lockoutTimer = null;
                failedAttempts = 0;
                lockoutLabel.setText(" ");
                statusLabel.setText(" ");
                setFormEnabled(true);
            } else {
                updateLockoutLabel();
            }
        });
        lockoutTimer.start();
    }

    private void updateLockoutLabel() {
        lockoutLabel.setText("Account locked. Try again in " + lockoutRemaining + "s.");
    }

    private void setFormEnabled(boolean on) {
        usernameField.setEnabled(on);
        passwordField.setEnabled(on);
        loginButton.setEnabled(on);
        for (RoleSelectable c : roleCards) c.setEnabled(on);
    }

    private void openMainFrame() {
        MainFrame mf = new MainFrame();
        mf.setVisible(true);
        dispose();
    }

    // -------------------- inner RoleCard wrapper --------------------

    private class RoleSelectable extends com.erp.view.components.RoleCard {
        private final String role;
        RoleSelectable(String role, String username, String desc, String icon) {
            super(role, username, desc, icon, null);
            this.role = role;
            // override handler in-place via MouseListener defined in parent; we reach via child listeners
            for (java.awt.event.MouseListener ml : getMouseListeners()) removeMouseListener(ml);
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { select(); }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isSelected()) setBackground(new Color(245, 249, 255));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isSelected()) setBackground(Constants.BG_WHITE);
                }
            });
        }
        private void select() {
            for (RoleSelectable c : roleCards) c.setSelected(false);
            setSelected(true);
            selectedRole = this;
            usernameField.setText(getDefaultUsername());
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
}
