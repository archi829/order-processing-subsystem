package com.erp.integration.endpoints;

/**
 * Endpoint namespace for authentication calls.
 *
 * SOLID: ISP — callers only depend on the constants they use.
 *              Each subsystem gets its own namespace so, e.g., HR panels
 *              never import Supply Chain endpoints (and vice-versa).
 */
public interface AuthEndpoints {
    String AUTH_LOGIN = "auth/login";
}
