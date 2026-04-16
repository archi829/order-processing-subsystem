package com.erp.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for HR Management (Team Jazz Girls).
 *
 * Contract fields: employeeId, name, role, department, assignedAssemblyLine,
 * shiftSchedule, hireDate, status. Plus extended fields for payroll, recruitment,
 * onboarding, attendance, performance workflows.
 */
public class EmployeeDTO {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_ON_LEAVE = "ON_LEAVE";
    public static final String STATUS_TERMINATED = "TERMINATED";
    public static final String STATUS_NEW = "NEW";

    // Core contract
    private String employeeId;
    private String name;
    private String role;
    private String department;
    private String assignedAssemblyLine;
    private String shiftSchedule;
    private LocalDate hireDate;
    private String status;

    // Payroll
    private BigDecimal grossSalary;
    private BigDecimal deductions;
    private BigDecimal netPay;
    private BigDecimal taxRecord;

    // Recruitment / ATS
    private String recruitmentStage; // APPLIED, SHORTLISTED, INTERVIEW, SELECTED, REJECTED
    private Integer interviewScore;

    // Onboarding
    private boolean backgroundCheckPassed;
    private boolean documentsVerified;
    private boolean onboardingVerified;

    // Performance
    private Integer performanceRating; // 1-5
    private String performanceFeedback;
    private String promotionStatus;

    public EmployeeDTO() {}

    public EmployeeDTO(String employeeId, String name, String role, String department,
                       String assignedAssemblyLine, String shiftSchedule,
                       LocalDate hireDate, String status) {
        this.employeeId = employeeId;
        this.name = name;
        this.role = role;
        this.department = department;
        this.assignedAssemblyLine = assignedAssemblyLine;
        this.shiftSchedule = shiftSchedule;
        this.hireDate = hireDate;
        this.status = status;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getAssignedAssemblyLine() { return assignedAssemblyLine; }
    public void setAssignedAssemblyLine(String l) { this.assignedAssemblyLine = l; }
    public String getShiftSchedule() { return shiftSchedule; }
    public void setShiftSchedule(String shiftSchedule) { this.shiftSchedule = shiftSchedule; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getNetPay() { return netPay; }
    public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    public BigDecimal getTaxRecord() { return taxRecord; }
    public void setTaxRecord(BigDecimal taxRecord) { this.taxRecord = taxRecord; }
    public String getRecruitmentStage() { return recruitmentStage; }
    public void setRecruitmentStage(String s) { this.recruitmentStage = s; }
    public Integer getInterviewScore() { return interviewScore; }
    public void setInterviewScore(Integer interviewScore) { this.interviewScore = interviewScore; }
    public boolean isBackgroundCheckPassed() { return backgroundCheckPassed; }
    public void setBackgroundCheckPassed(boolean v) { this.backgroundCheckPassed = v; }
    public boolean isDocumentsVerified() { return documentsVerified; }
    public void setDocumentsVerified(boolean v) { this.documentsVerified = v; }
    public boolean isOnboardingVerified() { return onboardingVerified; }
    public void setOnboardingVerified(boolean v) { this.onboardingVerified = v; }
    public Integer getPerformanceRating() { return performanceRating; }
    public void setPerformanceRating(Integer performanceRating) { this.performanceRating = performanceRating; }
    public String getPerformanceFeedback() { return performanceFeedback; }
    public void setPerformanceFeedback(String performanceFeedback) { this.performanceFeedback = performanceFeedback; }
    public String getPromotionStatus() { return promotionStatus; }
    public void setPromotionStatus(String promotionStatus) { this.promotionStatus = promotionStatus; }

    public void recalcNetPay() {
        if (grossSalary != null) {
            BigDecimal d = deductions == null ? BigDecimal.ZERO : deductions;
            BigDecimal t = taxRecord == null ? BigDecimal.ZERO : taxRecord;
            this.netPay = grossSalary.subtract(d).subtract(t);
        }
    }

    @Override public String toString() { return employeeId + " - " + name; }
}
