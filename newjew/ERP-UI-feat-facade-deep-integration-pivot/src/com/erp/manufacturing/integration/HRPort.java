package com.erp.manufacturing.integration;

public interface HRPort {
    boolean hasAssignableOperator(String workCenterId);
    boolean isCertified(String operatorId, String workCenterId);
}
