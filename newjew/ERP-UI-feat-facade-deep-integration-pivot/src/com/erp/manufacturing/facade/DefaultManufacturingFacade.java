package com.erp.manufacturing.facade;

import com.erp.exception.BusinessRuleException;
import com.erp.exception.IntegrationException;
import com.erp.exception.ValidationException;
import com.erp.manufacturing.command.ManufacturingCommandFactory;
import com.erp.manufacturing.integration.FinancePort;
import com.erp.manufacturing.integration.HRPort;
import com.erp.manufacturing.integration.InventoryPort;
import com.erp.manufacturing.integration.MaintenancePort;
import com.erp.manufacturing.integration.ProcurementPort;
import com.erp.manufacturing.integration.SalesPort;
import com.erp.manufacturing.repository.ManufacturingRepository;
import com.erp.manufacturing.rules.BomStructureRule;
import com.erp.manufacturing.rules.CapacityOverloadRule;
import com.erp.manufacturing.rules.ComponentStockRule;
import com.erp.manufacturing.rules.GIAccountMappingRule;
import com.erp.manufacturing.rules.ProductionOrderRuleContext;
import com.erp.manufacturing.rules.ProductionOrderRuleHandler;
import com.erp.manufacturing.rules.RequiredOrderDataRule;
import com.erp.manufacturing.rules.WorkOrderAssignmentRule;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.BomItemDTO;
import com.erp.model.dto.CarModelDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.model.dto.WorkCenterDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default manufacturing facade implementation.
 */
public class DefaultManufacturingFacade implements ManufacturingFacade {

    private static final double QC_THRESHOLD = 0.05;

    private final ManufacturingRepository repository;
    private final ManufacturingCommandFactory commandFactory;

    private final InventoryPort inventoryPort;
    private final ProcurementPort procurementPort;
    private final HRPort hrPort;
    private final FinancePort financePort;
    private final SalesPort salesPort;
    private final MaintenancePort maintenancePort;

    private final ProductionOrderRuleHandler createOrderRuleChain;

    public DefaultManufacturingFacade(ManufacturingRepository repository,
                                      ManufacturingCommandFactory commandFactory,
                                      InventoryPort inventoryPort,
                                      ProcurementPort procurementPort,
                                      HRPort hrPort,
                                      FinancePort financePort,
                                      SalesPort salesPort,
                                      MaintenancePort maintenancePort) {
        this.repository = repository;
        this.commandFactory = commandFactory;
        this.inventoryPort = inventoryPort;
        this.procurementPort = procurementPort;
        this.hrPort = hrPort;
        this.financePort = financePort;
        this.salesPort = salesPort;
        this.maintenancePort = maintenancePort;

        ProductionOrderRuleHandler head = new RequiredOrderDataRule();
        head.setNext(new BomStructureRule())
                .setNext(new ComponentStockRule())
                .setNext(new WorkOrderAssignmentRule())
                .setNext(new GIAccountMappingRule())
                .setNext(new CapacityOverloadRule());
        this.createOrderRuleChain = head;
    }

    @Override
    public List<CarModelDTO> listCars() {
        return repository.findCars();
    }

    @Override
    public List<ProductionOrderDTO> listProductionOrders() {
        List<ProductionOrderDTO> list = repository.findProductionOrders();
        LocalDate today = LocalDate.now();
        for (ProductionOrderDTO order : list) {
            if (order.getPlannedEndDate() != null
                    && order.getPlannedEndDate().isBefore(today)
                    && !ProductionOrderDTO.COMPLETED.equals(order.getStatus())
                    && !ProductionOrderDTO.CANCELLED.equals(order.getStatus())) {
                salesPort.notifyProductionDelay(order.getOrderId());
            }
        }
        return list;
    }

    @Override
    public List<BomDTO> listBoms() {
        return repository.findBomHeaders();
    }

    @Override
    public BomDTO getBomDetails(String bomId) {
        BomDTO bom = repository.findBomById(bomId);
        if (bom == null) {
            throw IntegrationException.fetchFailed("mfg/bom/details", "BOM not found: " + bomId);
        }
        if (bom.getItems() == null || bom.getItems().isEmpty()) {
            throw BusinessRuleException.invalidBomStructure(bomId, "missing required BOM components");
        }
        if (repository.hasDuplicateBomVersion(bom.getProductId(), bom.getBomVersion(), bom.getBomId())) {
            throw BusinessRuleException.duplicateBomVersion(bom.getProductId(), bom.getBomVersion());
        }
        if (bom.getBudgetLimit() != null && bom.getTotalCost() != null
                && bom.getTotalCost().compareTo(bom.getBudgetLimit()) > 0) {
            throw BusinessRuleException.bomCostExceedsBudget(
                    bom.getProductName(), bom.getTotalCost(), bom.getBudgetLimit());
        }
        return bom;
    }

    @Override
    public List<MaterialDTO> listMaterials() {
        return repository.findMaterials();
    }

    @Override
    public List<RoutingStepDTO> listRouting(String productId) {
        List<RoutingStepDTO> steps = repository.findRouting(productId);
        validateRoutingGaps(steps);
        return steps;
    }

    @Override
    public List<WorkCenterDTO> listWorkCenters() {
        return repository.findWorkCenters();
    }

    @Override
    public List<ExecutionLogDTO> listExecutionLogs(String orderId) {
        return repository.findExecutionLogs(orderId);
    }

    @Override
    public Map<String, Integer> stats() {
        List<ProductionOrderDTO> orders = repository.findProductionOrders();
        int pending = 0;
        int inProgress = 0;
        int completed = 0;
        int overdue = 0;

        LocalDate today = LocalDate.now();
        for (ProductionOrderDTO order : orders) {
            if (ProductionOrderDTO.PENDING.equals(order.getStatus())) pending++;
            if (ProductionOrderDTO.IN_PROGRESS.equals(order.getStatus())) inProgress++;
            if (ProductionOrderDTO.COMPLETED.equals(order.getStatus())) completed++;
            if (order.getPlannedEndDate() != null
                    && order.getPlannedEndDate().isBefore(today)
                    && !ProductionOrderDTO.COMPLETED.equals(order.getStatus())
                    && !ProductionOrderDTO.CANCELLED.equals(order.getStatus())) {
                overdue++;
            }
        }

        int qcFails = repository.countQcBreaches(QC_THRESHOLD);

        Map<String, Integer> out = new HashMap<>();
        out.put("cars", repository.findCars().size());
        out.put("pendingOrders", pending);
        out.put("inProgress", inProgress);
        out.put("completed", completed);
        out.put("qcFails", qcFails);
        out.put("workCenters", repository.findWorkCenters().size());
        out.put("overdueOrders", overdue);
        return out;
    }

    @Override
    public CarModelDTO updateCarStatus(String vin, String status) {
        return repository.updateCarStatus(vin, status);
    }

    @Override
    public BomDTO createBom(BomDTO dto) {
        if (dto.getBomId() == null || dto.getBomId().trim().isEmpty()) {
            throw ValidationException.requiredField("bom_id", null);
        }
        if (dto.getBomVersion() == null || dto.getBomVersion().trim().isEmpty()) {
            throw ValidationException.requiredField("bom_version", null);
        }
        if (dto.getProductId() == null || dto.getProductId().trim().isEmpty()) {
            throw ValidationException.requiredField("product_id", null);
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw BusinessRuleException.invalidBomStructure(dto.getBomId(), "at least one material item is required");
        }

        if (repository.hasDuplicateBomVersion(dto.getProductId(), dto.getBomVersion(), dto.getBomId())) {
            throw BusinessRuleException.duplicateBomVersion(dto.getProductId(), dto.getBomVersion());
        }

        BigDecimal totalCost = BigDecimal.ZERO;
        for (BomItemDTO item : dto.getItems()) {
            if (item.getQuantity() <= 0) {
                throw ValidationException.invalidQuantityInput(null, String.valueOf(item.getQuantity()));
            }
            MaterialDTO material = repository.findMaterial(item.getMaterialItemId());
            if (material == null) {
                throw IntegrationException.sendFailed("mfg/bom/create",
                        "Material not found: " + item.getMaterialItemId());
            }
            item.setPartName(material.getPartName());
            item.setUnitCost(material.getUnitCost());
            item.setLineCost(material.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity())));
            totalCost = totalCost.add(item.getLineCost());
        }
        dto.setTotalCost(totalCost);

        if (dto.getBudgetLimit() == null) {
            dto.setBudgetLimit(totalCost);
        }

        return repository.insertBom(dto);
    }

    @Override
    public MaterialDTO createMaterial(MaterialDTO dto) {
        if (dto.getMaterialItemId() == null || dto.getMaterialItemId().trim().isEmpty()) {
            throw ValidationException.requiredField("material_item_id", null);
        }
        if (dto.getPartName() == null || dto.getPartName().trim().isEmpty()) {
            throw ValidationException.requiredField("part_name", null);
        }
        if (dto.getUnitCost() == null || dto.getUnitCost().doubleValue() <= 0) {
            throw ValidationException.invalidQuantityInput(null,
                    dto.getUnitCost() == null ? "null" : dto.getUnitCost().toPlainString());
        }
        if (dto.getAvailableQty() < 0) {
            throw ValidationException.invalidQuantityInput(null, String.valueOf(dto.getAvailableQty()));
        }
        if (repository.findMaterial(dto.getMaterialItemId()) != null) {
            throw IntegrationException.sendFailed("mfg/materials/create",
                    "Material already exists: " + dto.getMaterialItemId());
        }
        return repository.insertMaterial(dto);
    }

    @Override
    public ProductionOrderDTO createProductionOrder(ProductionOrderDTO dto) {
        BomDTO bom = repository.findBomById(dto.getBomId());
        if (bom == null) {
            throw IntegrationException.sendFailed("mfg/production-orders/create", "BOM not found: " + dto.getBomId());
        }

        List<RoutingStepDTO> routing = repository.findRouting(dto.getProductId());
        List<WorkCenterDTO> wcs = repository.findWorkCenters();
        ProductionOrderRuleContext ctx = new ProductionOrderRuleContext(
                dto, bom, routing, wcs, inventoryPort, procurementPort, hrPort, financePort);
        createOrderRuleChain.check(ctx);

        return commandFactory.createProductionOrderCommand(dto, bom).execute();
    }

    @Override
    public ProductionOrderDTO cancelProductionOrder(String orderId) {
        ProductionOrderDTO order = repository.findProductionOrder(orderId);
        if (order == null) {
            throw IntegrationException.sendFailed("mfg/production-orders/cancel", "Production order not found: " + orderId);
        }
        if (ProductionOrderDTO.IN_PROGRESS.equals(order.getStatus())) {
            throw BusinessRuleException.productionOrderCancellationBlocked(orderId);
        }
        return commandFactory.cancelProductionOrderCommand(orderId).execute();
    }

    @Override
    public String recordExecutionLog(ExecutionLogDTO log) {
        if (log.getOrderId() == null || log.getOrderId().trim().isEmpty()) {
            throw ValidationException.requiredField("order_id", null);
        }
        if (log.getQtyProduced() <= 0 && log.getScrapQty() <= 0) {
            throw ValidationException.invalidQuantityInput(null,
                    "qty_produced=" + log.getQtyProduced() + ", scrap_qty=" + log.getScrapQty());
        }
        if (log.getStartTime() == null) {
            log.setStartTime(LocalDateTime.now());
        }

        ProductionOrderDTO order = repository.findProductionOrder(log.getOrderId());
        if (order == null) {
            throw IntegrationException.sendFailed("mfg/execution/log", "Production order not found: " + log.getOrderId());
        }

        List<RoutingStepDTO> routing = repository.findRouting(order.getProductId());
        if (!routing.isEmpty()) {
            String wcId = routing.get(0).getWorkCenterId();
            if (!hrPort.isCertified(log.getOperatorId(), wcId)) {
                throw BusinessRuleException.unauthorizedOperator(log.getOperatorId());
            }
        }

        if (!maintenancePort.isMachineOnline(log.getMachineId())) {
            throw BusinessRuleException.machineCommunication(log.getMachineId());
        }

        LocalDate today = LocalDate.now();
        if (order.getPlannedEndDate() != null
                && order.getPlannedEndDate().isBefore(today)
                && !ProductionOrderDTO.COMPLETED.equals(order.getStatus())
                && !ProductionOrderDTO.CANCELLED.equals(order.getStatus())) {
            salesPort.notifyProductionDelay(order.getOrderId());
            throw BusinessRuleException.productionOrderOverdue(order.getOrderId());
        }

        return commandFactory.recordExecutionCommand(log).execute();
    }

    @Override
    public QCCheckDTO submitQCCheck(QCCheckDTO dto) {
        if (dto.getSampleSize() <= 0) {
            throw ValidationException.invalidQuantityInput(null, String.valueOf(dto.getSampleSize()));
        }
        if (dto.getDefectsCount() < 0) {
            throw ValidationException.invalidQuantityInput(null, String.valueOf(dto.getDefectsCount()));
        }

        ProductionOrderDTO order = repository.findProductionOrder(dto.getProductionOrderId());
        if (order == null) {
            throw IntegrationException.sendFailed("mfg/qc/submit", "Production order not found: " + dto.getProductionOrderId());
        }

        List<RoutingStepDTO> routing = repository.findRouting(order.getProductId());
        if (!routing.isEmpty()) {
            String wcId = routing.get(0).getWorkCenterId();
            if (!hrPort.isCertified(dto.getInspectorId(), wcId)) {
                throw BusinessRuleException.unauthorizedOperator(dto.getInspectorId());
            }
        }

        QCCheckDTO saved = commandFactory.submitQcCommand(dto).execute();
        if (saved.defectRate() > QC_THRESHOLD) {
            throw BusinessRuleException.qcDefectThresholdExceeded(saved.getProductionOrderId(), saved.defectRate());
        }
        return saved;
    }

    private static void validateRoutingGaps(List<RoutingStepDTO> steps) {
        if (steps == null || steps.isEmpty()) return;

        Map<String, List<RoutingStepDTO>> byRouting = new HashMap<>();
        for (RoutingStepDTO step : steps) {
            byRouting.computeIfAbsent(step.getRoutingId(), k -> new ArrayList<>()).add(step);
        }

        for (Map.Entry<String, List<RoutingStepDTO>> e : byRouting.entrySet()) {
            List<RoutingStepDTO> sorted = e.getValue();
            sorted.sort((a, b) -> Integer.compare(a.getSequenceNumber(), b.getSequenceNumber()));
            for (int i = 1; i < sorted.size(); i++) {
                int prev = sorted.get(i - 1).getSequenceNumber();
                int cur = sorted.get(i).getSequenceNumber();
                if (cur - prev > 1) {
                    throw BusinessRuleException.routingStepGap(e.getKey(), prev + 1);
                }
            }
        }
    }
}
