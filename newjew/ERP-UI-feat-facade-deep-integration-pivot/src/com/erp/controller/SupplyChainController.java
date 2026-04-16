package com.erp.controller;

import com.erp.exception.ERPException;
import com.erp.exception.ExceptionHandler;
import com.erp.exception.IntegrationException;
import com.erp.integration.IUIService;
import com.erp.integration.ServiceLocator;
import com.erp.integration.endpoints.SupplyChainEndpoints;
import com.erp.model.dto.GoodsReceiptDTO;
import com.erp.model.dto.InvoiceDTO;
import com.erp.model.dto.PartDTO;
import com.erp.model.dto.PurchaseOrderDTO;
import com.erp.model.dto.ShipmentDTO;
import com.erp.model.dto.SupplierDTO;

import javax.swing.*;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller for the Supply Chain & Purchasing module (SwiftChain).
 *
 * PATTERN: Observer (Behavioral) — SupplyChainListener
 * GRASP: Controller
 * GRASP: Pure Fabrication — coordinator between views and IUIService
 */
public class SupplyChainController {

    public interface SupplyChainListener {
        default void onSuppliersLoaded(List<SupplierDTO> list) {}
        default void onPurchaseOrdersLoaded(List<PurchaseOrderDTO> list) {}
        default void onInventoryLoaded(List<PartDTO> list) {}
        default void onLowStockLoaded(List<PartDTO> list) {}
        default void onStatsLoaded(Map<String, Integer> stats) {}
        default void onSCMEntityChanged(Object entity) {}
    }

    private final IUIService ui = ServiceLocator.getUIService();
    private final List<SupplyChainListener> listeners = new ArrayList<>();

    public void addListener(SupplyChainListener l) { if (l != null) listeners.add(l); }
    public void removeListener(SupplyChainListener l) { listeners.remove(l); }

    // ===== Reads =====

    public void loadSuppliers(Component owner) {
        submit(owner,
                () -> SupplyChainController.<List<SupplierDTO>>cast(
                        ui.fetchData(SupplyChainEndpoints.SCM_SUPPLIERS, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onSuppliersLoaded(list)),
                () -> loadSuppliers(owner));
    }

    public void loadPurchaseOrders(Component owner, String statusFilter) {
        Map<String, Object> p = new HashMap<>();
        if (statusFilter != null) p.put("status", statusFilter);
        submit(owner,
                () -> SupplyChainController.<List<PurchaseOrderDTO>>cast(
                        ui.fetchData(SupplyChainEndpoints.SCM_PO_LIST, p, List.class)),
                list -> listeners.forEach(l -> l.onPurchaseOrdersLoaded(list)),
                () -> loadPurchaseOrders(owner, statusFilter));
    }

    public void loadInventory(Component owner) {
        submit(owner,
                () -> SupplyChainController.<List<PartDTO>>cast(
                        ui.fetchData(SupplyChainEndpoints.SCM_INVENTORY, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onInventoryLoaded(list)),
                () -> loadInventory(owner));
    }

    public void loadLowStock(Component owner) {
        submit(owner,
                () -> SupplyChainController.<List<PartDTO>>cast(
                        ui.fetchData(SupplyChainEndpoints.SCM_LOW_STOCK, new HashMap<>(), List.class)),
                list -> listeners.forEach(l -> l.onLowStockLoaded(list)),
                () -> loadLowStock(owner));
    }

    public void loadStats(Component owner) {
        submit(owner,
                () -> ui.fetchData(SupplyChainEndpoints.SCM_STATS, new HashMap<>(), Map.class),
                stats -> listeners.forEach(l -> l.onStatsLoaded(stats)),
                () -> loadStats(owner));
    }

    // ===== Writes =====

    public void createPurchaseOrder(Component owner, PurchaseOrderDTO dto, Consumer<PurchaseOrderDTO> after) {
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_PO_CREATE, dto, PurchaseOrderDTO.class),
                created -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(created));
                    if (after != null) after.accept(created);
                },
                () -> createPurchaseOrder(owner, dto, after));
    }

    public void approvePurchaseOrder(Component owner, String poId, String approverUserId, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("poId", poId); p.put("approverUserId", approverUserId);
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_PO_APPROVE, p, PurchaseOrderDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> approvePurchaseOrder(owner, poId, approverUserId, after));
    }

    public void reorderPart(Component owner, String partId, int quantity, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("partId", partId); p.put("quantity", quantity);
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_REORDER, p, PartDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> reorderPart(owner, partId, quantity, after));
    }

    public void createGoodsReceipt(Component owner, GoodsReceiptDTO dto, Consumer<GoodsReceiptDTO> after) {
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_GRN_CREATE, dto, GoodsReceiptDTO.class),
                created -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(created));
                    if (after != null) after.accept(created);
                },
                () -> createGoodsReceipt(owner, dto, after));
    }

    public void updateShipmentStatus(Component owner, String shipmentId, String status, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("shipmentId", shipmentId); p.put("status", status);
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_SHIPMENT_UPDATE, p, ShipmentDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> updateShipmentStatus(owner, shipmentId, status, after));
    }

    public void createInvoice(Component owner, InvoiceDTO dto, Consumer<InvoiceDTO> after) {
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_INVOICE_CREATE, dto, InvoiceDTO.class),
                created -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(created));
                    if (after != null) after.accept(created);
                },
                () -> createInvoice(owner, dto, after));
    }

    public void verifyInvoice(Component owner, String invoiceId, BigDecimal expectedAmount, Runnable after) {
        Map<String, Object> p = new HashMap<>();
        p.put("invoiceId", invoiceId); p.put("expectedAmount", expectedAmount);
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_INVOICE_VERIFY, p, InvoiceDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> verifyInvoice(owner, invoiceId, expectedAmount, after));
    }

    public void payInvoice(Component owner, String invoiceId, Runnable after) {
        submit(owner,
                () -> ui.sendData(SupplyChainEndpoints.SCM_INVOICE_PAY, invoiceId, InvoiceDTO.class),
                updated -> {
                    listeners.forEach(l -> l.onSCMEntityChanged(updated));
                    if (after != null) after.run();
                },
                () -> payInvoice(owner, invoiceId, after));
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
                                IntegrationException.fetchFailed("supply-chain", c.getMessage()), retry);
                    }
                }
            }
        }.execute();
    }
}
