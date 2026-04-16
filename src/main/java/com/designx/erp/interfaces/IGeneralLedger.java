package com.designx.erp.interfaces;

import com.designx.erp.model.Invoice;
import com.designx.erp.model.Payment;

/**
 * Interface for the Finance Module (General Ledger).
 * SOLID (DIP): Billing Generator and Payment Processor depend on this abstraction.
 * GRASP (Low Coupling): Decouples billing/payment from the Finance system.
 *
 * External System: Finance Module (listed in ERP Software features as #1 Financial management,
 * #2 Accounting — provides general ledger updates, accounting entries, and payment recording).
 *
 * Named IGeneralLedger to match the component diagram connector label.
 */
public interface IGeneralLedger {

    /**
     * Posts an invoice to the Finance module (accounts receivable entry).
     */
    void postInvoice(Invoice invoice);

    /**
     * Records a successful payment in the accounting ledger.
     */
    void recordPayment(Payment payment);

    /**
     * Returns accounting entry summary for a given order.
     */
    String getAccountingEntries(String orderId);
}
