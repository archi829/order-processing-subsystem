package com.erp.session;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for which roles may open which modules.
 * Consumed by both the navigation render (Sidebar) and the enforcement
 * gate (MainFrame.showPanel).
 */
public final class RoleAccess {

    private RoleAccess() {}

    private static final Map<String, Set<String>> ACCESS = new HashMap<>();

    static {
        Set<String> all = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_MANAGER, UserSession.ROLE_EMPLOYEE,
                UserSession.ROLE_HR, UserSession.ROLE_SALES,
                UserSession.ROLE_MFG, UserSession.ROLE_SCM));
        Set<String> adminOnly = new HashSet<>(Collections.singletonList(UserSession.ROLE_ADMIN));
        Set<String> adminMgr = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_MANAGER));
        Set<String> mgrOps = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_MANAGER, UserSession.ROLE_SALES));
        Set<String> hrStaff = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_HR));
        Set<String> employee = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_MANAGER,
                UserSession.ROLE_EMPLOYEE, UserSession.ROLE_SALES));
        Set<String> mfgStaff = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_MANAGER, UserSession.ROLE_MFG));
        Set<String> scmStaff = new HashSet<>(Arrays.asList(
                UserSession.ROLE_ADMIN, UserSession.ROLE_MANAGER, UserSession.ROLE_SCM));

        ACCESS.put("dashboard",     all);
        ACCESS.put("order",         employee);
        ACCESS.put("hr",            hrStaff);
        ACCESS.put("crm",           mgrOps);
        ACCESS.put("sales",         mgrOps);
        ACCESS.put("inventory",     scmStaff);
        ACCESS.put("manufacturing", mfgStaff);
        ACCESS.put("finance",       adminMgr);
        ACCESS.put("accounting",    adminMgr);
        ACCESS.put("project",       adminMgr);
        ACCESS.put("reporting",     adminMgr);
        ACCESS.put("analytics",     adminMgr);
        ACCESS.put("bi",            adminMgr);
        ACCESS.put("marketing",     mgrOps);
        ACCESS.put("automation",    adminOnly);
    }

    public static boolean canAccess(String command, String role) {
        Set<String> allowed = ACCESS.get(command);
        return allowed == null || (role != null && allowed.contains(role));
    }

    public static Set<String> rolesFor(String command) {
        return ACCESS.getOrDefault(command, Collections.emptySet());
    }
}
