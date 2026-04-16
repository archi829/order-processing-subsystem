package com.designx.erp.external;

import com.designx.erp.interfaces.ICustomerProfile;

import java.util.Map;

/**
 * Stub adapter for the CRM Module (external system).
 *
 * SOLID (DIP + LSP): Implements ICustomerProfile; can be swapped for a real HTTP/REST
 *                    client that calls the actual CRM system without changing any
 *                    internal component code.
 *
 * External System reference: ERP Software feature #12 — CRM Module.
 * Provides: Customer Profile, Customer Contact Details
 */
public class CrmModuleAdapter implements ICustomerProfile {

    // Simulated in-memory CRM data store
    // Format: customerId → { fullName, contactNumber }
    private static final Map<String, String[]> CUSTOMER_DB = Map.of(
            "C001", new String[]{"Ravi Kumar",   "+91-9876543210"},
            "C002", new String[]{"Priya Sharma",  "+91-9123456789"},
            "C003", new String[]{"Arjun Nair",    "+91-9988776655"},
            "C004", new String[]{"Sunita Reddy",  "+91-9871234560"}
    );

    @Override
    public String getCustomerProfile(String customerId) {
        String[] data = CUSTOMER_DB.get(customerId);
        return (data != null) ? data[0] : "Unknown Customer";
    }

    @Override
    public String getCustomerContactDetails(String customerId) {
        String[] data = CUSTOMER_DB.get(customerId);
        return (data != null) ? data[1] : "N/A";
    }

    @Override
    public boolean isValidCustomer(String customerId) {
        return CUSTOMER_DB.containsKey(customerId);
    }
}
