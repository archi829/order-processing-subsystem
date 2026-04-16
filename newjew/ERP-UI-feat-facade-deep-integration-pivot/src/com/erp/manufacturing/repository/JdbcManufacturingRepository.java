package com.erp.manufacturing.repository;

import com.erp.exception.IntegrationException;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-backed manufacturing repository.
 */
public class JdbcManufacturingRepository implements ManufacturingRepository {

    private final String jdbcUrl;

    public JdbcManufacturingRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public List<CarModelDTO> findCars() {
        String sql = "SELECT vin, model_name, chassis_type, build_status, assembly_line_id, started_at "
                + "FROM mfg_car ORDER BY started_at DESC";
        List<CarModelDTO> out = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new CarModelDTO(
                        rs.getString("vin"),
                        rs.getString("model_name"),
                        rs.getString("chassis_type"),
                        rs.getString("build_status"),
                        rs.getString("assembly_line_id"),
                        parseTs(rs.getString("started_at"))
                ));
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/cars/list", e.getMessage());
        }
    }

    @Override
    public CarModelDTO updateCarStatus(String vin, String status) {
        String sql = "UPDATE mfg_car SET build_status=? WHERE vin=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, vin);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw IntegrationException.sendFailed("mfg/cars/status", "Car not found: " + vin);
            }
            return findCarByVin(vin, c);
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/cars/status", e.getMessage());
        }
    }

    @Override
    public List<ProductionOrderDTO> findProductionOrders() {
        String sql = "SELECT order_id, order_date, planned_start_date, planned_end_date, status, product_id, "
                + "product_name, priority, actual_start_date, actual_end_date, bom_id, qty_planned, qty_produced, scrap_qty "
                + "FROM mfg_production_order ORDER BY order_date DESC, order_id DESC";
        List<ProductionOrderDTO> out = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ProductionOrderDTO dto = new ProductionOrderDTO();
                dto.setOrderId(rs.getString("order_id"));
                dto.setOrderDate(parseDate(rs.getString("order_date")));
                dto.setPlannedStartDate(parseDate(rs.getString("planned_start_date")));
                dto.setPlannedEndDate(parseDate(rs.getString("planned_end_date")));
                dto.setStatus(rs.getString("status"));
                dto.setProductId(rs.getString("product_id"));
                dto.setProductName(rs.getString("product_name"));
                dto.setPriority(rs.getString("priority"));
                dto.setActualStartDate(parseDate(rs.getString("actual_start_date")));
                dto.setActualEndDate(parseDate(rs.getString("actual_end_date")));
                dto.setBomId(rs.getString("bom_id"));
                dto.setQtyPlanned(rs.getInt("qty_planned"));
                dto.setQtyProduced(rs.getInt("qty_produced"));
                dto.setScrapQty(rs.getInt("scrap_qty"));
                out.add(dto);
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/production-orders/list", e.getMessage());
        }
    }

    @Override
    public ProductionOrderDTO findProductionOrder(String orderId) {
        String sql = "SELECT order_id, order_date, planned_start_date, planned_end_date, status, product_id, product_name, "
                + "priority, actual_start_date, actual_end_date, bom_id, qty_planned, qty_produced, scrap_qty "
                + "FROM mfg_production_order WHERE order_id=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ProductionOrderDTO dto = new ProductionOrderDTO();
                dto.setOrderId(rs.getString("order_id"));
                dto.setOrderDate(parseDate(rs.getString("order_date")));
                dto.setPlannedStartDate(parseDate(rs.getString("planned_start_date")));
                dto.setPlannedEndDate(parseDate(rs.getString("planned_end_date")));
                dto.setStatus(rs.getString("status"));
                dto.setProductId(rs.getString("product_id"));
                dto.setProductName(rs.getString("product_name"));
                dto.setPriority(rs.getString("priority"));
                dto.setActualStartDate(parseDate(rs.getString("actual_start_date")));
                dto.setActualEndDate(parseDate(rs.getString("actual_end_date")));
                dto.setBomId(rs.getString("bom_id"));
                dto.setQtyPlanned(rs.getInt("qty_planned"));
                dto.setQtyProduced(rs.getInt("qty_produced"));
                dto.setScrapQty(rs.getInt("scrap_qty"));
                return dto;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/production-orders/list", e.getMessage());
        }
    }

    @Override
    public ProductionOrderDTO insertProductionOrder(ProductionOrderDTO dto) {
        String sql = "INSERT INTO mfg_production_order (order_id, order_date, planned_start_date, planned_end_date, status, "
                + "product_id, product_name, priority, actual_start_date, actual_end_date, bom_id, qty_planned, qty_produced, scrap_qty) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dto.getOrderId());
            ps.setString(2, str(dto.getOrderDate()));
            ps.setString(3, str(dto.getPlannedStartDate()));
            ps.setString(4, str(dto.getPlannedEndDate()));
            ps.setString(5, dto.getStatus());
            ps.setString(6, dto.getProductId());
            ps.setString(7, dto.getProductName());
            ps.setString(8, dto.getPriority());
            ps.setString(9, str(dto.getActualStartDate()));
            ps.setString(10, str(dto.getActualEndDate()));
            ps.setString(11, dto.getBomId());
            ps.setInt(12, dto.getQtyPlanned());
            ps.setInt(13, dto.getQtyProduced());
            ps.setInt(14, dto.getScrapQty());
            ps.executeUpdate();
            return dto;
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/production-orders/create", e.getMessage());
        }
    }

    @Override
    public ProductionOrderDTO updateProductionOrderStatus(String orderId, String status, LocalDate actualEndDate) {
        String sql = "UPDATE mfg_production_order SET status=?, actual_end_date=? WHERE order_id=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, str(actualEndDate));
            ps.setString(3, orderId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw IntegrationException.sendFailed("mfg/production-orders/cancel", "Order not found: " + orderId);
            }
            return findProductionOrder(orderId);
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/production-orders/cancel", e.getMessage());
        }
    }

    @Override
    public void applyExecutionToOrder(String orderId, double qtyProduced, double scrapQty,
                                      LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "UPDATE mfg_production_order "
                + "SET qty_produced = qty_produced + ?, scrap_qty = scrap_qty + ?, "
                + "actual_start_date = COALESCE(actual_start_date, ?), "
                + "actual_end_date = CASE WHEN ? IS NULL THEN actual_end_date ELSE ? END, "
                + "status = CASE WHEN ? IS NULL THEN 'IN_PROGRESS' ELSE status END "
                + "WHERE order_id = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, (int) Math.round(qtyProduced));
            ps.setInt(2, (int) Math.round(scrapQty));
            ps.setString(3, startTime == null ? null : startTime.toLocalDate().toString());
            ps.setString(4, endTime == null ? null : endTime.toString());
            ps.setString(5, endTime == null ? null : endTime.toLocalDate().toString());
            ps.setString(6, endTime == null ? null : endTime.toString());
            ps.setString(7, orderId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw IntegrationException.sendFailed("mfg/execution/log", "Order not found: " + orderId);
            }
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/execution/log", e.getMessage());
        }
    }

    @Override
    public List<BomDTO> findBomHeaders() {
        String sql = "SELECT bom_id, bom_version, is_active, product_id, product_name, budget_limit "
                + "FROM mfg_bom ORDER BY product_name, bom_version DESC";
        List<BomDTO> out = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BomDTO dto = new BomDTO();
                dto.setBomId(rs.getString("bom_id"));
                dto.setBomVersion(rs.getString("bom_version"));
                dto.setActive(rs.getInt("is_active") == 1);
                dto.setProductId(rs.getString("product_id"));
                dto.setProductName(rs.getString("product_name"));
                dto.setBudgetLimit(BigDecimal.valueOf(rs.getDouble("budget_limit")));
                dto.setTotalCost(totalBomCost(c, dto.getBomId(), dto.getBomVersion()));
                out.add(dto);
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/bom/list", e.getMessage());
        }
    }

    @Override
    public BomDTO findBomById(String bomId) {
        String headerSql = "SELECT bom_id, bom_version, is_active, product_id, product_name, budget_limit "
                + "FROM mfg_bom WHERE bom_id=? ORDER BY is_active DESC, bom_version DESC LIMIT 1";
        String itemSql = "SELECT material_item_id, part_name, unit_cost, quantity FROM mfg_bom_item "
                + "WHERE bom_id=? AND bom_version=? ORDER BY material_item_id";
        try (Connection c = open(); PreparedStatement h = c.prepareStatement(headerSql)) {
            h.setString(1, bomId);
            try (ResultSet rs = h.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                BomDTO dto = new BomDTO();
                dto.setBomId(rs.getString("bom_id"));
                dto.setBomVersion(rs.getString("bom_version"));
                dto.setActive(rs.getInt("is_active") == 1);
                dto.setProductId(rs.getString("product_id"));
                dto.setProductName(rs.getString("product_name"));
                dto.setBudgetLimit(BigDecimal.valueOf(rs.getDouble("budget_limit")));
                List<BomItemDTO> items = new ArrayList<>();
                try (PreparedStatement i = c.prepareStatement(itemSql)) {
                    i.setString(1, dto.getBomId());
                    i.setString(2, dto.getBomVersion());
                    try (ResultSet ir = i.executeQuery()) {
                        while (ir.next()) {
                            double qty = ir.getDouble("quantity");
                            BigDecimal unit = BigDecimal.valueOf(ir.getDouble("unit_cost"));
                            items.add(new BomItemDTO(
                                    ir.getString("material_item_id"),
                                    ir.getString("part_name"),
                                    qty,
                                    unit,
                                    unit.multiply(BigDecimal.valueOf(qty))
                            ));
                        }
                    }
                }
                dto.setItems(items);
                dto.setTotalCost(totalBomCost(c, dto.getBomId(), dto.getBomVersion()));
                return dto;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/bom/details", e.getMessage());
        }
    }

    @Override
    public boolean hasDuplicateBomVersion(String productId, String bomVersion, String currentBomId) {
        String sql = "SELECT COUNT(1) cnt FROM mfg_bom WHERE product_id=? AND bom_version=? "
                + "AND (? IS NULL OR bom_id <> ?)";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, productId);
            ps.setString(2, bomVersion);
            ps.setString(3, currentBomId);
            ps.setString(4, currentBomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/bom/list", e.getMessage());
        }
    }

    @Override
    public BomDTO insertBom(BomDTO bom) {
        String headerSql = "INSERT INTO mfg_bom (bom_id, bom_version, is_active, product_id, product_name, budget_limit) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO mfg_bom_item (bom_id, bom_version, material_item_id, part_name, unit_cost, quantity) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection c = open()) {
            c.setAutoCommit(false);

            try (PreparedStatement h = c.prepareStatement(headerSql)) {
                h.setString(1, bom.getBomId());
                h.setString(2, bom.getBomVersion());
                h.setInt(3, bom.isActive() ? 1 : 0);
                h.setString(4, bom.getProductId());
                h.setString(5, bom.getProductName());
                h.setDouble(6, bom.getBudgetLimit() == null ? 0 : bom.getBudgetLimit().doubleValue());
                h.executeUpdate();
            }

            try (PreparedStatement i = c.prepareStatement(itemSql)) {
                for (BomItemDTO item : bom.getItems()) {
                    i.setString(1, bom.getBomId());
                    i.setString(2, bom.getBomVersion());
                    i.setString(3, item.getMaterialItemId());
                    i.setString(4, item.getPartName());
                    i.setDouble(5, item.getUnitCost().doubleValue());
                    i.setDouble(6, item.getQuantity());
                    i.addBatch();
                }
                i.executeBatch();
            }

            c.commit();
            c.setAutoCommit(true);
            bom.setTotalCost(totalBomCost(c, bom.getBomId(), bom.getBomVersion()));
            return bom;
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/bom/create", e.getMessage());
        }
    }

    @Override
    public List<MaterialDTO> findMaterials() {
        String sql = "SELECT m.material_item_id, m.part_name, m.unit_cost, COALESCE(s.available_qty, 0) available_qty "
                + "FROM mfg_material_master m "
                + "LEFT JOIN mfg_component_stock s ON s.material_item_id = m.material_item_id "
                + "ORDER BY m.material_item_id";

        List<MaterialDTO> out = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new MaterialDTO(
                        rs.getString("material_item_id"),
                        rs.getString("part_name"),
                        BigDecimal.valueOf(rs.getDouble("unit_cost")),
                        rs.getDouble("available_qty")
                ));
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/materials/list", e.getMessage());
        }
    }

    @Override
    public MaterialDTO findMaterial(String materialItemId) {
        String sql = "SELECT m.material_item_id, m.part_name, m.unit_cost, COALESCE(s.available_qty, 0) available_qty "
                + "FROM mfg_material_master m "
                + "LEFT JOIN mfg_component_stock s ON s.material_item_id = m.material_item_id "
                + "WHERE m.material_item_id = ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, materialItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new MaterialDTO(
                        rs.getString("material_item_id"),
                        rs.getString("part_name"),
                        BigDecimal.valueOf(rs.getDouble("unit_cost")),
                        rs.getDouble("available_qty")
                );
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/materials/list", e.getMessage());
        }
    }

    @Override
    public MaterialDTO insertMaterial(MaterialDTO material) {
        String insertMaterial = "INSERT INTO mfg_material_master (material_item_id, part_name, unit_cost) VALUES (?, ?, ?)";
        String insertStock = "INSERT INTO mfg_component_stock (material_item_id, available_qty) VALUES (?, ?)";

        try (Connection c = open()) {
            c.setAutoCommit(false);

            try (PreparedStatement im = c.prepareStatement(insertMaterial)) {
                im.setString(1, material.getMaterialItemId());
                im.setString(2, material.getPartName());
                im.setDouble(3, material.getUnitCost().doubleValue());
                im.executeUpdate();
            }

            try (PreparedStatement is = c.prepareStatement(insertStock)) {
                is.setString(1, material.getMaterialItemId());
                is.setDouble(2, material.getAvailableQty());
                is.executeUpdate();
            }

            c.commit();
            c.setAutoCommit(true);
            return material;
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/materials/create", e.getMessage());
        }
    }

    @Override
    public List<RoutingStepDTO> findRouting(String productId) {
        String sql = "SELECT routing_id, product_id, operation_id, sequence_number, operation_name, work_center_id, setup_time, run_time "
                + "FROM mfg_routing_step WHERE (? IS NULL OR product_id=?) ORDER BY routing_id, sequence_number";
        List<RoutingStepDTO> out = new ArrayList<>();
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, productId == null || productId.isEmpty() ? null : productId);
            ps.setString(2, productId == null || productId.isEmpty() ? null : productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RoutingStepDTO dto = new RoutingStepDTO();
                    dto.setRoutingId(rs.getString("routing_id"));
                    dto.setOperationId(rs.getString("operation_id"));
                    dto.setSequenceNumber(rs.getInt("sequence_number"));
                    dto.setOperationName(rs.getString("operation_name"));
                    dto.setWorkCenterId(rs.getString("work_center_id"));
                    dto.setSetupTime(rs.getDouble("setup_time"));
                    dto.setRunTime(rs.getDouble("run_time"));
                    out.add(dto);
                }
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/routing/list", e.getMessage());
        }
    }

    @Override
    public List<WorkCenterDTO> findWorkCenters() {
        String sql = "SELECT wc_id, wc_name, wc_type, capacity_hours, utilization_pct, location "
                + "FROM mfg_work_center ORDER BY wc_id";
        List<WorkCenterDTO> out = new ArrayList<>();
        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new WorkCenterDTO(
                        rs.getString("wc_id"),
                        rs.getString("wc_name"),
                        rs.getString("wc_type"),
                        rs.getDouble("capacity_hours"),
                        rs.getDouble("utilization_pct"),
                        rs.getString("location")
                ));
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/work-centers/list", e.getMessage());
        }
    }

    @Override
    public ExecutionLogDTO insertExecutionLog(ExecutionLogDTO dto) {
        String sql = "INSERT INTO mfg_execution_log (log_id, order_id, start_time, end_time, operator_id, qty_produced, scrap_qty, machine_id, note) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        if (dto.getLogId() == null || dto.getLogId().trim().isEmpty()) {
            dto.setLogId(nextId("mfg_execution_log", "log_id", "LOG-"));
        }
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dto.getLogId());
            ps.setString(2, dto.getOrderId());
            ps.setString(3, str(dto.getStartTime()));
            ps.setString(4, str(dto.getEndTime()));
            ps.setString(5, dto.getOperatorId());
            ps.setDouble(6, dto.getQtyProduced());
            ps.setDouble(7, dto.getScrapQty());
            ps.setString(8, dto.getMachineId());
            ps.setString(9, dto.getNote());
            ps.executeUpdate();
            return dto;
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/execution/log", e.getMessage());
        }
    }

    @Override
    public List<ExecutionLogDTO> findExecutionLogs(String orderId) {
        String sql = "SELECT log_id, order_id, start_time, end_time, operator_id, qty_produced, scrap_qty, machine_id, note "
                + "FROM mfg_execution_log WHERE (? IS NULL OR order_id=?) ORDER BY start_time DESC";
        List<ExecutionLogDTO> out = new ArrayList<>();
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, orderId == null || orderId.isEmpty() ? null : orderId);
            ps.setString(2, orderId == null || orderId.isEmpty() ? null : orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ExecutionLogDTO(
                            rs.getString("log_id"),
                            rs.getString("order_id"),
                            parseTs(rs.getString("start_time")),
                            parseTs(rs.getString("end_time")),
                            rs.getString("operator_id"),
                            rs.getDouble("qty_produced"),
                            rs.getDouble("scrap_qty"),
                            rs.getString("machine_id"),
                            rs.getString("note")
                    ));
                }
            }
            return out;
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/execution/logs", e.getMessage());
        }
    }

    @Override
    public QCCheckDTO insertQCCheck(QCCheckDTO dto) {
        String sql = "INSERT INTO mfg_quality_control (qc_check_id, order_id, inspection_date, sample_size, defects_count, pass_fail_status, operator_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        if (dto.getQcCheckId() == null || dto.getQcCheckId().trim().isEmpty()) {
            dto.setQcCheckId(nextId("mfg_quality_control", "qc_check_id", "QC-"));
        }
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dto.getQcCheckId());
            ps.setString(2, dto.getProductionOrderId());
            ps.setString(3, str(dto.getInspectionDate()));
            ps.setInt(4, dto.getSampleSize());
            ps.setInt(5, dto.getDefectsCount());
            ps.setInt(6, dto.isPassFailStatus() ? 1 : 0);
            ps.setString(7, dto.getInspectorId());
            ps.executeUpdate();
            return dto;
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/qc/submit", e.getMessage());
        }
    }

    @Override
    public int countQcBreaches(double threshold) {
        String sql = "SELECT COUNT(1) cnt FROM mfg_quality_control WHERE sample_size > 0 AND (CAST(defects_count AS REAL)/sample_size) > ?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/stats", e.getMessage());
        }
    }

    @Override
    public double availableStock(String materialItemId) {
        String sql = "SELECT available_qty FROM mfg_component_stock WHERE material_item_id=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, materialItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0;
                return rs.getDouble("available_qty");
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/stock", e.getMessage());
        }
    }

    @Override
    public void reserveStock(String materialItemId, double quantity) {
        String sql = "UPDATE mfg_component_stock SET available_qty = available_qty - ? WHERE material_item_id=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, quantity);
            ps.setString(2, materialItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw IntegrationException.sendFailed("mfg/stock", e.getMessage());
        }
    }

    @Override
    public String glAccount(String costCenterCode) {
        String sql = "SELECT gl_account FROM mfg_gl_account_map WHERE cost_center_code=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, costCenterCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("gl_account") : null;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/gl-map", e.getMessage());
        }
    }

    @Override
    public boolean hasAvailableOperator(String workCenterId) {
        String sql = "SELECT COUNT(1) cnt FROM mfg_work_center_operator WHERE work_center_id=? AND is_available=1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, workCenterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/assignment", e.getMessage());
        }
    }

    @Override
    public boolean isOperatorCertified(String operatorId, String workCenterId) {
        String sql = "SELECT certified FROM mfg_work_center_operator WHERE work_center_id=? AND operator_id=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, workCenterId);
            ps.setString(2, operatorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("certified") == 1;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/certification", e.getMessage());
        }
    }

    @Override
    public boolean isMachineOnline(String machineId) {
        if (machineId == null || machineId.trim().isEmpty()) {
            return true;
        }
        String sql = "SELECT active FROM mfg_machine_heartbeat WHERE machine_id=?";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, machineId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("active") == 1;
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/machine", e.getMessage());
        }
    }

    @Override
    public String nextId(String tableName, String idColumn, String prefix) {
        String sql = "SELECT " + idColumn + " FROM " + tableName + " WHERE " + idColumn + " LIKE ? ORDER BY " + idColumn + " DESC LIMIT 1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return prefix + "1001";
                }
                String last = rs.getString(1);
                if (last == null || last.length() <= prefix.length()) {
                    return prefix + "1001";
                }
                String n = last.substring(prefix.length()).replaceAll("[^0-9]", "");
                if (n.isEmpty()) {
                    return prefix + "1001";
                }
                return prefix + (Integer.parseInt(n) + 1);
            }
        } catch (SQLException e) {
            throw IntegrationException.fetchFailed("mfg/id", e.getMessage());
        }
    }

    private BigDecimal totalBomCost(Connection c, String bomId, String bomVersion) throws SQLException {
        String sql = "SELECT COALESCE(SUM(unit_cost * quantity), 0) total_cost FROM mfg_bom_item WHERE bom_id=? AND bom_version=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bomId);
            ps.setString(2, bomVersion);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return BigDecimal.ZERO;
                return BigDecimal.valueOf(rs.getDouble("total_cost"));
            }
        }
    }

    private CarModelDTO findCarByVin(String vin, Connection c) throws SQLException {
        String sql = "SELECT vin, model_name, chassis_type, build_status, assembly_line_id, started_at FROM mfg_car WHERE vin=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vin);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new CarModelDTO(
                        rs.getString("vin"),
                        rs.getString("model_name"),
                        rs.getString("chassis_type"),
                        rs.getString("build_status"),
                        rs.getString("assembly_line_id"),
                        parseTs(rs.getString("started_at"))
                );
            }
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    private static LocalDate parseDate(String s) {
        return s == null || s.trim().isEmpty() ? null : LocalDate.parse(s);
    }

    private static LocalDateTime parseTs(String s) {
        return s == null || s.trim().isEmpty() ? null : LocalDateTime.parse(s);
    }

    private static String str(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private static String str(LocalDateTime d) {
        return d == null ? null : d.toString();
    }
}
