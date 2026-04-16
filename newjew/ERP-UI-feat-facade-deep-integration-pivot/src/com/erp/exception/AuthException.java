package com.erp.exception;

/**
 * Authentication and authorization exceptions (RBAC).
 */
public class AuthException extends ERPException {

    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String UNAUTHORIZED_MODULE_ACCESS = "UNAUTHORIZED_MODULE_ACCESS";
    public static final String BUTTON_ACCESS_DENIED = "BUTTON_ACCESS_DENIED";

    public AuthException(String code, String message, Severity severity) {
        super(code, message, severity);
    }

    public static AuthException invalidCredentials() {
        return new AuthException(INVALID_CREDENTIALS, "Invalid username or password", Severity.INFO);
    }

    public static AuthException unauthorizedModule(String module, String role) {
        return new AuthException(UNAUTHORIZED_MODULE_ACCESS,
                "Role '" + role + "' is not permitted to access module: " + module, Severity.MAJOR);
    }

    public static AuthException buttonAccessDenied(String action) {
        return new AuthException(BUTTON_ACCESS_DENIED,
                "You do not have permission to perform: " + action, Severity.WARNING);
    }
}
