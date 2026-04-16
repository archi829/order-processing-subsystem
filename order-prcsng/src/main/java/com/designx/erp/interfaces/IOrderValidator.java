package com.designx.erp.interfaces;

import com.designx.erp.model.Order;

/**
 * SOLID (ISP): Separate interface for validation responsibility.
 */
public interface IOrderValidator {
    /**
     * Validates a captured order.
     * @return true if valid; populates rejection reason on the order if invalid.
     */
    boolean validate(Order order);
}
