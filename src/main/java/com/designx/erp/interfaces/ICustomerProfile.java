package com.designx.erp.interfaces;

/**
 * Interface for the CRM Module.
 * SOLID (DIP): Internal components depend on this abstraction, not on the concrete CRM system.
 * GRASP (Low Coupling): Decouples Order Processing from CRM implementation.
 *
 * External System: CRM Module (listed in ERP Software features as feature #12)
 */
public interface ICustomerProfile {

    /**
     * Retrieves the full customer profile string for a given customer ID.
     */
    String getCustomerProfile(String customerId);

    /**
     * Retrieves customer contact details.
     */
    String getCustomerContactDetails(String customerId);

    /**
     * Checks whether a customer ID is valid and exists.
     */
    boolean isValidCustomer(String customerId);
}
