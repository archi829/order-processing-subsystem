package com.designx.erp.components;

import com.designx.erp.interfaces.IGeneralLedger;
import com.designx.erp.model.Invoice;
import com.designx.erp.model.Order;
import com.designx.erp.model.OrderStatus;

/**
 * GRASP (Creator): Creates Invoice objects — has all the required order and tax data.
 * SOLID (SRP): Only generates invoices; delegates ledger posting to the Finance Module.
 * SOLID (DIP): Depends on IGeneralLedger abstraction, not a concrete Finance class.
 *
 * Reads:  Order ID, Customer Details (from Order)
 * Writes: Invoice ID, Invoice Amount, Tax Details
 *         → also posts the invoice to Finance Module via IGeneralLedger
 */
public class BillingGenerator {

    /** GST rate applicable to vehicles in India (28%). */
    private static final double GST_RATE = 0.28;

    private final IGeneralLedger financeModule;

    public BillingGenerator(IGeneralLedger financeModule) {
        this.financeModule = financeModule;
    }

    /**
     * Generates an invoice for a fulfilled order and posts it to the Finance Module.
     *
     * @param order a FULFILLED order
     * @return generated Invoice
     */
    public Invoice generateInvoice(Order order) {
        double taxAmount = order.getOrderValue() * GST_RATE;

        String customerDetails = order.getCustomerName()
                + " | " + order.getCustomerContactDetails()
                + " | CustomerID: " + order.getCustomerId();

        Invoice invoice = new Invoice(
                order.getOrderId(),
                customerDetails,
                order.getOrderValue(),
                taxAmount
        );

        // Post to Finance Module (General Ledger — accounts receivable entry)
        financeModule.postInvoice(invoice);

        System.out.println("[BillingGenerator] Invoice generated: " + invoice);
        return invoice;
    }
}
