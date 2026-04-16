package com.erp.manufacturing.repository;

import com.erp.model.dto.BomDTO;
import com.erp.model.dto.CarModelDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.model.dto.WorkCenterDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistence boundary for the manufacturing module.
 *
 * SOLID: ISP/SRP - callers only depend on this narrow manufacturing store contract.
 */
public interface ManufacturingRepository {

    List<CarModelDTO> findCars();
    CarModelDTO updateCarStatus(String vin, String status);

    List<ProductionOrderDTO> findProductionOrders();
    ProductionOrderDTO findProductionOrder(String orderId);
    ProductionOrderDTO insertProductionOrder(ProductionOrderDTO dto);
    ProductionOrderDTO updateProductionOrderStatus(String orderId, String status, LocalDate actualEndDate);
    void applyExecutionToOrder(String orderId, double qtyProduced, double scrapQty,
                               LocalDateTime startTime, LocalDateTime endTime);

    List<BomDTO> findBomHeaders();
    BomDTO findBomById(String bomId);
    boolean hasDuplicateBomVersion(String productId, String bomVersion, String currentBomId);
    BomDTO insertBom(BomDTO bom);

    List<MaterialDTO> findMaterials();
    MaterialDTO findMaterial(String materialItemId);
    MaterialDTO insertMaterial(MaterialDTO material);

    List<RoutingStepDTO> findRouting(String productId);
    List<WorkCenterDTO> findWorkCenters();

    ExecutionLogDTO insertExecutionLog(ExecutionLogDTO dto);
    List<ExecutionLogDTO> findExecutionLogs(String orderId);

    QCCheckDTO insertQCCheck(QCCheckDTO dto);
    int countQcBreaches(double threshold);

    double availableStock(String materialItemId);
    void reserveStock(String materialItemId, double quantity);

    String glAccount(String costCenterCode);
    boolean hasAvailableOperator(String workCenterId);
    boolean isOperatorCertified(String operatorId, String workCenterId);
    boolean isMachineOnline(String machineId);

    String nextId(String tableName, String idColumn, String prefix);
}
