package com.erp.integration;

import com.erp.exception.IntegrationException;

import java.util.Map;

/**
 * Boundary interface between the UI and backend subsystems.
 *
 * SOLID: ISP — this interface is the transport contract only. It carries no
 *              module-specific endpoint constants; each subsystem has its own
 *              endpoint namespace interface under
 *              {@code com.erp.integration.endpoints.*} (AuthEndpoints,
 *              OrdersEndpoints, HREndpoints, ManufacturingEndpoints,
 *              SupplyChainEndpoints). A controller depends only on the
 *              namespace it uses, so adding a new module does not bloat the
 *              surface area seen by existing callers.
 *
 * Implementations may be mocks (in-memory), HTTP clients, or any other transport.
 */
public interface IUIService {

    /**
     * Read data from the backend.
     * @throws IntegrationException (FETCH_DATA_FAILED) on error
     */
    <T> T fetchData(String endpoint, Map<String, Object> params, Class<T> resultType)
            throws IntegrationException;

    /**
     * Write data to the backend.
     * @throws IntegrationException (SEND_DATA_FAILED) on error
     */
    <R> R sendData(String endpoint, Object payload, Class<R> resultType)
            throws IntegrationException;
}
