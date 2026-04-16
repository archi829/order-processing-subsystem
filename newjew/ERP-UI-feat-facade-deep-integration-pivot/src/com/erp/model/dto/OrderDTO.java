package com.erp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Order Processing (Team DesignX).
 *
 * Fields match the integration contract: orderId, customerName, carVIN, carModel,
 * chassisType, amount, date, status, paymentStatus.
 */
public class OrderDTO {

    // Status constants
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String REVISION = "REVISION";
    public static final String IN_TRANSIT = "IN_TRANSIT";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    // Payment status
    public static final String PAY_PENDING = "PENDING";
    public static final String PAY_PARTIAL = "PARTIAL";
    public static final String PAY_PAID = "PAID";
    public static final String PAY_FAILED = "FAILED";
    public static final String PAY_REFUNDED = "REFUNDED";

    private String orderId;
    private String customerName;
    private String carVIN;
    private String carModel;
    private String chassisType;
    private BigDecimal amount;
    private LocalDate date;
    private String status;
    private String paymentStatus;

    // Extended fields for workflows
    private String courier;
    private String trackingNumber;
    private String cancellationReason;
    private BigDecimal amountPaid;
    private String notes;

    public OrderDTO() {}

    public OrderDTO(String orderId, String customerName, String carVIN, String carModel,
                    String chassisType, BigDecimal amount, LocalDate date,
                    String status, String paymentStatus) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.carVIN = carVIN;
        this.carModel = carModel;
        this.chassisType = chassisType;
        this.amount = amount;
        this.date = date;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.amountPaid = BigDecimal.ZERO;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCarVIN() { return carVIN; }
    public void setCarVIN(String carVIN) { this.carVIN = carVIN; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public String getChassisType() { return chassisType; }
    public void setChassisType(String chassisType) { this.chassisType = chassisType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getCourier() { return courier; }
    public void setCourier(String courier) { this.courier = courier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String r) { this.cancellationReason = r; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return orderId + " - " + customerName + " (" + carModel + ")";
    }
}
