package com.erp.exception;

import javax.swing.JComponent;

/**
 * Module-specific validation errors. Carries an optional offending component
 * so the handler can highlight it (red border).
 */
public class ValidationException extends ERPException {

    public static final String NO_VIN_SELECTED = "NO_VIN_SELECTED";
    public static final String INVALID_REORDER_QUANTITY = "INVALID_REORDER_QUANTITY";
    public static final String REQUIRED_FIELD = "REQUIRED_FIELD";
    public static final String INVALID_QUANTITY_INPUT = "INVALID_QUANTITY_INPUT";

    private final transient JComponent offendingComponent;

    public ValidationException(String code, String message, JComponent offendingComponent) {
        super(code, message, Severity.WARNING);
        this.offendingComponent = offendingComponent;
    }

    public JComponent getOffendingComponent() { return offendingComponent; }

    public static ValidationException noVinSelected(JComponent table) {
        return new ValidationException(NO_VIN_SELECTED,
                "Please select a vehicle (VIN) from the table before continuing.", table);
    }

    public static ValidationException invalidReorderQuantity(JComponent field) {
        return new ValidationException(INVALID_REORDER_QUANTITY,
                "Reorder quantity must be a positive integer.", field);
    }

    public static ValidationException requiredField(String fieldName, JComponent field) {
        return new ValidationException(REQUIRED_FIELD,
                "Required field missing: " + fieldName, field);
    }

    public static ValidationException invalidQuantityInput(JComponent field, String value) {
        return new ValidationException(INVALID_QUANTITY_INPUT,
                "Quantity entered must be a positive non-zero number. Value '" + value + "' is invalid.",
                field);
    }
}
