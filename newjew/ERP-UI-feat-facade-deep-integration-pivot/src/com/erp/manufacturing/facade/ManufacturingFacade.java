package com.erp.manufacturing.facade;

import com.erp.model.dto.BomDTO;
import com.erp.model.dto.CarModelDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.model.dto.WorkCenterDTO;

import java.util.List;
import java.util.Map;

/**
 * High-level manufacturing use-case facade.
 *
 * PATTERN: Facade (Structural)
 */
public interface ManufacturingFacade {

    List<CarModelDTO> listCars();
    List<ProductionOrderDTO> listProductionOrders();
    List<BomDTO> listBoms();
    BomDTO getBomDetails(String bomId);
    List<MaterialDTO> listMaterials();
    List<RoutingStepDTO> listRouting(String productId);
    List<WorkCenterDTO> listWorkCenters();
    List<ExecutionLogDTO> listExecutionLogs(String orderId);
    Map<String, Integer> stats();

    CarModelDTO updateCarStatus(String vin, String status);
    BomDTO createBom(BomDTO dto);
    MaterialDTO createMaterial(MaterialDTO dto);
    ProductionOrderDTO createProductionOrder(ProductionOrderDTO dto);
    ProductionOrderDTO cancelProductionOrder(String orderId);
    String recordExecutionLog(ExecutionLogDTO log);
    QCCheckDTO submitQCCheck(QCCheckDTO dto);
}
