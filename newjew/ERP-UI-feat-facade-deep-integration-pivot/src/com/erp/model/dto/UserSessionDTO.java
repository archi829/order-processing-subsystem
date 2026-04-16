package com.erp.model.dto;

/**
 * DTO returned by IUIService.sendData("auth/login", ...).
 * Mirrors UserSession fields.
 */
public class UserSessionDTO {

    private String userId;
    private String displayName;
    private String role;
    private boolean valid;

    public UserSessionDTO() {}

    public UserSessionDTO(String userId, String displayName, String role, boolean valid) {
        this.userId = userId;
        this.displayName = displayName;
        this.role = role;
        this.valid = valid;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
