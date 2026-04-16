package com.erp.integration.endpoints;

/**
 * Endpoint namespace for the HR Management subsystem.
 *
 * SOLID: ISP — per-module endpoint namespace.
 */
public interface HREndpoints {
    String HR_EMPLOYEES         = "hr/employees";
    String HR_EMPLOYEE_UPDATE   = "hr/employee/update";
    String HR_RECRUITMENT       = "hr/recruitment";
    String HR_RECRUITMENT_STAGE = "hr/recruitment/stage";
    String HR_ONBOARDING        = "hr/onboarding";
    String HR_ONBOARDING_UPDATE = "hr/onboarding/update";
    String HR_PAYROLL           = "hr/payroll";
    String HR_PAYROLL_TRANSFER  = "hr/payroll/transfer";
    String HR_ATTENDANCE        = "hr/attendance";
    String HR_ATTENDANCE_LOG    = "hr/attendance/log";
    String HR_LEAVE             = "hr/leave";
    String HR_LEAVE_ACTION      = "hr/leave/action";
    String HR_PERFORMANCE       = "hr/performance";
    String HR_STATS             = "hr/stats";
}
