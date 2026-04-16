package com.erp.exception;

/**
 * Thrown when IUIService communication fails (fetch or send).
 * Major severity; UI handler offers a Retry action.
 */
public class IntegrationException extends ERPException {

    public static final String FETCH_DATA_FAILED = "FETCH_DATA_FAILED";
    public static final String SEND_DATA_FAILED = "SEND_DATA_FAILED";

    private final String endpoint;

    public IntegrationException(String code, String endpoint, String message) {
        super(code, message, Severity.MAJOR);
        this.endpoint = endpoint;
    }

    public IntegrationException(String code, String endpoint, String message, Throwable cause) {
        super(code, message, Severity.MAJOR, cause);
        this.endpoint = endpoint;
    }

    public String getEndpoint() { return endpoint; }

    public static IntegrationException fetchFailed(String endpoint, String reason) {
        return new IntegrationException(FETCH_DATA_FAILED, endpoint,
                "Failed to fetch data from '" + endpoint + "': " + reason);
    }

    public static IntegrationException sendFailed(String endpoint, String reason) {
        return new IntegrationException(SEND_DATA_FAILED, endpoint,
                "Failed to send data to '" + endpoint + "': " + reason);
    }
}
