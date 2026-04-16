package com.erp.exception;

import java.math.BigDecimal;

/**
 * Domain / business-rule violations raised by backend subsystems
 * (Manufacturing, Supply Chain) when a request is transport-valid but
 * violates a business invariant.
 *
 * SOLID: OCP — extends the existing exception hierarchy without modifying
 *              {@link ExceptionHandler}. The handler dispatches purely on
 *              {@link ERPException.Severity}, so new subclasses flow through
 *              it automatically.
 *
 * PATTERN: Factory Method (static factories) — each documented error code
 *          has a named factory so callers never hard-code message strings.
 */
public class BusinessRuleException extends ERPException {

    // ===== Manufacturing =====
    public static final String INVALID_BOM_STRUCTURE               = "INVALID_BOM_STRUCTURE";
    public static final String COMPONENT_STOCK_INSUFFICIENT        = "COMPONENT_STOCK_INSUFFICIENT";
        public static final String PRODUCTION_ORDER_OVERDUE            = "PRODUCTION_ORDER_OVERDUE";
        public static final String WORK_ORDER_ASSIGNMENT_FAILED        = "WORK_ORDER_ASSIGNMENT_FAILED";
        public static final String GI_ACCOUNT_MAPPING                  = "GI_ACCOUNT_MAPPING";
        public static final String UNAUTHORIZED_OPERATOR               = "UNAUTHORIZED_OPERATOR";
    public static final String ROUTING_STEP_GAP                    = "ROUTING_STEP_GAP";
    public static final String QC_DEFECT_THRESHOLD_EXCEEDED        = "QC_DEFECT_THRESHOLD_EXCEEDED";
    public static final String PRODUCTION_ORDER_CANCELLATION_BLOCKED = "PRODUCTION_ORDER_CANCELLATION_BLOCKED";
        public static final String BOM_COST_EXCEEDS_BUDGET            = "BOM_COST_EXCEEDS_BUDGET";
    public static final String CAPACITY_OVERLOAD                   = "CAPACITY_OVERLOAD";
        public static final String MACHINE_COMMUNICATION               = "MACHINE_COMMUNICATION";
    public static final String DUPLICATE_BOM_VERSION               = "DUPLICATE_BOM_VERSION";

    // ===== Supply Chain =====
    public static final String SUPPLIER_NOT_FOUND                  = "SUPPLIER_NOT_FOUND";
    public static final String GOODS_RECEIPT_MISMATCH              = "GOODS_RECEIPT_MISMATCH";
    public static final String INVOICE_MISMATCH                    = "INVOICE_MISMATCH";
    public static final String DUPLICATE_PO                        = "DUPLICATE_PO";
    public static final String STOCK_BELOW_THRESHOLD               = "STOCK_BELOW_THRESHOLD";
    public static final String SHIPMENT_DELAYED                    = "SHIPMENT_DELAYED";
    public static final String PAYMENT_PROCESSING_FAILED           = "PAYMENT_PROCESSING_FAILED";
    public static final String FOUR_EYES_RULE_VIOLATION            = "FOUR_EYES_RULE_VIOLATION";

    public BusinessRuleException(String code, String message, Severity severity) {
        super(code, message, severity);
    }

    // ===== Manufacturing factories =====

    public static BusinessRuleException invalidBomStructure(String bomId, String reason) {
        return new BusinessRuleException(INVALID_BOM_STRUCTURE,
                "BOM " + bomId + " has invalid structure: " + reason, Severity.MAJOR);
    }

    public static BusinessRuleException componentStockInsufficient(String partId, int required, int available) {
        return new BusinessRuleException(COMPONENT_STOCK_INSUFFICIENT,
                "Insufficient stock for part " + partId
                        + " (required=" + required + ", available=" + available + ")",
                Severity.MAJOR);
    }

    public static BusinessRuleException productionOrderOverdue(String orderId) {
        return new BusinessRuleException(PRODUCTION_ORDER_OVERDUE,
                "Production order #" + orderId + " has passed its planned end date without completion.",
                Severity.MAJOR);
    }

    public static BusinessRuleException workOrderAssignmentFailed(String operationName) {
        return new BusinessRuleException(WORK_ORDER_ASSIGNMENT_FAILED,
                "No available employee or work center found for operation " + operationName,
                Severity.MAJOR);
    }

    public static BusinessRuleException giAccountMapping(String costCenterCode) {
        return new BusinessRuleException(GI_ACCOUNT_MAPPING,
                "No valid General Ledger account found for cost center: " + costCenterCode,
                Severity.MAJOR);
    }

    public static BusinessRuleException unauthorizedOperator(String operatorId) {
        return new BusinessRuleException(UNAUTHORIZED_OPERATOR,
                "Operator " + operatorId + " lacks required certification for this work center.",
                Severity.MAJOR);
    }

    public static BusinessRuleException routingStepGap(String routingId, int missingStep) {
        return new BusinessRuleException(ROUTING_STEP_GAP,
                "Routing " + routingId + " has a gap at sequence " + missingStep,
                Severity.WARNING);
    }

    public static BusinessRuleException qcDefectThresholdExceeded(String orderId, double defectRate) {
        return new BusinessRuleException(QC_DEFECT_THRESHOLD_EXCEEDED,
                "Production order " + orderId + " defect rate "
                        + String.format("%.1f%%", defectRate * 100) + " exceeds threshold",
                Severity.WARNING);
    }

    public static BusinessRuleException productionOrderCancellationBlocked(String orderId) {
        return new BusinessRuleException(PRODUCTION_ORDER_CANCELLATION_BLOCKED,
                "Cannot cancel production order " + orderId + ": work orders are in progress",
                                Severity.WARNING);
    }

    public static BusinessRuleException capacityOverload(String workCenterId, double utilization) {
        return new BusinessRuleException(CAPACITY_OVERLOAD,
                "Work center " + workCenterId + " utilization "
                        + String.format("%.0f%%", utilization) + " exceeds 100%",
                Severity.WARNING);
    }

    public static BusinessRuleException duplicateBomVersion(String productId, String version) {
        return new BusinessRuleException(DUPLICATE_BOM_VERSION,
                "BOM version " + version + " already exists for product " + productId,
                Severity.WARNING);
    }

    public static BusinessRuleException bomCostExceedsBudget(String productName, BigDecimal actualCost, BigDecimal budgetLimit) {
        return new BusinessRuleException(BOM_COST_EXCEEDS_BUDGET,
                "Total BOM cost (" + actualCost + ") exceeds budget limit (" + budgetLimit + ") for product "
                        + productName,
                Severity.WARNING);
    }

    public static BusinessRuleException machineCommunication(String machineId) {
        return new BusinessRuleException(MACHINE_COMMUNICATION,
                "Heartbeat lost for Machine ID: " + machineId + " on shop floor.",
                Severity.WARNING);
    }

    // ===== Supply Chain factories =====

    public static BusinessRuleException supplierNotFound(String supplierId) {
        return new BusinessRuleException(SUPPLIER_NOT_FOUND,
                "Supplier '" + supplierId + "' does not exist or is not approved",
                Severity.MAJOR);
    }

    public static BusinessRuleException goodsReceiptMismatch(String poId, int expected, int received) {
        return new BusinessRuleException(GOODS_RECEIPT_MISMATCH,
                "Goods receipt for PO " + poId + " qty mismatch (expected "
                        + expected + ", received " + received + ")",
                Severity.MAJOR);
    }

    public static BusinessRuleException invoiceMismatch(String invoiceId, BigDecimal poAmount, BigDecimal invAmount) {
        return new BusinessRuleException(INVOICE_MISMATCH,
                "Invoice " + invoiceId + " amount " + invAmount
                        + " does not match PO amount " + poAmount,
                Severity.MAJOR);
    }

    public static BusinessRuleException duplicatePurchaseOrder(String supplierId) {
        return new BusinessRuleException(DUPLICATE_PO,
                "A similar open PO already exists for supplier " + supplierId,
                Severity.WARNING);
    }

    public static BusinessRuleException stockBelowThreshold(String partId, int level, int safetyStock) {
        return new BusinessRuleException(STOCK_BELOW_THRESHOLD,
                "Part " + partId + " stock (" + level + ") below safety level " + safetyStock,
                Severity.WARNING);
    }

    public static BusinessRuleException shipmentDelayed(String shipmentId) {
        return new BusinessRuleException(SHIPMENT_DELAYED,
                "Shipment " + shipmentId + " is delayed beyond its estimated arrival",
                Severity.INFO);
    }

    public static BusinessRuleException paymentProcessingFailed(String invoiceId, String reason) {
        return new BusinessRuleException(PAYMENT_PROCESSING_FAILED,
                "Payment for invoice " + invoiceId + " failed: " + reason,
                Severity.MAJOR);
    }

    public static BusinessRuleException fourEyesRuleViolation(String poId) {
        return new BusinessRuleException(FOUR_EYES_RULE_VIOLATION,
                "PO " + poId + " cannot be approved by the user who created it",
                Severity.MAJOR);
    }
}
