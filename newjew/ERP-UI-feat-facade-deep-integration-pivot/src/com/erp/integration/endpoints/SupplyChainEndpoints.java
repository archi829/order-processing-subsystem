package com.erp.integration.endpoints;

/**
 * Endpoint namespace for the Supply Chain &amp; Purchasing subsystem (SwiftChain).
 *
 * SOLID: ISP — per-module endpoint namespace.
 *
 * Naming convention: {@code scm/&lt;resource&gt;/&lt;action&gt;}.
 */
public interface SupplyChainEndpoints {
    String SCM_SUPPLIERS        = "scm/suppliers/list";

    String SCM_PO_LIST          = "scm/po/list";
    String SCM_PO_CREATE        = "scm/po/create";
    String SCM_PO_APPROVE       = "scm/po/approve";

    String SCM_INVENTORY        = "scm/inventory/list";
    String SCM_LOW_STOCK        = "scm/inventory/low-stock";
    String SCM_REORDER          = "scm/inventory/reorder";

    String SCM_GRN_CREATE       = "scm/grn/create";
    String SCM_SHIPMENT_UPDATE  = "scm/shipment/update";

    String SCM_INVOICE_CREATE   = "scm/invoice/create";
    String SCM_INVOICE_VERIFY   = "scm/invoice/verify";
    String SCM_INVOICE_PAY      = "scm/invoice/pay";

    String SCM_STATS            = "scm/stats";
}
