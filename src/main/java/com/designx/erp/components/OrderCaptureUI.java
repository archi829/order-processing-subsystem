package com.designx.erp.components;

import com.designx.erp.interfaces.ICustomerProfile;
import com.designx.erp.model.Order;

/**
 * GRASP (Creator): Responsible for creating Order objects — has all required data.
 * GRASP (Information Expert): Knows customer info via CRM and captures vehicle details.
 * SOLID (SRP): Single responsibility — capture and construct an order only.
 * SOLID (DIP): Depends on ICustomerProfile interface, not concrete CRM class.
 */
public class OrderCaptureUI {

    private final ICustomerProfile crmModule;

    public OrderCaptureUI(ICustomerProfile crmModule) {
        this.crmModule = crmModule;
    }

    /**
     * Captures a new vehicle order from customer input.
     * Reads customer profile from CRM Module and writes a raw Order object.
     *
     * @return a new Order in CAPTURED status
     * @throws IllegalArgumentException if the customer ID is not found in CRM
     */
    public Order captureOrder(String customerId,
                              String vehicleModel,
                              String vehicleVariant,
                              String vehicleColor,
                              String customFeaturesOrAddOns,
                              String orderDetails,
                              double orderValue) {

        if (!crmModule.isValidCustomer(customerId)) {
            throw new IllegalArgumentException("Invalid customer ID: " + customerId);
        }

        String customerName    = crmModule.getCustomerProfile(customerId);
        String contactDetails  = crmModule.getCustomerContactDetails(customerId);

        Order order = new Order(
                customerId, customerName, contactDetails,
                vehicleModel, vehicleVariant, vehicleColor,
                customFeaturesOrAddOns, orderDetails, orderValue
        );

        System.out.println("[OrderCaptureUI] Order captured: " + order);
        return order;
    }
}
