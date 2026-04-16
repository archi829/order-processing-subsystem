package com.erp.integration;

import com.erp.exception.BusinessRuleException;
import com.erp.exception.IntegrationException;
import com.erp.integration.endpoints.AuthEndpoints;
import com.erp.integration.endpoints.HREndpoints;
import com.erp.integration.endpoints.ManufacturingEndpoints;
import com.erp.integration.endpoints.OrdersEndpoints;
import com.erp.integration.endpoints.SupplyChainEndpoints;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.BomItemDTO;
import com.erp.model.dto.CarModelDTO;
import com.erp.model.dto.EmployeeDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.GoodsReceiptDTO;
import com.erp.model.dto.InvoiceDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.model.dto.OrderDTO;
import com.erp.model.dto.POLineItemDTO;
import com.erp.model.dto.PartDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.PurchaseOrderDTO;
import com.erp.model.dto.QCCheckDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.model.dto.ShipmentDTO;
import com.erp.model.dto.SupplierDTO;
import com.erp.model.dto.UserSessionDTO;
import com.erp.model.dto.WorkCenterDTO;
import com.erp.session.UserSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory reference implementation of {@link IUIService}.
 *
 * GRASP: Information Expert — this class owns the in-memory state for every
 *        module, so business-rule validation (stock levels, PO/GRN matching,
 *        routing gaps, four-eyes approval, cancellation guards) lives here
 *        where the data lives.
 *
 * Simulates a network with ~150 ms latency. A test hook {@link #setFailNext(boolean)}
 * forces the next call to throw an {@link IntegrationException} so the UI's
 * retry flow can be demonstrated without real infrastructure.
 */
public class MockUIService implements IUIService {

    // ===== Orders / HR state (existing modules) =====
    private final List<OrderDTO> orders = new ArrayList<>();
    private final List<EmployeeDTO> employees = new ArrayList<>();
    private final List<String[]> leaveRequests = new ArrayList<>();
    private final List<String[]> attendanceLog = new ArrayList<>();
    private final List<String[]> activityLog = new ArrayList<>();

    // ===== Manufacturing state =====
    private final List<CarModelDTO> cars = new ArrayList<>();
    private final List<BomDTO> boms = new ArrayList<>();
    private final List<ProductionOrderDTO> productionOrders = new ArrayList<>();
    private final List<RoutingStepDTO> routingSteps = new ArrayList<>();
    private final List<WorkCenterDTO> workCenters = new ArrayList<>();
    private final List<ExecutionLogDTO> executionLogs = new ArrayList<>();
    private final List<QCCheckDTO> qcChecks = new ArrayList<>();
    private final List<MaterialDTO> materialCatalog = new ArrayList<>();

    // ===== Supply Chain state =====
    private final List<SupplierDTO> suppliers = new ArrayList<>();
    private final List<PurchaseOrderDTO> purchaseOrders = new ArrayList<>();
    private final List<PartDTO> parts = new ArrayList<>();
    private final List<GoodsReceiptDTO> goodsReceipts = new ArrayList<>();
    private final List<ShipmentDTO> shipments = new ArrayList<>();
    private final List<InvoiceDTO> invoices = new ArrayList<>();

    private final AtomicInteger orderSeq = new AtomicInteger(1000);
    private final AtomicInteger leaveSeq = new AtomicInteger(500);
    private final AtomicInteger attendanceSeq = new AtomicInteger(900);
    private final AtomicInteger prodOrderSeq = new AtomicInteger(700);
    private final AtomicInteger qcSeq = new AtomicInteger(300);
    private final AtomicInteger poSeq = new AtomicInteger(5000);
    private final AtomicInteger grnSeq = new AtomicInteger(800);
    private final AtomicInteger shipmentSeq = new AtomicInteger(600);
    private final AtomicInteger invoiceSeq = new AtomicInteger(900);

    private volatile boolean failNext = false;
    private volatile long latencyMs = 150L;

    public MockUIService() {
        seedOrders();
        seedEmployees();
        seedLeave();
        seedAttendance();
        seedManufacturing();
        seedSupplyChain();
        seedMaterialCatalog();
    }

    // ==================== Test hooks ====================
    public void setFailNext(boolean v) { this.failNext = v; }
    public void setLatencyMs(long ms) { this.latencyMs = ms; }

    // ==================== IUIService ====================

    @Override
    @SuppressWarnings("unchecked")
    public <T> T fetchData(String endpoint, Map<String, Object> params, Class<T> resultType)
            throws IntegrationException {
        simulate(endpoint, true);
        Map<String, Object> p = params == null ? new HashMap<>() : params;
        try {
            switch (endpoint) {
                // Orders
                case "orders/list":      return (T) filterOrders(p);
                case "orders/stats":     return (T) orderStats();
                // HR
                case HREndpoints.HR_EMPLOYEES:     return (T) filterEmployees(p);
                case HREndpoints.HR_RECRUITMENT:   return (T) recruitmentPipeline();
                case HREndpoints.HR_ONBOARDING:    return (T) onboardingList();
                case HREndpoints.HR_PAYROLL:       return (T) payrollList();
                case HREndpoints.HR_ATTENDANCE:    return (T) new ArrayList<>(attendanceLog);
                case HREndpoints.HR_LEAVE:         return (T) new ArrayList<>(leaveRequests);
                case HREndpoints.HR_PERFORMANCE:   return (T) performanceList();
                case HREndpoints.HR_STATS:         return (T) hrStats();
                // Manufacturing
                case ManufacturingEndpoints.MFG_CARS_LIST:        return (T) new ArrayList<>(cars);
                case ManufacturingEndpoints.MFG_PRODUCTION_ORDERS:return (T) new ArrayList<>(productionOrders);
                case ManufacturingEndpoints.MFG_BOM_LIST:         return (T) new ArrayList<>(boms);
                case ManufacturingEndpoints.MFG_BOM_DETAILS:      return (T) findBomDetails(str(p, "bomId"));
                case ManufacturingEndpoints.MFG_MATERIALS_LIST:   return (T) new ArrayList<>(materialCatalog);
                case ManufacturingEndpoints.MFG_ROUTING:          return (T) routingFor(str(p, "productId"));
                case ManufacturingEndpoints.MFG_WORK_CENTERS:     return (T) new ArrayList<>(workCenters);
                case ManufacturingEndpoints.MFG_EXECUTION_LOGS:   return (T) executionLogsFor(str(p, "orderId"));
                case ManufacturingEndpoints.MFG_STATS:            return (T) mfgStats();
                // Supply Chain
                case SupplyChainEndpoints.SCM_SUPPLIERS:          return (T) new ArrayList<>(suppliers);
                case SupplyChainEndpoints.SCM_PO_LIST:            return (T) new ArrayList<>(purchaseOrders);
                case SupplyChainEndpoints.SCM_INVENTORY:          return (T) new ArrayList<>(parts);
                case SupplyChainEndpoints.SCM_LOW_STOCK:          return (T) lowStockParts();
                case SupplyChainEndpoints.SCM_STATS:              return (T) scmStats();
                default:
                    throw IntegrationException.fetchFailed(endpoint, "Unknown endpoint");
            }
        } catch (IntegrationException | BusinessRuleException ie) { throw ie; }
          catch (Exception e) { throw IntegrationException.fetchFailed(endpoint, e.getMessage()); }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R sendData(String endpoint, Object payload, Class<R> resultType)
            throws IntegrationException {
        simulate(endpoint, false);
        try {
            switch (endpoint) {
                // Auth
                case AuthEndpoints.AUTH_LOGIN:          return (R) login((Map<String, Object>) payload);
                // Orders
                case "orders/create":   return (R) createOrder((OrderDTO) payload);
                case "orders/approve":  return (R) updateStatus((String) payload, OrderDTO.APPROVED, "Approved");
                case "orders/reject":   return (R) updateStatus((String) payload, OrderDTO.REJECTED, "Rejected");
                case "orders/revision": return (R) updateStatus((String) payload, OrderDTO.REVISION, "Sent back for revision");
                case "orders/ship":     return (R) ship((Map<String, Object>) payload);
                case "orders/pay":      return (R) pay((Map<String, Object>) payload);
                case "orders/cancel":   return (R) cancel((Map<String, Object>) payload);
                // HR
                case HREndpoints.HR_EMPLOYEE_UPDATE:    return (R) updateEmployee((EmployeeDTO) payload);
                case HREndpoints.HR_RECRUITMENT_STAGE: return (R) moveRecruitmentStage((Map<String, Object>) payload);
                case HREndpoints.HR_ONBOARDING_UPDATE:  return (R) updateEmployee((EmployeeDTO) payload);
                case HREndpoints.HR_PAYROLL_TRANSFER:   return (R) transferSalary((String) payload);
                case HREndpoints.HR_ATTENDANCE_LOG:     return (R) logAttendance((Map<String, Object>) payload);
                case HREndpoints.HR_LEAVE_ACTION:       return (R) leaveAction((Map<String, Object>) payload);
                // Manufacturing
                case ManufacturingEndpoints.MFG_CAR_STATUS_UPDATE:       return (R) updateCarStatus((Map<String, Object>) payload);
                case ManufacturingEndpoints.MFG_PRODUCTION_ORDER_CREATE: return (R) createProductionOrder((ProductionOrderDTO) payload);
                case ManufacturingEndpoints.MFG_BOM_CREATE:              return (R) createBom((BomDTO) payload);
                case ManufacturingEndpoints.MFG_MATERIAL_CREATE:         return (R) createMaterial((MaterialDTO) payload);
                case ManufacturingEndpoints.MFG_PRODUCTION_ORDER_CANCEL: return (R) cancelProductionOrder((String) payload);
                case ManufacturingEndpoints.MFG_EXECUTION_LOG:
                    if (payload instanceof ExecutionLogDTO) return (R) recordExecution((ExecutionLogDTO) payload);
                    return (R) recordExecution((Map<String, Object>) payload);
                case ManufacturingEndpoints.MFG_QC_SUBMIT:               return (R) submitQC((QCCheckDTO) payload);
                // Supply Chain
                case SupplyChainEndpoints.SCM_PO_CREATE:         return (R) createPurchaseOrder((PurchaseOrderDTO) payload);
                case SupplyChainEndpoints.SCM_PO_APPROVE:        return (R) approvePurchaseOrder((Map<String, Object>) payload);
                case SupplyChainEndpoints.SCM_REORDER:           return (R) reorderPart((Map<String, Object>) payload);
                case SupplyChainEndpoints.SCM_GRN_CREATE:        return (R) createGoodsReceipt((GoodsReceiptDTO) payload);
                case SupplyChainEndpoints.SCM_SHIPMENT_UPDATE:   return (R) updateShipment((Map<String, Object>) payload);
                case SupplyChainEndpoints.SCM_INVOICE_CREATE:    return (R) createInvoice((InvoiceDTO) payload);
                case SupplyChainEndpoints.SCM_INVOICE_VERIFY:    return (R) verifyInvoice((String) payload);
                case SupplyChainEndpoints.SCM_INVOICE_PAY:       return (R) payInvoice((String) payload);
                default:
                    throw IntegrationException.sendFailed(endpoint, "Unknown endpoint");
            }
        } catch (IntegrationException | BusinessRuleException ie) { throw ie; }
          catch (Exception e) { throw IntegrationException.sendFailed(endpoint, e.getMessage()); }
    }

    private void simulate(String endpoint, boolean fetch) throws IntegrationException {
        try { Thread.sleep(latencyMs); } catch (InterruptedException ignored) {}
        if (failNext) {
            failNext = false;
            if (fetch) throw IntegrationException.fetchFailed(endpoint, "Simulated network failure");
            else throw IntegrationException.sendFailed(endpoint, "Simulated network failure");
        }
    }

    // ==================== Auth ====================

    private UserSessionDTO login(Map<String, Object> creds) {
        String u = str(creds, "username");
        String p = str(creds, "password");
        String role = str(creds, "role");
        Map<String, String[]> db = new HashMap<>();
        db.put("admin",    new String[]{"admin123",    "System Administrator", UserSession.ROLE_ADMIN});
        db.put("manager",  new String[]{"manager123",  "Ravi Manager",         UserSession.ROLE_MANAGER});
        db.put("emp001",   new String[]{"emp123",      "Arjun Verma",          UserSession.ROLE_EMPLOYEE});
        db.put("hr_admin", new String[]{"hr123",       "Kavita Joshi",         UserSession.ROLE_HR});
        db.put("sales01",  new String[]{"sales123",    "Imran Ali",            UserSession.ROLE_SALES});
        db.put("mfg_admin",new String[]{"mfg123",      "Rohan Shetty",         UserSession.ROLE_MFG});
        db.put("scm_admin",new String[]{"scm123",      "Priya Nair",           UserSession.ROLE_SCM});
        db.put("employee", db.get("emp001"));
        db.put("hr",       db.get("hr_admin"));
        db.put("sales",    db.get("sales01"));
        db.put("mfg",      db.get("mfg_admin"));
        db.put("scm",      db.get("scm_admin"));
        String[] row = db.get(u == null ? "" : u.toLowerCase());
        if (row == null || !row[0].equals(p)) {
            return new UserSessionDTO(null, null, null, false);
        }
        String effectiveRole = (role == null || role.isEmpty()) ? row[2] : role;
        return new UserSessionDTO(u, row[1], effectiveRole, true);
    }

    // ==================== Orders ====================

    private List<OrderDTO> filterOrders(Map<String, Object> p) {
        String status = str(p, "status");
        String q = str(p, "q");
        List<OrderDTO> out = new ArrayList<>();
        for (OrderDTO o : orders) {
            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(o.getStatus())) continue;
            if (q != null && !q.isEmpty()) {
                String hay = (o.getOrderId() + " " + o.getCustomerName() + " "
                        + o.getCarVIN() + " " + o.getCarModel()).toLowerCase();
                if (!hay.contains(q.toLowerCase())) continue;
            }
            out.add(o);
        }
        return out;
    }

    private Map<String, Integer> orderStats() {
        Map<String, Integer> s = new HashMap<>();
        s.put("total", orders.size());
        s.put("pending", 0); s.put("approved", 0); s.put("inTransit", 0);
        s.put("delivered", 0); s.put("cancelled", 0);
        for (OrderDTO o : orders) {
            switch (o.getStatus()) {
                case OrderDTO.PENDING:    s.merge("pending", 1, Integer::sum); break;
                case OrderDTO.APPROVED:   s.merge("approved", 1, Integer::sum); break;
                case OrderDTO.IN_TRANSIT: s.merge("inTransit", 1, Integer::sum); break;
                case OrderDTO.DELIVERED:  s.merge("delivered", 1, Integer::sum); break;
                case OrderDTO.CANCELLED:  s.merge("cancelled", 1, Integer::sum); break;
                default: break;
            }
        }
        return s;
    }

    private OrderDTO findOrder(String id) {
        for (OrderDTO o : orders) if (o.getOrderId().equals(id)) return o;
        throw IntegrationException.sendFailed("orders", "Order not found: " + id);
    }

    private OrderDTO createOrder(OrderDTO dto) {
        dto.setOrderId("ORD-" + orderSeq.incrementAndGet());
        if (dto.getDate() == null) dto.setDate(LocalDate.now());
        dto.setStatus(OrderDTO.PENDING);
        dto.setPaymentStatus(OrderDTO.PAY_PENDING);
        if (dto.getAmountPaid() == null) dto.setAmountPaid(BigDecimal.ZERO);
        orders.add(0, dto);
        activity("Order created: " + dto.getOrderId() + " (" + dto.getCustomerName() + ")");
        return dto;
    }

    private OrderDTO updateStatus(String orderId, String status, String action) {
        OrderDTO o = findOrder(orderId);
        o.setStatus(status);
        activity("Order " + orderId + ": " + action);
        return o;
    }

    private OrderDTO ship(Map<String, Object> p) {
        OrderDTO o = findOrder(str(p, "orderId"));
        o.setStatus(OrderDTO.IN_TRANSIT);
        o.setCourier(str(p, "courier"));
        o.setTrackingNumber(str(p, "tracking"));
        activity("Order " + o.getOrderId() + " shipped via " + o.getCourier());
        return o;
    }

    private OrderDTO pay(Map<String, Object> p) {
        OrderDTO o = findOrder(str(p, "orderId"));
        BigDecimal amt = (BigDecimal) p.get("amount");
        boolean fail = Boolean.TRUE.equals(p.get("simulateFail"));
        if (fail) {
            o.setPaymentStatus(OrderDTO.PAY_FAILED);
            activity("Payment FAILED for " + o.getOrderId());
            return o;
        }
        BigDecimal paid = o.getAmountPaid() == null ? BigDecimal.ZERO : o.getAmountPaid();
        paid = paid.add(amt == null ? BigDecimal.ZERO : amt);
        o.setAmountPaid(paid);
        if (paid.compareTo(o.getAmount()) >= 0) o.setPaymentStatus(OrderDTO.PAY_PAID);
        else o.setPaymentStatus(OrderDTO.PAY_PARTIAL);
        activity("Payment " + amt + " recorded for " + o.getOrderId() + " (" + o.getPaymentStatus() + ")");
        return o;
    }

    private OrderDTO cancel(Map<String, Object> p) {
        OrderDTO o = findOrder(str(p, "orderId"));
        o.setStatus(OrderDTO.CANCELLED);
        o.setCancellationReason(str(p, "reason"));
        if (OrderDTO.PAY_PAID.equals(o.getPaymentStatus()) || OrderDTO.PAY_PARTIAL.equals(o.getPaymentStatus())) {
            o.setPaymentStatus(OrderDTO.PAY_REFUNDED);
        }
        activity("Order " + o.getOrderId() + " cancelled: " + o.getCancellationReason());
        return o;
    }

    // ==================== HR ====================

    private List<EmployeeDTO> filterEmployees(Map<String, Object> p) {
        String dept = str(p, "department");
        String status = str(p, "status");
        String q = str(p, "q");
        List<EmployeeDTO> out = new ArrayList<>();
        for (EmployeeDTO e : employees) {
            if (e.getRecruitmentStage() != null && !"SELECTED".equals(e.getRecruitmentStage())
                    && !"HIRED".equals(e.getRecruitmentStage())) continue;
            if (dept != null && !dept.isEmpty() && !dept.equalsIgnoreCase(e.getDepartment())) continue;
            if (status != null && !status.isEmpty() && !status.equalsIgnoreCase(e.getStatus())) continue;
            if (q != null && !q.isEmpty()) {
                String hay = (e.getEmployeeId() + " " + e.getName() + " " + e.getRole()).toLowerCase();
                if (!hay.contains(q.toLowerCase())) continue;
            }
            out.add(e);
        }
        return out;
    }

    private EmployeeDTO updateEmployee(EmployeeDTO dto) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeId().equals(dto.getEmployeeId())) {
                dto.recalcNetPay();
                employees.set(i, dto);
                activity("Employee updated: " + dto.getEmployeeId());
                return dto;
            }
        }
        throw IntegrationException.sendFailed("hr", "Employee not found: " + dto.getEmployeeId());
    }

    private List<EmployeeDTO> recruitmentPipeline() {
        List<EmployeeDTO> out = new ArrayList<>();
        for (EmployeeDTO e : employees) if (e.getRecruitmentStage() != null) out.add(e);
        return out;
    }

    private EmployeeDTO moveRecruitmentStage(Map<String, Object> p) {
        String id = str(p, "employeeId");
        String stage = str(p, "stage");
        Integer score = (Integer) p.get("score");
        for (EmployeeDTO e : employees) {
            if (e.getEmployeeId().equals(id)) {
                e.setRecruitmentStage(stage);
                if (score != null) e.setInterviewScore(score);
                activity("Candidate " + id + " moved to " + stage);
                return e;
            }
        }
        throw IntegrationException.sendFailed("hr", "Candidate not found: " + id);
    }

    private List<EmployeeDTO> onboardingList() {
        List<EmployeeDTO> out = new ArrayList<>();
        for (EmployeeDTO e : employees)
            if (EmployeeDTO.STATUS_NEW.equals(e.getStatus()) || "SELECTED".equals(e.getRecruitmentStage()))
                out.add(e);
        return out;
    }

    private List<EmployeeDTO> payrollList() {
        List<EmployeeDTO> out = new ArrayList<>();
        for (EmployeeDTO e : employees) if (e.getGrossSalary() != null) out.add(e);
        return out;
    }

    private String transferSalary(String employeeId) {
        activity("Salary transferred for " + employeeId);
        return "OK";
    }

    private String logAttendance(Map<String, Object> p) {
        String id = "ATT-" + attendanceSeq.incrementAndGet();
        attendanceLog.add(0, new String[]{id,
                str(p, "employeeId"), str(p, "checkIn"), str(p, "checkOut"), str(p, "overtime")});
        activity("Attendance logged for " + str(p, "employeeId"));
        return id;
    }

    private String leaveAction(Map<String, Object> p) {
        String id = str(p, "id");
        String action = str(p, "action");
        for (String[] r : leaveRequests) {
            if (r[0].equals(id)) {
                r[5] = action;
                activity("Leave " + id + " " + action);
                return "OK";
            }
        }
        throw IntegrationException.sendFailed("hr", "Leave request not found: " + id);
    }

    private List<EmployeeDTO> performanceList() {
        List<EmployeeDTO> out = new ArrayList<>();
        for (EmployeeDTO e : employees)
            if (e.getStatus() != null && EmployeeDTO.STATUS_ACTIVE.equals(e.getStatus())) out.add(e);
        return out;
    }

    private Map<String, Integer> hrStats() {
        int total = 0, active = 0, newJoiners = 0, onLeave = 0, pendingLeave = 0;
        for (EmployeeDTO e : employees) {
            if (e.getStatus() == null) continue;
            total++;
            if (EmployeeDTO.STATUS_ACTIVE.equals(e.getStatus())) active++;
            if (EmployeeDTO.STATUS_NEW.equals(e.getStatus())) newJoiners++;
            if (EmployeeDTO.STATUS_ON_LEAVE.equals(e.getStatus())) onLeave++;
        }
        for (String[] r : leaveRequests) if ("PENDING".equals(r[5])) pendingLeave++;
        Map<String, Integer> s = new HashMap<>();
        s.put("total", total); s.put("active", active); s.put("newJoiners", newJoiners);
        s.put("onLeave", onLeave); s.put("pendingLeave", pendingLeave);
        return s;
    }

    // ==================== Manufacturing ====================

    private CarModelDTO updateCarStatus(Map<String, Object> p) {
        String vin = str(p, "vin");
        String newStatus = str(p, "status");
        for (CarModelDTO c : cars) {
            if (c.getVin().equals(vin)) {
                c.setBuildStatus(newStatus);
                activity("Car " + vin + " → " + newStatus);
                return c;
            }
        }
        throw IntegrationException.sendFailed("mfg", "Car not found: " + vin);
    }

    private BomDTO findBomDetails(String bomId) {
        for (BomDTO b : boms) if (b.getBomId().equals(bomId)) return b;
        throw IntegrationException.fetchFailed("mfg", "BOM not found: " + bomId);
    }

    private BomDTO createBom(BomDTO dto) {
        if (dto.getBomId() == null || dto.getBomId().trim().isEmpty()) {
            throw IntegrationException.sendFailed("mfg/bom/create", "Missing bom_id");
        }
        if (dto.getBomVersion() == null || dto.getBomVersion().trim().isEmpty()) {
            throw IntegrationException.sendFailed("mfg/bom/create", "Missing bom_version");
        }
        for (BomDTO b : boms) {
            if (b.getBomId().equalsIgnoreCase(dto.getBomId())) {
                throw BusinessRuleException.duplicateBomVersion(dto.getProductId(), dto.getBomVersion());
            }
            if (b.getProductId().equalsIgnoreCase(dto.getProductId())
                    && b.getBomVersion().equalsIgnoreCase(dto.getBomVersion())) {
                throw BusinessRuleException.duplicateBomVersion(dto.getProductId(), dto.getBomVersion());
            }
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw BusinessRuleException.invalidBomStructure(dto.getBomId(), "at least one item is required");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BomItemDTO item : dto.getItems()) {
            MaterialDTO m = findMaterial(item.getMaterialItemId());
            if (m == null) {
                throw IntegrationException.sendFailed("mfg/bom/create", "Unknown material: " + item.getMaterialItemId());
            }
            item.setPartName(m.getPartName());
            item.setUnitCost(m.getUnitCost());
            item.setLineCost(m.getUnitCost().multiply(new BigDecimal(item.getQuantity())));
            total = total.add(item.getLineCost());
        }
        dto.setTotalCost(total);
        if (dto.getBudgetLimit() == null) dto.setBudgetLimit(total);

        boms.add(0, dto);
        activity("BOM created: " + dto.getBomId() + " (" + dto.getProductId() + ")");
        return dto;
    }

    private MaterialDTO createMaterial(MaterialDTO material) {
        if (material.getMaterialItemId() == null || material.getMaterialItemId().trim().isEmpty()) {
            throw IntegrationException.sendFailed("mfg/materials/create", "Missing material_item_id");
        }
        if (material.getPartName() == null || material.getPartName().trim().isEmpty()) {
            throw IntegrationException.sendFailed("mfg/materials/create", "Missing part_name");
        }
        if (material.getUnitCost() == null || material.getUnitCost().doubleValue() <= 0) {
            throw IntegrationException.sendFailed("mfg/materials/create", "unit_cost must be positive");
        }
        if (findMaterial(material.getMaterialItemId()) != null) {
            throw IntegrationException.sendFailed("mfg/materials/create", "Material already exists: " + material.getMaterialItemId());
        }

        materialCatalog.add(0, material);
        PartDTO part = findPart(material.getMaterialItemId());
        if (part == null) {
            parts.add(new PartDTO(
                    material.getMaterialItemId(),
                    material.getPartName(),
                    (int) Math.round(material.getAvailableQty()),
                    0,
                    0,
                    "MFG-WH",
                    material.getUnitCost()));
        } else {
            part.setPartName(material.getPartName());
            part.setUnitCost(material.getUnitCost());
            part.setStockLevel((int) Math.round(material.getAvailableQty()));
        }

        activity("Material created: " + material.getMaterialItemId());
        return material;
    }

    private MaterialDTO findMaterial(String materialItemId) {
        for (MaterialDTO m : materialCatalog) {
            if (m.getMaterialItemId().equalsIgnoreCase(materialItemId)) return m;
        }
        return null;
    }

    private List<RoutingStepDTO> routingFor(String productId) {
        List<RoutingStepDTO> out = new ArrayList<>();
        if (productId == null || productId.isEmpty()) return new ArrayList<>(routingSteps);
        if ("PRD-NEXON".equalsIgnoreCase(productId) || "PROD-001".equalsIgnoreCase(productId)) {
            for (RoutingStepDTO s : routingSteps) if ("RT-01".equals(s.getRoutingId())) out.add(s);
        } else if ("PRD-NEXON-EV".equalsIgnoreCase(productId) || "PROD-002".equalsIgnoreCase(productId)) {
            for (RoutingStepDTO s : routingSteps) if ("RT-02".equals(s.getRoutingId())) out.add(s);
        } else {
            return new ArrayList<>(routingSteps);
        }
        // GRASP: Information Expert — detect contiguous-sequence gaps here.
        out.sort((a, b) -> Integer.compare(a.getSequenceNumber(), b.getSequenceNumber()));
        for (int i = 1; i < out.size(); i++) {
            int prev = out.get(i - 1).getSequenceNumber();
            int cur  = out.get(i).getSequenceNumber();
            if (cur - prev > 1) {
                throw BusinessRuleException.routingStepGap(out.get(i).getRoutingId(), prev + 1);
            }
        }
        return out;
    }

    private ProductionOrderDTO createProductionOrder(ProductionOrderDTO dto) {
        // Component-stock check against inventory.
        BomDTO bom = null;
        for (BomDTO b : boms) if (b.getBomId().equals(dto.getBomId())) { bom = b; break; }
        if (bom != null) {
            for (BomItemDTO item : bom.getItems()) {
                PartDTO part = findPart(item.getMaterialItemId());
                int required = (int) Math.ceil(item.getQuantity() * dto.getQtyPlanned());
                if (part != null && part.getStockLevel() < required) {
                    throw BusinessRuleException.componentStockInsufficient(
                            part.getPartId(), required, part.getStockLevel());
                }
            }
        }
        dto.setOrderId("PO-" + prodOrderSeq.incrementAndGet());
        if (dto.getStatus() == null) dto.setStatus(ProductionOrderDTO.PENDING);
        productionOrders.add(0, dto);
        activity("Production order created: " + dto.getOrderId());
        return dto;
    }

    private ProductionOrderDTO cancelProductionOrder(String orderId) {
        for (ProductionOrderDTO po : productionOrders) {
            if (po.getOrderId().equals(orderId)) {
                if (ProductionOrderDTO.IN_PROGRESS.equals(po.getStatus())) {
                    throw BusinessRuleException.productionOrderCancellationBlocked(orderId);
                }
                po.setStatus(ProductionOrderDTO.CANCELLED);
                activity("Production order cancelled: " + orderId);
                return po;
            }
        }
        throw IntegrationException.sendFailed("mfg", "Production order not found: " + orderId);
    }

    private String recordExecution(Map<String, Object> p) {
        ExecutionLogDTO dto = new ExecutionLogDTO();
        dto.setOrderId(str(p, "orderId"));
        dto.setOperatorId(str(p, "operatorId"));
        dto.setMachineId(str(p, "machineId"));
        dto.setNote(str(p, "note"));
        Object produced = p.get("qtyProduced");
        Object scrap = p.get("scrapQty");
        dto.setQtyProduced(produced instanceof Number ? ((Number) produced).doubleValue() : 0);
        dto.setScrapQty(scrap instanceof Number ? ((Number) scrap).doubleValue() : 0);
        dto.setStartTime(LocalDateTime.now());
        return recordExecution(dto);
    }

    private String recordExecution(ExecutionLogDTO dto) {
        if (dto.getLogId() == null || dto.getLogId().trim().isEmpty()) {
            dto.setLogId("LOG-" + (executionLogs.size() + 1001));
        }
        executionLogs.add(0, dto);

        String orderId = dto.getOrderId();
        int produced = (int) Math.round(dto.getQtyProduced());
        int scrap = (int) Math.round(dto.getScrapQty());
        for (ProductionOrderDTO po : productionOrders) {
            if (po.getOrderId().equals(orderId)) {
                po.setQtyProduced(po.getQtyProduced() + produced);
                po.setScrapQty(po.getScrapQty() + scrap);
                po.setStatus(ProductionOrderDTO.IN_PROGRESS);
                activity("Execution logged for " + orderId + " (+" + produced + " produced)");
                return "ACK-" + dto.getLogId();
            }
        }
        throw IntegrationException.sendFailed("mfg", "Production order not found: " + orderId);
    }

    private List<ExecutionLogDTO> executionLogsFor(String orderId) {
        if (orderId == null || orderId.isEmpty()) return new ArrayList<>(executionLogs);
        List<ExecutionLogDTO> out = new ArrayList<>();
        for (ExecutionLogDTO log : executionLogs) {
            if (orderId.equals(log.getOrderId())) out.add(log);
        }
        return out;
    }

    private QCCheckDTO submitQC(QCCheckDTO dto) {
        dto.setQcCheckId("QC-" + qcSeq.incrementAndGet());
        qcChecks.add(0, dto);
        activity("QC check " + dto.getQcCheckId() + " submitted ("
                + dto.getDefectsCount() + "/" + dto.getSampleSize() + " defects)");
        double rate = dto.defectRate();
        if (rate > 0.05) {
            // Informational warning surfaced via ExceptionHandler without blocking save.
            throw BusinessRuleException.qcDefectThresholdExceeded(dto.getProductionOrderId(), rate);
        }
        return dto;
    }

    private Map<String, Integer> mfgStats() {
        int inProgress = 0, completed = 0, pending = 0, inQuality = 0;
        for (ProductionOrderDTO po : productionOrders) {
            if (ProductionOrderDTO.IN_PROGRESS.equals(po.getStatus())) inProgress++;
            else if (ProductionOrderDTO.COMPLETED.equals(po.getStatus())) completed++;
            else if (ProductionOrderDTO.PENDING.equals(po.getStatus())) pending++;
        }
        for (CarModelDTO c : cars) if (CarModelDTO.IN_QUALITY.equals(c.getBuildStatus())) inQuality++;
        Map<String, Integer> s = new HashMap<>();
        s.put("cars", cars.size());
        s.put("inProgress", inProgress);
        s.put("completed", completed);
        s.put("pendingOrders", pending);
        s.put("qcFails", inQuality);
        s.put("workCenters", workCenters.size());
        return s;
    }

    // ==================== Supply Chain ====================

    private PartDTO findPart(String partId) {
        for (PartDTO p : parts) if (p.getPartId().equals(partId)) return p;
        return null;
    }

    private List<PartDTO> lowStockParts() {
        List<PartDTO> out = new ArrayList<>();
        for (PartDTO p : parts) if (p.isBelowReorderPoint()) out.add(p);
        return out;
    }

    private PurchaseOrderDTO createPurchaseOrder(PurchaseOrderDTO dto) {
        // Supplier must exist and be approved.
        SupplierDTO sup = null;
        for (SupplierDTO s : suppliers) if (s.getSupplierId().equals(dto.getSupplierId())) { sup = s; break; }
        if (sup == null || !sup.isApproved()) {
            throw BusinessRuleException.supplierNotFound(dto.getSupplierId());
        }
        // Duplicate-PO detection: same supplier + same total amount currently open.
        for (PurchaseOrderDTO existing : purchaseOrders) {
            if (existing.getSupplierId().equals(dto.getSupplierId())
                    && existing.getTotalAmount() != null
                    && dto.getTotalAmount() != null
                    && existing.getTotalAmount().compareTo(dto.getTotalAmount()) == 0
                    && !PurchaseOrderDTO.CANCELLED.equals(existing.getStatus())
                    && !PurchaseOrderDTO.RECEIVED.equals(existing.getStatus())) {
                throw BusinessRuleException.duplicatePurchaseOrder(dto.getSupplierId());
            }
        }
        dto.setPoId("PO-" + poSeq.incrementAndGet());
        if (dto.getCreatedDate() == null) dto.setCreatedDate(LocalDate.now());
        dto.setStatus(PurchaseOrderDTO.PENDING_APPROVAL);
        dto.setSupplierName(sup.getSupplierName());
        purchaseOrders.add(0, dto);
        activity("PO created: " + dto.getPoId() + " for " + sup.getSupplierName());
        return dto;
    }

    private PurchaseOrderDTO approvePurchaseOrder(Map<String, Object> p) {
        String poId = str(p, "poId");
        String approver = str(p, "approverUserId");
        for (PurchaseOrderDTO po : purchaseOrders) {
            if (po.getPoId().equals(poId)) {
                // Four-eyes rule.
                if (approver != null && approver.equalsIgnoreCase(po.getCreatedBy())) {
                    throw BusinessRuleException.fourEyesRuleViolation(poId);
                }
                po.setStatus(PurchaseOrderDTO.APPROVED);
                po.setApprovedBy(approver);
                po.setApprovalDate(LocalDate.now());
                activity("PO approved: " + poId + " by " + approver);
                return po;
            }
        }
        throw IntegrationException.sendFailed("scm", "PO not found: " + poId);
    }

    private String reorderPart(Map<String, Object> p) {
        String partId = str(p, "partId");
        Integer qty = (Integer) p.get("quantity");
        PartDTO part = findPart(partId);
        if (part == null) throw IntegrationException.sendFailed("scm", "Part not found: " + partId);
        if (qty == null || qty <= 0) {
            throw new BusinessRuleException(BusinessRuleException.STOCK_BELOW_THRESHOLD,
                    "Reorder quantity must be positive",
                    com.erp.exception.ERPException.Severity.WARNING);
        }
        part.setStockLevel(part.getStockLevel() + qty);
        activity("Reordered " + qty + " x " + part.getPartName() + " (" + partId + ")");
        return "OK";
    }

    private GoodsReceiptDTO createGoodsReceipt(GoodsReceiptDTO dto) {
        if (dto.getReceivedQty() != dto.getExpectedQty()) {
            throw BusinessRuleException.goodsReceiptMismatch(
                    dto.getPoId(), dto.getExpectedQty(), dto.getReceivedQty());
        }
        dto.setGrnId("GRN-" + grnSeq.incrementAndGet());
        if (dto.getReceivedDate() == null) dto.setReceivedDate(LocalDate.now());
        if (dto.getInspectionStatus() == null) dto.setInspectionStatus(GoodsReceiptDTO.PASSED);
        goodsReceipts.add(0, dto);
        // Move matching PO to RECEIVED.
        for (PurchaseOrderDTO po : purchaseOrders) {
            if (po.getPoId().equals(dto.getPoId())) po.setStatus(PurchaseOrderDTO.RECEIVED);
        }
        activity("GRN " + dto.getGrnId() + " created for " + dto.getPoId());
        return dto;
    }

    private ShipmentDTO updateShipment(Map<String, Object> p) {
        String id = str(p, "shipmentId");
        String status = str(p, "status");
        for (ShipmentDTO s : shipments) {
            if (s.getShipmentId().equals(id)) {
                s.setStatus(status);
                if (ShipmentDTO.DELIVERED.equals(status)) s.setActualArrival(LocalDate.now());
                activity("Shipment " + id + " → " + status);
                return s;
            }
        }
        throw IntegrationException.sendFailed("scm", "Shipment not found: " + id);
    }

    private InvoiceDTO createInvoice(InvoiceDTO dto) {
        dto.setInvoiceId("INV-" + invoiceSeq.incrementAndGet());
        if (dto.getInvoiceDate() == null) dto.setInvoiceDate(LocalDate.now());
        if (dto.getDueDate() == null) dto.setDueDate(LocalDate.now().plusDays(30));
        dto.setPaymentStatus(InvoiceDTO.PENDING);
        invoices.add(0, dto);
        activity("Invoice " + dto.getInvoiceId() + " created for " + dto.getPoId());
        return dto;
    }

    private InvoiceDTO verifyInvoice(String invoiceId) {
        InvoiceDTO inv = findInvoice(invoiceId);
        // Match against PO total.
        for (PurchaseOrderDTO po : purchaseOrders) {
            if (po.getPoId().equals(inv.getPoId())
                    && po.getTotalAmount() != null
                    && inv.getInvoiceAmount() != null
                    && po.getTotalAmount().compareTo(inv.getInvoiceAmount()) != 0) {
                throw BusinessRuleException.invoiceMismatch(
                        invoiceId, po.getTotalAmount(), inv.getInvoiceAmount());
            }
        }
        inv.setPaymentStatus(InvoiceDTO.AUTHORIZED);
        activity("Invoice " + invoiceId + " verified");
        return inv;
    }

    private InvoiceDTO payInvoice(String invoiceId) {
        InvoiceDTO inv = findInvoice(invoiceId);
        if (!InvoiceDTO.AUTHORIZED.equals(inv.getPaymentStatus())) {
            throw BusinessRuleException.paymentProcessingFailed(
                    invoiceId, "invoice must be AUTHORIZED before payment");
        }
        inv.setPaymentStatus(InvoiceDTO.PAID);
        activity("Invoice " + invoiceId + " paid");
        return inv;
    }

    private InvoiceDTO findInvoice(String invoiceId) {
        for (InvoiceDTO i : invoices) if (i.getInvoiceId().equals(invoiceId)) return i;
        throw IntegrationException.sendFailed("scm", "Invoice not found: " + invoiceId);
    }

    private Map<String, Integer> scmStats() {
        int openPO = 0, lowStock = 0, onTime = 0, latePO = 0;
        for (PurchaseOrderDTO po : purchaseOrders) {
            if (!PurchaseOrderDTO.RECEIVED.equals(po.getStatus())
                    && !PurchaseOrderDTO.CANCELLED.equals(po.getStatus())) openPO++;
        }
        for (PartDTO p : parts) if (p.isBelowReorderPoint()) lowStock++;
        for (ShipmentDTO s : shipments) {
            if (ShipmentDTO.DELIVERED.equals(s.getStatus())
                    && s.getActualArrival() != null && s.getEstimatedArrival() != null
                    && !s.getActualArrival().isAfter(s.getEstimatedArrival())) onTime++;
            else if (ShipmentDTO.DELAYED.equals(s.getStatus())) latePO++;
        }
        Map<String, Integer> s = new HashMap<>();
        s.put("skus", parts.size());
        s.put("lowStock", lowStock);
        s.put("openPO", openPO);
        s.put("onTime", onTime);
        s.put("delayed", latePO);
        return s;
    }

    // ==================== Activity log ====================

    public List<String[]> getActivityLog() { return new ArrayList<>(activityLog); }

    private void activity(String msg) {
        activityLog.add(0, new String[]{LocalDateTime.now().toString(), msg});
        if (activityLog.size() > 50) activityLog.remove(activityLog.size() - 1);
    }

    // ==================== Seed data ====================

    private void seedOrders() {
        String[][] seed = {
                {"ORD-1001", "Rakesh Industries",   "TML-1001", "Nexon",   "Steel A1",  "1250000", "PENDING",    "PENDING"},
                {"ORD-1002", "Tata Motors Dealer",  "TML-1002", "Harrier", "Alloy B2",  "1820000", "APPROVED",   "PARTIAL"},
                {"ORD-1003", "Mahindra Showroom",   "TML-1003", "Safari",  "Reinforced","2450000", "IN_TRANSIT", "PAID"},
                {"ORD-1004", "Pearson Motors",      "TML-1004", "Tiago",   "Steel A1",   "675000", "DELIVERED",  "PAID"},
                {"ORD-1005", "Gupta Automobiles",   "TML-1005", "Altroz",  "Aluminum",   "895000", "PENDING",    "PENDING"},
                {"ORD-1006", "Sharma & Sons",       "TML-1006", "Harrier", "Alloy B2",  "1790000", "APPROVED",   "PENDING"},
                {"ORD-1007", "Orion Logistics",     "TML-1007", "Safari",  "Reinforced","2550000", "IN_TRANSIT", "PARTIAL"},
                {"ORD-1008", "Blue Horizon Cars",   "TML-1008", "Punch",   "Steel A1",   "725000", "APPROVED",   "PAID"},
                {"ORD-1009", "Nova Fleet Services", "TML-1009", "Nexon EV","Aluminum",  "1780000", "REVISION",   "PENDING"},
                {"ORD-1010", "Metro Auto Hub",      "TML-1010", "Harrier", "Alloy B2",  "1860000", "DELIVERED",  "PAID"},
                {"ORD-1011", "Sunrise Motors",      "TML-1011", "Nexon",   "Steel A1",  "1275000", "PENDING",    "PENDING"},
                {"ORD-1012", "Capital Cars Ltd",    "TML-1012", "Safari",  "Reinforced","2510000", "CANCELLED",  "REFUNDED"},
                {"ORD-1013", "Eastern Autos",       "TML-1013", "Harrier", "Alloy B2",  "1840000", "APPROVED",   "PARTIAL"},
                {"ORD-1014", "GreenDrive Co",       "TML-1014", "Nexon EV","Aluminum",  "1840000", "PENDING",    "PENDING"},
                {"ORD-1015", "Highway Dealers",     "TML-1015", "Tiago",   "Steel A1",   "685000", "IN_TRANSIT", "PAID"},
        };
        for (String[] s : seed) {
            OrderDTO o = new OrderDTO(s[0], s[1], s[2], s[3], s[4],
                    new BigDecimal(s[5]), LocalDate.now().minusDays(seed.length - orders.size()),
                    s[6], s[7]);
            if ("PAID".equals(s[7])) o.setAmountPaid(o.getAmount());
            else if ("PARTIAL".equals(s[7])) o.setAmountPaid(o.getAmount().divide(new BigDecimal("2")));
            else o.setAmountPaid(BigDecimal.ZERO);
            if ("IN_TRANSIT".equals(s[6])) {
                o.setCourier("BlueDart Auto"); o.setTrackingNumber("BD" + (100000 + orders.size()));
            }
            orders.add(o);
        }
        orderSeq.set(1015);
    }

    private void seedEmployees() {
        String[] lines = {"Pune Line-A", "Pune Line-B", "Sanand Line-C", "Jamshedpur Line-D", "N/A"};
        String[] shifts = {"Morning (06-14)", "Afternoon (14-22)", "Night (22-06)", "General (09-18)"};
        String[][] seed = {
                {"EMP-001", "Arjun Verma",     "Line Supervisor",     "Manufacturing", "ACTIVE"},
                {"EMP-002", "Priya Singh",     "Quality Inspector",   "Quality",       "ACTIVE"},
                {"EMP-003", "Rohan Das",       "Assembly Technician", "Assembly",      "ACTIVE"},
                {"EMP-004", "Meera Nair",      "R&D Engineer",        "R&D",           "ACTIVE"},
                {"EMP-005", "Vikram Shah",     "HR Executive",        "HR",            "ACTIVE"},
                {"EMP-006", "Anita Rao",       "Payroll Manager",     "HR",            "ACTIVE"},
                {"EMP-007", "Karan Mehta",     "Sales Associate",     "Sales",         "ACTIVE"},
                {"EMP-008", "Sneha Kapoor",    "Accountant",          "Finance",       "ACTIVE"},
                {"EMP-009", "Deepak Iyer",     "Welder",              "Manufacturing", "ACTIVE"},
                {"EMP-010", "Ritu Sharma",     "QA Lead",             "Quality",       "ACTIVE"},
                {"EMP-011", "Ahmed Khan",      "Paint Specialist",    "Assembly",      "ON_LEAVE"},
                {"EMP-012", "Latha Menon",     "Project Lead",        "R&D",           "ACTIVE"},
                {"EMP-013", "Suresh Patil",    "Logistics Coord.",    "Manufacturing", "ACTIVE"},
                {"EMP-014", "Kavita Joshi",    "Recruitment Lead",    "HR",            "ACTIVE"},
                {"EMP-015", "Imran Ali",       "Sales Manager",       "Sales",         "ACTIVE"},
                {"EMP-016", "Pooja Bansal",    "Finance Analyst",     "Finance",       "ACTIVE"},
                {"EMP-017", "Gagan Chauhan",   "Assembly Technician", "Assembly",      "NEW"},
                {"EMP-018", "Rhea D'Souza",    "Quality Analyst",     "Quality",       "NEW"},
                {"EMP-019", "Nikhil Bhat",     "Welder",              "Manufacturing", "NEW"},
                {"EMP-020", "Tanvi Shetty",    "R&D Intern",          "R&D",           "NEW"},
        };
        int i = 0;
        for (String[] s : seed) {
            EmployeeDTO e = new EmployeeDTO(s[0], s[1], s[2], s[3],
                    lines[i % lines.length], shifts[i % shifts.length],
                    LocalDate.now().minusDays(30 + i * 15), s[4]);
            BigDecimal gross = new BigDecimal(45000 + (i * 1700));
            e.setGrossSalary(gross);
            e.setDeductions(gross.multiply(new BigDecimal("0.08")));
            e.setTaxRecord(gross.multiply(new BigDecimal("0.12")));
            e.recalcNetPay();
            e.setPerformanceRating(3 + (i % 3));
            e.setPerformanceFeedback("Consistent contribution to " + s[3].toLowerCase() + " team.");
            e.setPromotionStatus(i % 5 == 0 ? "Under Review" : "Stable");
            if (EmployeeDTO.STATUS_NEW.equals(s[4])) {
                e.setBackgroundCheckPassed(i % 2 == 0);
                e.setDocumentsVerified(i % 3 != 0);
                e.setOnboardingVerified(false);
            } else {
                e.setBackgroundCheckPassed(true);
                e.setDocumentsVerified(true);
                e.setOnboardingVerified(true);
            }
            employees.add(e);
            i++;
        }
        String[][] candidates = {
                {"CAN-101", "Ankit Jain",      "Assembly Technician", "Assembly",      "APPLIED"},
                {"CAN-102", "Divya Hegde",     "Quality Inspector",   "Quality",       "SHORTLISTED"},
                {"CAN-103", "Farhan Ahmed",    "R&D Engineer",        "R&D",           "INTERVIEW"},
                {"CAN-104", "Isha Kulkarni",   "HR Executive",        "HR",            "INTERVIEW"},
                {"CAN-105", "Manoj Pillai",    "Welder",              "Manufacturing", "APPLIED"},
                {"CAN-106", "Nina George",     "Sales Associate",     "Sales",         "SELECTED"},
        };
        int j = 0;
        for (String[] c : candidates) {
            EmployeeDTO e = new EmployeeDTO(c[0], c[1], c[2], c[3],
                    "N/A", "General (09-18)", LocalDate.now().plusDays(5), "NEW");
            e.setRecruitmentStage(c[4]);
            e.setInterviewScore("INTERVIEW".equals(c[4]) || "SELECTED".equals(c[4]) ? 70 + (j * 3) : null);
            employees.add(e);
            j++;
        }
    }

    private void seedLeave() {
        String[][] seed = {
                {"LV-500", "EMP-003", "Casual",  "2026-04-20", "2026-04-22", "PENDING"},
                {"LV-501", "EMP-007", "Sick",    "2026-04-15", "2026-04-16", "APPROVED"},
                {"LV-502", "EMP-011", "Earned",  "2026-04-10", "2026-04-25", "APPROVED"},
                {"LV-503", "EMP-002", "Casual",  "2026-04-18", "2026-04-18", "PENDING"},
                {"LV-504", "EMP-015", "Sick",    "2026-04-12", "2026-04-13", "REJECTED"},
                {"LV-505", "EMP-009", "Earned",  "2026-05-01", "2026-05-07", "PENDING"},
        };
        leaveRequests.addAll(Arrays.asList(seed));
        leaveSeq.set(505);
    }

    private void seedAttendance() {
        String[][] seed = {
                {"ATT-901", "EMP-001", "06:05", "14:10", "10m"},
                {"ATT-902", "EMP-003", "06:00", "15:30", "1h30m"},
                {"ATT-903", "EMP-009", "14:02", "22:15", "15m"},
                {"ATT-904", "EMP-013", "22:00", "06:05", "05m"},
                {"ATT-905", "EMP-002", "09:00", "18:30", "30m"},
        };
        attendanceLog.addAll(Arrays.asList(seed));
        attendanceSeq.set(905);
    }

    private void seedManufacturing() {
        // Cars on the line
        String[][] carSeed = {
                {"TML-1001", "Nexon",    "Steel A1",   CarModelDTO.IN_ASSEMBLY, "Pune Line-A"},
                {"TML-1002", "Harrier",  "Alloy B2",   CarModelDTO.IN_ASSEMBLY, "Pune Line-A"},
                {"TML-1003", "Safari",   "Reinforced", CarModelDTO.IN_QUALITY,  "Pune Line-B"},
                {"TML-1004", "Tiago",    "Steel A1",   CarModelDTO.READY,       "Sanand Line-C"},
                {"TML-1005", "Altroz",   "Aluminum",   CarModelDTO.IN_ASSEMBLY, "Sanand Line-C"},
                {"TML-1006", "Harrier",  "Alloy B2",   CarModelDTO.IN_QUALITY,  "Pune Line-B"},
                {"TML-1007", "Safari",   "Reinforced", CarModelDTO.PENDING,     "Jamshedpur Line-D"},
                {"TML-1008", "Punch",    "Steel A1",   CarModelDTO.READY,       "Sanand Line-C"},
                {"TML-1009", "Nexon EV", "Aluminum",   CarModelDTO.IN_ASSEMBLY, "Pune Line-A"},
                {"TML-1010", "Harrier",  "Alloy B2",   CarModelDTO.SHIPPED,     "Pune Line-B"},
        };
        for (String[] c : carSeed) {
            cars.add(new CarModelDTO(c[0], c[1], c[2], c[3], c[4],
                    LocalDateTime.now().minusHours(cars.size() * 6L + 2)));
        }

        // BOMs
        BomDTO nexonBom = new BomDTO("BOM-01", "PRD-NEXON", "Nexon", "v1.0", true,
                new BigDecimal("420000"), new ArrayList<>(Arrays.asList(
                new BomItemDTO("PART-ENG-1", "1.2L Revotron Engine", 1, new BigDecimal("180000"), new BigDecimal("180000")),
                new BomItemDTO("PART-CHA-1", "Steel A1 Chassis",     1, new BigDecimal("90000"),  new BigDecimal("90000")),
                new BomItemDTO("PART-WHL-1", "Alloy Wheel Set",      4, new BigDecimal("12000"),  new BigDecimal("48000")),
                new BomItemDTO("PART-BAT-1", "48V Battery",          1, new BigDecimal("35000"),  new BigDecimal("35000")),
                new BomItemDTO("PART-PNT-1", "Exterior Paint Kit",   1, new BigDecimal("9000"),   new BigDecimal("9000")))));
        boms.add(nexonBom);

        BomDTO harrierBom = new BomDTO("BOM-02", "PRD-HARRIER", "Harrier", "v2.1", true,
                new BigDecimal("610000"), new ArrayList<>(Arrays.asList(
                new BomItemDTO("PART-ENG-2", "2.0L Kryotec Engine", 1, new BigDecimal("260000"), new BigDecimal("260000")),
                new BomItemDTO("PART-CHA-2", "Alloy B2 Chassis",    1, new BigDecimal("140000"), new BigDecimal("140000")),
                new BomItemDTO("PART-WHL-2", "Premium Alloy Wheels",4, new BigDecimal("18000"),  new BigDecimal("72000")),
                new BomItemDTO("PART-INF-1", "Infotainment Unit",   1, new BigDecimal("45000"),  new BigDecimal("45000")))));
        boms.add(harrierBom);

        BomDTO safariBom = new BomDTO("BOM-03", "PRD-SAFARI", "Safari", "v2.0", true,
                new BigDecimal("720000"), new ArrayList<>(Arrays.asList(
                new BomItemDTO("PART-ENG-2", "2.0L Kryotec Engine", 1, new BigDecimal("260000"), new BigDecimal("260000")),
                new BomItemDTO("PART-CHA-3", "Reinforced Chassis",  1, new BigDecimal("180000"), new BigDecimal("180000")),
                new BomItemDTO("PART-WHL-2", "Premium Alloy Wheels",4, new BigDecimal("18000"),  new BigDecimal("72000")))));
        boms.add(safariBom);

        BomDTO altrozBom = new BomDTO("BOM-04", "PRD-ALTROZ", "Altroz", "v1.0", true,
                new BigDecimal("310000"), new ArrayList<>(Arrays.asList(
                new BomItemDTO("PART-ENG-1", "1.2L Revotron Engine", 1, new BigDecimal("180000"), new BigDecimal("180000")),
                new BomItemDTO("PART-CHA-4", "Aluminum Chassis",    1, new BigDecimal("110000"), new BigDecimal("110000")))));
        boms.add(altrozBom);

        BomDTO evBom = new BomDTO("BOM-05", "PRD-NEXON-EV", "Nexon EV", "v1.0", true,
                new BigDecimal("880000"), new ArrayList<>(Arrays.asList(
                new BomItemDTO("PART-BAT-EV", "40 kWh Li-Ion Pack", 1, new BigDecimal("520000"), new BigDecimal("520000")),
                new BomItemDTO("PART-MOT-EV", "Permanent Magnet Motor", 1, new BigDecimal("180000"), new BigDecimal("180000")),
                new BomItemDTO("PART-CHA-4", "Aluminum Chassis",    1, new BigDecimal("110000"), new BigDecimal("110000")))));
        boms.add(evBom);

        // Production orders
        String[][] poSeed = {
                {"Nexon",    "PRD-NEXON",    "BOM-01", ProductionOrderDTO.IN_PROGRESS, ProductionOrderDTO.PRI_HIGH,   "50"},
                {"Harrier",  "PRD-HARRIER",  "BOM-02", ProductionOrderDTO.PENDING,     ProductionOrderDTO.PRI_MEDIUM, "30"},
                {"Safari",   "PRD-SAFARI",   "BOM-03", ProductionOrderDTO.IN_PROGRESS, ProductionOrderDTO.PRI_URGENT, "20"},
                {"Tiago",    "PRD-TIAGO",    "BOM-01", ProductionOrderDTO.COMPLETED,   ProductionOrderDTO.PRI_LOW,    "80"},
                {"Altroz",   "PRD-ALTROZ",   "BOM-04", ProductionOrderDTO.PENDING,     ProductionOrderDTO.PRI_MEDIUM, "60"},
                {"Harrier",  "PRD-HARRIER",  "BOM-02", ProductionOrderDTO.COMPLETED,   ProductionOrderDTO.PRI_HIGH,   "25"},
                {"Nexon EV", "PRD-NEXON-EV", "BOM-05", ProductionOrderDTO.IN_PROGRESS, ProductionOrderDTO.PRI_HIGH,   "15"},
                {"Punch",    "PRD-PUNCH",    "BOM-01", ProductionOrderDTO.PENDING,     ProductionOrderDTO.PRI_LOW,    "40"},
        };
        for (int i = 0; i < poSeed.length; i++) {
            String[] s = poSeed[i];
            ProductionOrderDTO po = new ProductionOrderDTO(
                    "PO-" + (700 + i + 1), s[1], s[0], s[2],
                    LocalDate.now().minusDays(10 - i), LocalDate.now().plusDays(i + 5),
                    s[3], s[4], Integer.parseInt(s[5]));
            if (ProductionOrderDTO.IN_PROGRESS.equals(s[3]) || ProductionOrderDTO.COMPLETED.equals(s[3])) {
                po.setActualStartDate(LocalDate.now().minusDays(5));
                po.setQtyProduced(Integer.parseInt(s[5]) / 2);
            }
            if (ProductionOrderDTO.COMPLETED.equals(s[3])) {
                po.setActualEndDate(LocalDate.now().minusDays(1));
                po.setQtyProduced(Integer.parseInt(s[5]));
            }
            productionOrders.add(po);
        }
        prodOrderSeq.set(708);

        // Work centers
        workCenters.add(new WorkCenterDTO("WC-01", "Body Weld Station",  WorkCenterDTO.TYPE_WELDING,   160, 82, "Pune Line-A"));
        workCenters.add(new WorkCenterDTO("WC-02", "Paint Booth 1",      WorkCenterDTO.TYPE_PAINT,     160, 75, "Pune Line-A"));
        workCenters.add(new WorkCenterDTO("WC-03", "Final Assembly A",   WorkCenterDTO.TYPE_ASSEMBLY,  160, 95, "Pune Line-B"));
        workCenters.add(new WorkCenterDTO("WC-04", "Quality Gate 1",     WorkCenterDTO.TYPE_TESTING,   120, 68, "Pune Line-B"));
        workCenters.add(new WorkCenterDTO("WC-05", "EV Battery Install", WorkCenterDTO.TYPE_ASSEMBLY,  160, 88, "Sanand Line-C"));
        workCenters.add(new WorkCenterDTO("WC-06", "Packaging & PDI",    WorkCenterDTO.TYPE_PACKAGING, 160, 60, "Jamshedpur Line-D"));

        // Routing steps — RT-01 contiguous (1,2,3,4); RT-02 seeded with a gap (1,2,4) for the Routing tab demo.
        routingSteps.add(new RoutingStepDTO("RT-01", "OP-101", 1, "Weld Chassis",    "WC-01", 0.5, 2.0));
        routingSteps.add(new RoutingStepDTO("RT-01", "OP-102", 2, "Paint Body",      "WC-02", 0.3, 1.5));
        routingSteps.add(new RoutingStepDTO("RT-01", "OP-103", 3, "Final Assembly",  "WC-03", 0.6, 3.2));
        routingSteps.add(new RoutingStepDTO("RT-01", "OP-104", 4, "Quality Gate",    "WC-04", 0.2, 0.8));
        routingSteps.add(new RoutingStepDTO("RT-02", "OP-201", 1, "Install Battery", "WC-05", 0.7, 2.4));
        routingSteps.add(new RoutingStepDTO("RT-02", "OP-202", 2, "Wire Harness",    "WC-03", 0.4, 1.6));
        // sequence 3 intentionally missing to trigger ROUTING_STEP_GAP
        routingSteps.add(new RoutingStepDTO("RT-02", "OP-204", 4, "Packaging & PDI", "WC-06", 0.3, 0.6));
    }

    private void seedSupplyChain() {
        // Suppliers — mix of approved / unapproved.
        suppliers.add(new SupplierDTO("SUP-001", "Bharat Forge Ltd",      true,  4.6, true,  "contact@bharatforge.in", "NET-30"));
        suppliers.add(new SupplierDTO("SUP-002", "Motherson Sumi",        true,  4.4, true,  "sales@motherson.in",     "NET-45"));
        suppliers.add(new SupplierDTO("SUP-003", "Sundaram Clayton",      true,  4.2, true,  "po@sundaramclayton.in",  "NET-30"));
        suppliers.add(new SupplierDTO("SUP-004", "Minda Industries",      true,  4.0, true,  "info@minda.com",         "NET-60"));
        suppliers.add(new SupplierDTO("SUP-005", "Exide Industries",      true,  3.9, true,  "orders@exide.in",        "NET-30"));
        suppliers.add(new SupplierDTO("SUP-006", "Wheels India",          true,  4.1, true,  "sales@wheelsindia.in",   "NET-45"));
        suppliers.add(new SupplierDTO("SUP-007", "Asian Paints Auto",     true,  4.3, true,  "auto@asianpaints.in",    "NET-30"));
        suppliers.add(new SupplierDTO("SUP-008", "Reliance EV Cells",     false, 3.2, false, "ev@reliance.in",         "NET-90"));

        // Parts / inventory — seed a few below reorder point to trigger low-stock alerts.
        parts.add(new PartDTO("PART-ENG-1",  "1.2L Revotron Engine",    120, 40,  60,  "Pune-WH1",      new BigDecimal("180000")));
        parts.add(new PartDTO("PART-ENG-2",  "2.0L Kryotec Engine",      75, 30,  45,  "Pune-WH1",      new BigDecimal("260000")));
        parts.add(new PartDTO("PART-CHA-1",  "Steel A1 Chassis",        200, 60,  90,  "Sanand-WH2",    new BigDecimal("90000")));
        parts.add(new PartDTO("PART-CHA-2",  "Alloy B2 Chassis",         35, 40,  55,  "Pune-WH1",      new BigDecimal("140000"))); // LOW
        parts.add(new PartDTO("PART-CHA-3",  "Reinforced Chassis",       28, 25,  40,  "Jamshedpur-WH3",new BigDecimal("180000"))); // LOW
        parts.add(new PartDTO("PART-CHA-4",  "Aluminum Chassis",        160, 40,  60,  "Sanand-WH2",    new BigDecimal("110000")));
        parts.add(new PartDTO("PART-WHL-1",  "Alloy Wheel Set",         480, 160, 240, "Pune-WH1",      new BigDecimal("12000")));
        parts.add(new PartDTO("PART-WHL-2",  "Premium Alloy Wheels",    300, 120, 180, "Pune-WH1",      new BigDecimal("18000")));
        parts.add(new PartDTO("PART-BAT-1",  "48V Battery",             140, 50,  80,  "Sanand-WH2",    new BigDecimal("35000")));
        parts.add(new PartDTO("PART-BAT-EV", "40 kWh Li-Ion Pack",       12, 20,  30,  "Sanand-WH2",    new BigDecimal("520000"))); // LOW
        parts.add(new PartDTO("PART-MOT-EV", "Permanent Magnet Motor",   42, 30,  45,  "Sanand-WH2",    new BigDecimal("180000"))); // LOW
        parts.add(new PartDTO("PART-INF-1",  "Infotainment Unit",       220, 80,  120, "Pune-WH1",      new BigDecimal("45000")));
        parts.add(new PartDTO("PART-PNT-1",  "Exterior Paint Kit",      360, 100, 160, "Pune-WH1",      new BigDecimal("9000")));
        parts.add(new PartDTO("PART-HRN-1",  "Wire Harness",            180, 80,  120, "Pune-WH1",      new BigDecimal("4500")));
        parts.add(new PartDTO("PART-SEN-1",  "ADAS Sensor Pack",         54, 40,  60,  "Pune-WH1",      new BigDecimal("28000"))); // LOW

        // Purchase orders
        String[][] poS = {
                {"SUP-001", "850000",  PurchaseOrderDTO.APPROVED,        "PART-ENG-1", "PART-CHA-1"},
                {"SUP-002", "1260000", PurchaseOrderDTO.PENDING_APPROVAL,"PART-INF-1", "PART-HRN-1"},
                {"SUP-003", "560000",  PurchaseOrderDTO.DISPATCHED,      "PART-WHL-2", "PART-WHL-1"},
                {"SUP-004", "340000",  PurchaseOrderDTO.APPROVED,        "PART-SEN-1"},
                {"SUP-005", "980000",  PurchaseOrderDTO.RECEIVED,        "PART-BAT-1"},
                {"SUP-006", "220000",  PurchaseOrderDTO.DRAFT,           "PART-WHL-1"},
                {"SUP-007", "180000",  PurchaseOrderDTO.APPROVED,        "PART-PNT-1"},
                {"SUP-001", "720000",  PurchaseOrderDTO.DISPATCHED,      "PART-CHA-2"},
                {"SUP-002", "1800000", PurchaseOrderDTO.PENDING_APPROVAL,"PART-MOT-EV"},
                {"SUP-005", "2600000", PurchaseOrderDTO.APPROVED,        "PART-BAT-EV"},
        };
        int i = 0;
        for (String[] s : poS) {
            List<POLineItemDTO> items = new ArrayList<>();
            int ln = 1;
            for (int k = 3; k < s.length; k++) {
                PartDTO part = findPart(s[k]);
                int qty = 20 + (k * 5);
                BigDecimal unit = part == null ? BigDecimal.ZERO : part.getUnitCost();
                items.add(new POLineItemDTO(ln++, s[k],
                        part == null ? s[k] : part.getPartName(),
                        qty, unit, unit.multiply(new BigDecimal(qty))));
            }
            PurchaseOrderDTO po = new PurchaseOrderDTO(
                    "PO-" + (5000 + i + 1), s[0], supplierName(s[0]),
                    items, new BigDecimal(s[1]), s[2],
                    LocalDate.now().minusDays(20 - i), "admin",
                    LocalDate.now().plusDays(10 + i));
            if (PurchaseOrderDTO.APPROVED.equals(s[2]) || PurchaseOrderDTO.RECEIVED.equals(s[2])
                    || PurchaseOrderDTO.DISPATCHED.equals(s[2])) {
                po.setApprovedBy("manager");
                po.setApprovalDate(LocalDate.now().minusDays(15 - i));
            }
            purchaseOrders.add(po);
            i++;
        }
        poSeq.set(5010);

        // GRNs for received POs
        goodsReceipts.add(new GoodsReceiptDTO("GRN-801", "PO-5005", LocalDate.now().minusDays(6),
                45, 45, GoodsReceiptDTO.PASSED, null));
        goodsReceipts.add(new GoodsReceiptDTO("GRN-802", "PO-5003", LocalDate.now().minusDays(2),
                60, 60, GoodsReceiptDTO.PASSED, null));
        goodsReceipts.add(new GoodsReceiptDTO("GRN-803", "PO-5008", LocalDate.now().minusDays(1),
                80, 80, GoodsReceiptDTO.ON_HOLD, "Cosmetic scratches on 3 units"));
        goodsReceipts.add(new GoodsReceiptDTO("GRN-804", "PO-5007", LocalDate.now().minusDays(4),
                70, 70, GoodsReceiptDTO.PASSED, null));
        grnSeq.set(804);

        // Shipments
        shipments.add(new ShipmentDTO("SHP-601", "PO-5003", "TRK-30001", "BlueDart Cargo",
                LocalDate.now().plusDays(2), null, ShipmentDTO.IN_TRANSIT));
        shipments.add(new ShipmentDTO("SHP-602", "PO-5005", "TRK-30002", "DHL Freight",
                LocalDate.now().minusDays(6), LocalDate.now().minusDays(6), ShipmentDTO.DELIVERED));
        shipments.add(new ShipmentDTO("SHP-603", "PO-5008", "TRK-30003", "Safex Logistics",
                LocalDate.now().minusDays(3), LocalDate.now().minusDays(1), ShipmentDTO.DELIVERED));
        shipments.add(new ShipmentDTO("SHP-604", "PO-5010", "TRK-30004", "Gati Freight",
                LocalDate.now().minusDays(1), null, ShipmentDTO.DELAYED));
        shipmentSeq.set(604);

        // Invoices
        invoices.add(new InvoiceDTO("INV-901", "SUP-005", "PO-5005", "GRN-801",
                new BigDecimal("980000"),  LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), InvoiceDTO.PAID));
        invoices.add(new InvoiceDTO("INV-902", "SUP-003", "PO-5003", "GRN-802",
                new BigDecimal("560000"),  LocalDate.now().minusDays(1), LocalDate.now().plusDays(29), InvoiceDTO.AUTHORIZED));
        invoices.add(new InvoiceDTO("INV-903", "SUP-001", "PO-5008", "GRN-803",
                new BigDecimal("720000"),  LocalDate.now(), LocalDate.now().plusDays(30), InvoiceDTO.PENDING));
        invoices.add(new InvoiceDTO("INV-904", "SUP-007", "PO-5007", "GRN-804",
                new BigDecimal("180000"),  LocalDate.now().minusDays(2), LocalDate.now().plusDays(28), InvoiceDTO.AUTHORIZED));
        invoiceSeq.set(904);
    }

    private void seedMaterialCatalog() {
        materialCatalog.clear();
        for (PartDTO p : parts) {
            if (p.getPartId() == null || !p.getPartId().startsWith("PART-")) continue;
            materialCatalog.add(new MaterialDTO(
                    p.getPartId(),
                    p.getPartName(),
                    p.getUnitCost() == null ? BigDecimal.ZERO : p.getUnitCost(),
                    p.getStockLevel()));
        }
    }

    private String supplierName(String supplierId) {
        for (SupplierDTO s : suppliers) if (s.getSupplierId().equals(supplierId)) return s.getSupplierName();
        return supplierId;
    }

    // ==================== helpers ====================
    private static String str(Map<String, Object> m, String k) {
        Object v = m == null ? null : m.get(k);
        return v == null ? null : v.toString();
    }
}
