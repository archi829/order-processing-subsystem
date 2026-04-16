package com.erp.integration;

import com.erp.exception.ERPException;
import com.erp.exception.IntegrationException;
import com.erp.integration.endpoints.ManufacturingEndpoints;
import com.erp.manufacturing.facade.ManufacturingFacade;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Proxy IUIService that routes only manufacturing endpoints to the SQL module,
 * and delegates all other endpoints to the fallback service.
 *
 * PATTERN: Proxy (Structural)
 */
public class SqlManufacturingUIService implements IUIService {

    private final IUIService fallback;
    private final ManufacturingFacade manufacturingFacade;

    public SqlManufacturingUIService(IUIService fallback, ManufacturingFacade manufacturingFacade) {
        this.fallback = fallback;
        this.manufacturingFacade = manufacturingFacade;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fetchData(String endpoint, Map<String, Object> params, Class<T> resultType)
            throws IntegrationException {
        try {
            switch (endpoint) {
                case ManufacturingEndpoints.MFG_CARS_LIST:
                    return (T) manufacturingFacade.listCars();
                case ManufacturingEndpoints.MFG_PRODUCTION_ORDERS:
                    return (T) manufacturingFacade.listProductionOrders();
                case ManufacturingEndpoints.MFG_BOM_LIST:
                    return (T) manufacturingFacade.listBoms();
                case ManufacturingEndpoints.MFG_BOM_DETAILS:
                    return (T) manufacturingFacade.getBomDetails(str(params, "bomId"));
                case ManufacturingEndpoints.MFG_MATERIALS_LIST:
                    return (T) manufacturingFacade.listMaterials();
                case ManufacturingEndpoints.MFG_ROUTING:
                    return (T) manufacturingFacade.listRouting(str(params, "productId"));
                case ManufacturingEndpoints.MFG_WORK_CENTERS:
                    return (T) manufacturingFacade.listWorkCenters();
                case ManufacturingEndpoints.MFG_EXECUTION_LOGS:
                    return (T) manufacturingFacade.listExecutionLogs(str(params, "orderId"));
                case ManufacturingEndpoints.MFG_STATS:
                    return (T) manufacturingFacade.stats();
                default:
                    return fallback.fetchData(endpoint, params, resultType);
            }
        } catch (RuntimeException e) {
            if (e instanceof ERPException) {
                throw e;
            }
            throw IntegrationException.fetchFailed(endpoint, e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R sendData(String endpoint, Object payload, Class<R> resultType)
            throws IntegrationException {
        try {
            switch (endpoint) {
                case ManufacturingEndpoints.MFG_CAR_STATUS_UPDATE:
                    return (R) manufacturingFacade.updateCarStatus(
                            str((Map<String, Object>) payload, "vin"),
                            str((Map<String, Object>) payload, "status"));
                case ManufacturingEndpoints.MFG_PRODUCTION_ORDER_CREATE:
                    return (R) manufacturingFacade.createProductionOrder((ProductionOrderDTO) payload);
                case ManufacturingEndpoints.MFG_BOM_CREATE:
                    return (R) manufacturingFacade.createBom((BomDTO) payload);
                case ManufacturingEndpoints.MFG_MATERIAL_CREATE:
                    return (R) manufacturingFacade.createMaterial((MaterialDTO) payload);
                case ManufacturingEndpoints.MFG_PRODUCTION_ORDER_CANCEL:
                    return (R) manufacturingFacade.cancelProductionOrder(payload.toString());
                case ManufacturingEndpoints.MFG_EXECUTION_LOG:
                    return (R) manufacturingFacade.recordExecutionLog(toExecutionLog(payload));
                case ManufacturingEndpoints.MFG_QC_SUBMIT:
                    return (R) manufacturingFacade.submitQCCheck((QCCheckDTO) payload);
                default:
                    return fallback.sendData(endpoint, payload, resultType);
            }
        } catch (RuntimeException e) {
            if (e instanceof ERPException) {
                throw e;
            }
            throw IntegrationException.sendFailed(endpoint, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static ExecutionLogDTO toExecutionLog(Object payload) {
        if (payload instanceof ExecutionLogDTO) {
            return (ExecutionLogDTO) payload;
        }
        if (!(payload instanceof Map)) {
            throw IntegrationException.sendFailed(ManufacturingEndpoints.MFG_EXECUTION_LOG,
                    "Unsupported execution payload type: " + payload.getClass().getName());
        }

        Map<String, Object> p = (Map<String, Object>) payload;
        ExecutionLogDTO dto = new ExecutionLogDTO();
        dto.setOrderId(str(p, "orderId"));
        dto.setOperatorId(str(p, "operatorId"));
        dto.setMachineId(str(p, "machineId"));
        dto.setNote(str(p, "note"));
        dto.setQtyProduced(num(p.get("qtyProduced")));
        dto.setScrapQty(num(p.get("scrapQty")));
        dto.setStartTime(ts(p.get("startTime")));
        dto.setEndTime(ts(p.get("endTime")));
        return dto;
    }

    private static LocalDateTime ts(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime) return (LocalDateTime) v;
        String s = v.toString();
        return s.trim().isEmpty() ? null : LocalDateTime.parse(s);
    }

    private static double num(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        String s = v.toString().trim();
        return s.isEmpty() ? 0 : Double.parseDouble(s);
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v == null ? null : v.toString();
    }
}
