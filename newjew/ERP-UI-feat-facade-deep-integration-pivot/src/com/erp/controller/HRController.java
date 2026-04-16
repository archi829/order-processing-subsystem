package com.erp.controller;

import com.erp.exception.ERPException;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.IntegrationException;
import com.erp.integration.IUIService;
import com.erp.integration.ServiceLocator;
import com.erp.integration.endpoints.HREndpoints;
import com.erp.model.dto.EmployeeDTO;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for the HR Management module (Team Jazz Girls).
 *
 * Owns the IUIService, runs every call on a SwingWorker, publishes results
 * to registered HRListener observers. Exceptions flow through ExceptionHandler
 * with retry wiring.
 */
public class HRController {

    public interface HRListener {
        default void onEmployeesLoaded(List<EmployeeDTO> list) {}
        default void onRecruitmentLoaded(List<EmployeeDTO> list) {}
        default void onOnboardingLoaded(List<EmployeeDTO> list) {}
        default void onPayrollLoaded(List<EmployeeDTO> list) {}
        default void onPerformanceLoaded(List<EmployeeDTO> list) {}
        default void onAttendanceLoaded(List<String[]> rows) {}
        default void onLeaveLoaded(List<String[]> rows) {}
        default void onStatsLoaded(Map<String, Integer> stats) {}
        default void onEmployeeChanged(EmployeeDTO e) {}
    }

    private final IUIService ui = ServiceLocator.getUIService();
    private final List<HRListener> listeners = new ArrayList<>();

    public void addListener(HRListener l) { if (l != null) listeners.add(l); }
    public void removeListener(HRListener l) { listeners.remove(l); }

    // ===== Reads =====

    public void loadEmployees(Component owner, String department, String status, String q) {
        Map<String, Object> p = new HashMap<>();
        if (department != null) p.put("department", department);
        if (status != null) p.put("status", status);
        if (q != null) p.put("q", q);
        submit(owner,
                () -> HRController.<List<EmployeeDTO>>cast(ui.fetchData(HREndpoints.HR_EMPLOYEES, p, List.class)),
                list -> listeners.forEach(l -> l.onEmployeesLoaded(list)),
                () -> loadEmployees(owner, department, status, q));
    }

    public void loadRecruitment(Component owner) {
        submit(owner,
                () -> HRController.<List<EmployeeDTO>>cast(ui.fetchData(HREndpoints.HR_RECRUITMENT, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onRecruitmentLoaded(list)),
                () -> loadRecruitment(owner));
    }

    public void loadOnboarding(Component owner) {
        submit(owner,
                () -> HRController.<List<EmployeeDTO>>cast(ui.fetchData(HREndpoints.HR_ONBOARDING, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onOnboardingLoaded(list)),
                () -> loadOnboarding(owner));
    }

    public void loadPayroll(Component owner) {
        submit(owner,
                () -> HRController.<List<EmployeeDTO>>cast(ui.fetchData(HREndpoints.HR_PAYROLL, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onPayrollLoaded(list)),
                () -> loadPayroll(owner));
    }

    public void loadPerformance(Component owner) {
        submit(owner,
                () -> HRController.<List<EmployeeDTO>>cast(ui.fetchData(HREndpoints.HR_PERFORMANCE, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onPerformanceLoaded(list)),
                () -> loadPerformance(owner));
    }

    public void loadAttendance(Component owner) {
        submit(owner,
                () -> HRController.<List<String[]>>cast(ui.fetchData(HREndpoints.HR_ATTENDANCE, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onAttendanceLoaded(list)),
                () -> loadAttendance(owner));
    }

    public void loadLeave(Component owner) {
        submit(owner,
                () -> HRController.<List<String[]>>cast(ui.fetchData(HREndpoints.HR_LEAVE, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onLeaveLoaded(list)),
                () -> loadLeave(owner));
    }

    public void loadStats(Component owner) {
        submit(owner,
                () -> ui.fetchData(HREndpoints.HR_STATS, new HashMap<>(), Map.class),
                stats -> listeners.forEach(l -> l.onStatsLoaded(stats)),
                () -> loadStats(owner));
    }

    // ===== Writes =====

    public void updateEmployee(Component owner, EmployeeDTO dto, Consumer<EmployeeDTO> after) {
        submit(owner,
                () -> ui.sendData(HREndpoints.HR_EMPLOYEE_UPDATE, dto, EmployeeDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onEmployeeChanged(updated));
                    if (after != null) after.accept(updated);
                },
                () -> updateEmployee(owner, dto, after));
    }

    public void updateOnboarding(Component owner, EmployeeDTO dto, Runnable after) {
        submit(owner,
                () -> ui.sendData(HREndpoints.HR_ONBOARDING_UPDATE, dto, EmployeeDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onEmployeeChanged(updated));
                    if (after != null) after.run();
                },
                () -> updateOnboarding(owner, dto, after));
    }

    public void moveRecruitmentStage(Component owner, String employeeId, String stage, Integer score, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("employeeId", employeeId); p.put("stage", stage); p.put("score", score);
        submit(owner,
                () -> ui.sendData(HREndpoints.HR_RECRUITMENT_STAGE, p, EmployeeDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onEmployeeChanged(updated));
                    if (after != null) after.run();
                },
                () -> moveRecruitmentStage(owner, employeeId, stage, score, after));
    }

    public void transferSalary(Component owner, String employeeId, Runnable after) {
        submit(owner,
                () -> ui.sendData(HREndpoints.HR_PAYROLL_TRANSFER, employeeId, String.class),
                ok -> { if (after != null) after.run(); },
                () -> transferSalary(owner, employeeId, after));
    }

    public void logAttendance(Component owner, String employeeId, String checkIn, String checkOut,
                              String overtime, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("employeeId", employeeId); p.put("checkIn", checkIn);
        p.put("checkOut", checkOut); p.put("overtime", overtime);
        submit(owner,
                () -> ui.sendData(HREndpoints.HR_ATTENDANCE_LOG, p, String.class),
                ok -> { if (after != null) after.run(); },
                () -> logAttendance(owner, employeeId, checkIn, checkOut, overtime, after));
    }

    public void leaveAction(Component owner, String leaveId, String action, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", leaveId); p.put("action", action);
        submit(owner,
                () -> ui.sendData(HREndpoints.HR_LEAVE_ACTION, p, String.class),
                ok -> { if (after != null) after.run(); },
                () -> leaveAction(owner, leaveId, action, after));
    }

    // ===== helpers =====

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) { return (T) o; }

    private <T> void submit(Component owner,
                            java.util.concurrent.Callable<T> work,
                            Consumer<T> onOk,
                            Runnable retry) {
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return work.call(); }
            @Override protected void done() {
                try { onOk.accept(get()); }
                catch (Exception e) {
                    Throwable c = e.getCause() != null ? e.getCause() : e;
                    if (c instanceof IntegrationException) {
                        ExceptionHandler.handle(owner, (ERPException) c, retry);
                    } else if (c instanceof ERPException) {
                        ExceptionHandler.handle(owner, (ERPException) c);
                    } else {
                        ExceptionHandler.handle(owner,
                                IntegrationException.fetchFailed("hr", c.getMessage()), retry);
                    }
                }
            }
        }.execute();
    }
}
