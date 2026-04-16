package com.erp.exception;

/**
 * Base class for all ERP-specific exceptions.
 * Carries a Severity level that drives UI handling (dialog type, retry options).
 */
public abstract class ERPException extends RuntimeException {

    public enum Severity { INFO, WARNING, MAJOR }

    private final String code;
    private final Severity severity;

    protected ERPException(String code, String message, Severity severity) {
        super(message);
        this.code = code;
        this.severity = severity;
    }

    protected ERPException(String code, String message, Severity severity, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.severity = severity;
    }

    public String getCode() { return code; }
    public Severity getSeverity() { return severity; }
}
