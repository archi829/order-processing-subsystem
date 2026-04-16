package com.erp.controller;

import com.erp.exception.ERPException;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.IntegrationException;
import com.erp.integration.IUIService;
import com.erp.integration.ServiceLocator;
import com.erp.integration.endpoints.ManufacturingEndpoints;
import com.erp.model.dto.BomDTO;
import com.erp.model.dto.CarModelDTO;
import com.erp.model.dto.ExecutionLogDTO;
import com.erp.model.dto.MaterialDTO;
import com.erp.model.dto.ProductionOrderDTO;
import com.erp.model.dto.QCCheckDTO;
import com.erp.model.dto.RoutingStepDTO;
import com.erp.model.dto.WorkCenterDTO;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for the Manufacturing module.
 *
 * PATTERN: Observer (Behavioral) — ManufacturingListener
 * GRASP: Controller — brokers between views and IUIService
 * GRASP: Pure Fabrication — artificial coordinator class; it is not a real
 *        domain entity, it exists purely to keep views off the EDT and route
 *        exceptions through {@link ExceptionHandler}.
 */
public class ManufacturingController {

    public interface ManufacturingListener {
        default void onCarsLoaded(List<CarModelDTO> list) {}
        default void onProductionOrdersLoaded(List<ProductionOrderDTO> list) {}
        default void onBomListLoaded(List<BomDTO> list) {}
        default void onBomDetailsLoaded(BomDTO bom) {}
        default void onMaterialsLoaded(List<MaterialDTO> list) {}
        default void onRoutingLoaded(List<RoutingStepDTO> list) {}
        default void onWorkCentersLoaded(List<WorkCenterDTO> list) {}
        default void onExecutionLogsLoaded(List<ExecutionLogDTO> list) {}
        default void onStatsLoaded(Map<String, Integer> stats) {}
        default void onMfgEntityChanged(Object entity) {}
    }

    private final IUIService ui = ServiceLocator.getUIService();
    private final List<ManufacturingListener> listeners = new ArrayList<>();

    public void addListener(ManufacturingListener l) { if (l != null) listeners.add(l); }
    public void removeListener(ManufacturingListener l) { listeners.remove(l); }

    // ===== Reads =====

    public void loadCars(Component owner) {
        submit(owner,
                () -> ManufacturingController.<List<CarModelDTO>>cast(
                        ui.fetchData(ManufacturingEndpoints.MFG_CARS_LIST, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onCarsLoaded(list)),
                () -> loadCars(owner));
    }

    public void loadProductionOrders(Component owner) {
        submit(owner,
                () -> ManufacturingController.<List<ProductionOrderDTO>>cast(
                        ui.fetchData(ManufacturingEndpoints.MFG_PRODUCTION_ORDERS, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onProductionOrdersLoaded(list)),
                () -> loadProductionOrders(owner));
    }

    public void loadBomList(Component owner) {
        submit(owner,
                () -> ManufacturingController.<List<BomDTO>>cast(
                        ui.fetchData(ManufacturingEndpoints.MFG_BOM_LIST, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onBomListLoaded(list)),
                () -> loadBomList(owner));
    }

    public void loadBomDetails(Component owner, String bomId) {
        Map<String, Object> p = new HashMap<>();
        p.put("bomId", bomId);
        submit(owner,
                () -> ui.fetchData(ManufacturingEndpoints.MFG_BOM_DETAILS, p, BomDTO.class),
                bom -> listeners.forEach(l -> l.onBomDetailsLoaded(bom)),
                () -> loadBomDetails(owner, bomId));
    }

    public void loadRouting(Component owner, String productId) {
        Map<String, Object> p = new HashMap<>();
        if (productId != null) p.put("productId", productId);
        submit(owner,
                () -> ManufacturingController.<List<RoutingStepDTO>>cast(
                        ui.fetchData(ManufacturingEndpoints.MFG_ROUTING, p, List.class)),
                list -> listeners.forEach(l -> l.onRoutingLoaded(list)),
                () -> loadRouting(owner, productId));
    }

            public void loadMaterials(Component owner) {
            submit(owner,
                () -> ManufacturingController.<List<MaterialDTO>>cast(
                    ui.fetchData(ManufacturingEndpoints.MFG_MATERIALS_LIST, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onMaterialsLoaded(list)),
                () -> loadMaterials(owner));
            }

    public void loadWorkCenters(Component owner) {
        submit(owner,
                () -> ManufacturingController.<List<WorkCenterDTO>>cast(
                        ui.fetchData(ManufacturingEndpoints.MFG_WORK_CENTERS, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onWorkCentersLoaded(list)),
                () -> loadWorkCenters(owner));
    }

        public void loadExecutionLogs(Component owner, String orderId) {
        Map<String, Object> p = new HashMap<>();
        if (orderId != null && !orderId.trim().isEmpty()) p.put("orderId", orderId);
        submit(owner,
            () -> ManufacturingController.<List<ExecutionLogDTO>>cast(
                ui.fetchData(ManufacturingEndpoints.MFG_EXECUTION_LOGS, p, List.class)),
            list -> listeners.forEach(l -> l.onExecutionLogsLoaded(list)),
            () -> loadExecutionLogs(owner, orderId));
        }

    public void loadStats(Component owner) {
        submit(owner,
                () -> ui.fetchData(ManufacturingEndpoints.MFG_STATS, new HashMap<>(), Map.class),
                stats -> listeners.forEach(l -> l.onStatsLoaded(stats)),
                () -> loadStats(owner));
    }

    // ===== Writes =====

    public void updateCarStatus(Component owner, String vin, String newStatus, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("vin", vin); p.put("status", newStatus);
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_CAR_STATUS_UPDATE, p, CarModelDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onMfgEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> updateCarStatus(owner, vin, newStatus, after));
    }

    public void createProductionOrder(Component owner, ProductionOrderDTO dto, Consumer<ProductionOrderDTO> after) {
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_PRODUCTION_ORDER_CREATE, dto, ProductionOrderDTO.class),
                created -> {
                    listeners.forEach(l -> l.onMfgEntityChanged(created));
                    if (after != null) after.accept(created);
                },
                () -> createProductionOrder(owner, dto, after));
    }

    public void createBom(Component owner, BomDTO dto, Consumer<BomDTO> after) {
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_BOM_CREATE, dto, BomDTO.class),
                created -> {
                    listeners.forEach(l -> l.onMfgEntityChanged(created));
                    if (after != null) after.accept(created);
                },
                () -> createBom(owner, dto, after));
    }

    public void createMaterial(Component owner, MaterialDTO dto, Consumer<MaterialDTO> after) {
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_MATERIAL_CREATE, dto, MaterialDTO.class),
                created -> {
                    listeners.forEach(l -> l.onMfgEntityChanged(created));
                    if (after != null) after.accept(created);
                },
                () -> createMaterial(owner, dto, after));
    }

    public void cancelProductionOrder(Component owner, String orderId, Runnable after) {
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_PRODUCTION_ORDER_CANCEL, orderId, ProductionOrderDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onMfgEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> cancelProductionOrder(owner, orderId, after));
    }

    public void recordExecutionLog(Component owner, ExecutionLogDTO log, Runnable after) {
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_EXECUTION_LOG, log, String.class),
                ok -> { if (after != null) after.run(); },
                () -> recordExecutionLog(owner, log, after));
    }

    public void submitQCCheck(Component owner, QCCheckDTO dto, Consumer<QCCheckDTO> after) {
        submit(owner,
                () -> ui.sendData(ManufacturingEndpoints.MFG_QC_SUBMIT, dto, QCCheckDTO.class),
                saved -> {
                    listeners.forEach(l -> l.onMfgEntityChanged(saved));
                    if (after != null) after.accept(saved);
                },
                () -> submitQCCheck(owner, dto, after));
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
                                IntegrationException.fetchFailed("manufacturing", c.getMessage()), retry);
                    }
                }
            }
        }.execute();
    }
}
