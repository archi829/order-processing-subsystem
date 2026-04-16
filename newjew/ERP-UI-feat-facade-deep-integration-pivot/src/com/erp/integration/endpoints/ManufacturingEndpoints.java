package com.erp.integration.endpoints;

/**
 * Endpoint namespace for the Manufacturing subsystem.
 *
 * SOLID: ISP — per-module endpoint namespace. ManufacturingController
 *              depends only on these constants; Supply Chain callers do not
 *              see these strings, and vice-versa.
 *
 * Naming convention: {@code mfg/&lt;resource&gt;/&lt;action&gt;}.
 */
public interface ManufacturingEndpoints {
    String MFG_CARS_LIST                = "mfg/cars/list";
    String MFG_CAR_STATUS_UPDATE        = "mfg/cars/status";

    String MFG_PRODUCTION_ORDERS        = "mfg/production-orders/list";
    String MFG_PRODUCTION_ORDER_CREATE  = "mfg/production-orders/create";
    String MFG_PRODUCTION_ORDER_CANCEL  = "mfg/production-orders/cancel";

    String MFG_BOM_LIST                 = "mfg/bom/list";
    String MFG_BOM_DETAILS              = "mfg/bom/details";
    String MFG_BOM_CREATE               = "mfg/bom/create";

    String MFG_MATERIALS_LIST           = "mfg/materials/list";
    String MFG_MATERIAL_CREATE          = "mfg/materials/create";

    String MFG_ROUTING                  = "mfg/routing/list";
    String MFG_WORK_CENTERS             = "mfg/work-centers/list";

    String MFG_EXECUTION_LOG            = "mfg/execution/log";
    String MFG_EXECUTION_LOGS           = "mfg/execution/logs";
    String MFG_QC_SUBMIT                = "mfg/qc/submit";
    String MFG_STATS                    = "mfg/stats";
}
