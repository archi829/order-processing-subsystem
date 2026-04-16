PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS mfg_quality_control;
DROP TABLE IF EXISTS mfg_execution_log;
DROP TABLE IF EXISTS mfg_machine_heartbeat;
DROP TABLE IF EXISTS mfg_work_center_operator;
DROP TABLE IF EXISTS mfg_gl_account_map;
DROP TABLE IF EXISTS mfg_routing_step;
DROP TABLE IF EXISTS mfg_work_center;
DROP TABLE IF EXISTS mfg_production_order;
DROP TABLE IF EXISTS mfg_component_stock;
DROP TABLE IF EXISTS mfg_bom_item;
DROP TABLE IF EXISTS mfg_bom;
DROP TABLE IF EXISTS mfg_material_master;
DROP TABLE IF EXISTS mfg_car;

CREATE TABLE mfg_car (
    vin TEXT PRIMARY KEY,
    model_name TEXT NOT NULL,
    chassis_type TEXT NOT NULL,
    build_status TEXT NOT NULL,
    assembly_line_id TEXT NOT NULL,
    started_at TEXT NOT NULL
);

CREATE TABLE mfg_bom (
    bom_id TEXT NOT NULL,
    bom_version TEXT NOT NULL,
    is_active INTEGER NOT NULL CHECK (is_active IN (0, 1)),
    product_id TEXT NOT NULL,
    product_name TEXT NOT NULL,
    budget_limit REAL NOT NULL,
    PRIMARY KEY (bom_id, bom_version)
);

CREATE TABLE mfg_bom_item (
    bom_id TEXT NOT NULL,
    bom_version TEXT NOT NULL,
    material_item_id TEXT NOT NULL,
    part_name TEXT NOT NULL,
    unit_cost REAL NOT NULL,
    quantity REAL NOT NULL,
    PRIMARY KEY (bom_id, bom_version, material_item_id),
    FOREIGN KEY (bom_id, bom_version) REFERENCES mfg_bom (bom_id, bom_version)
);

CREATE TABLE mfg_material_master (
    material_item_id TEXT PRIMARY KEY,
    part_name TEXT NOT NULL,
    unit_cost REAL NOT NULL
);

CREATE TABLE mfg_component_stock (
    material_item_id TEXT PRIMARY KEY,
    available_qty REAL NOT NULL
);

CREATE TABLE mfg_production_order (
    order_id TEXT PRIMARY KEY,
    order_date TEXT NOT NULL,
    planned_start_date TEXT NOT NULL,
    planned_end_date TEXT NOT NULL,
    status TEXT NOT NULL,
    product_id TEXT NOT NULL,
    product_name TEXT NOT NULL,
    priority TEXT NOT NULL,
    actual_start_date TEXT,
    actual_end_date TEXT,
    bom_id TEXT NOT NULL,
    qty_planned INTEGER NOT NULL,
    qty_produced INTEGER NOT NULL DEFAULT 0,
    scrap_qty INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE mfg_work_center (
    wc_id TEXT PRIMARY KEY,
    wc_name TEXT NOT NULL,
    wc_type TEXT NOT NULL,
    capacity_hours REAL NOT NULL,
    utilization_pct REAL NOT NULL,
    location TEXT NOT NULL
);

CREATE TABLE mfg_routing_step (
    routing_id TEXT NOT NULL,
    product_id TEXT NOT NULL,
    operation_id TEXT NOT NULL,
    sequence_number INTEGER NOT NULL,
    operation_name TEXT NOT NULL,
    work_center_id TEXT NOT NULL,
    setup_time REAL NOT NULL,
    run_time REAL NOT NULL,
    PRIMARY KEY (routing_id, operation_id),
    FOREIGN KEY (work_center_id) REFERENCES mfg_work_center (wc_id)
);

CREATE TABLE mfg_gl_account_map (
    cost_center_code TEXT PRIMARY KEY,
    gl_account TEXT NOT NULL
);

CREATE TABLE mfg_work_center_operator (
    operator_id TEXT NOT NULL,
    work_center_id TEXT NOT NULL,
    certified INTEGER NOT NULL CHECK (certified IN (0, 1)),
    is_available INTEGER NOT NULL CHECK (is_available IN (0, 1)),
    PRIMARY KEY (operator_id, work_center_id),
    FOREIGN KEY (work_center_id) REFERENCES mfg_work_center (wc_id)
);

CREATE TABLE mfg_machine_heartbeat (
    machine_id TEXT PRIMARY KEY,
    active INTEGER NOT NULL CHECK (active IN (0, 1)),
    last_seen TEXT NOT NULL
);

CREATE TABLE mfg_execution_log (
    log_id TEXT PRIMARY KEY,
    order_id TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT,
    operator_id TEXT NOT NULL,
    qty_produced REAL NOT NULL,
    scrap_qty REAL NOT NULL,
    machine_id TEXT,
    note TEXT,
    FOREIGN KEY (order_id) REFERENCES mfg_production_order (order_id)
);

CREATE TABLE mfg_quality_control (
    qc_check_id TEXT PRIMARY KEY,
    order_id TEXT NOT NULL,
    inspection_date TEXT NOT NULL,
    sample_size INTEGER NOT NULL,
    defects_count INTEGER NOT NULL,
    pass_fail_status INTEGER NOT NULL CHECK (pass_fail_status IN (0, 1)),
    operator_id TEXT NOT NULL,
    FOREIGN KEY (order_id) REFERENCES mfg_production_order (order_id)
);

INSERT INTO mfg_car (vin, model_name, chassis_type, build_status, assembly_line_id, started_at) VALUES
('TML-2001', 'Nexon', 'Steel A1', 'IN_ASSEMBLY', 'Pune Line-A', '2026-04-10T08:00:00'),
('TML-2002', 'Harrier', 'Alloy B2', 'IN_QUALITY', 'Pune Line-B', '2026-04-10T06:30:00'),
('TML-2003', 'Safari', 'Reinforced', 'PENDING', 'Sanand Line-C', '2026-04-11T07:15:00'),
('TML-2004', 'Nexon EV', 'Aluminum', 'IN_ASSEMBLY', 'Sanand Line-C', '2026-04-11T09:45:00');

INSERT INTO mfg_bom (bom_id, bom_version, is_active, product_id, product_name, budget_limit) VALUES
('BOM-01', 'v1.0', 1, 'PRD-NEXON', 'Nexon', 450000),
('BOM-02', 'v2.1', 1, 'PRD-HARRIER', 'Harrier', 650000),
('BOM-03', 'v2.0', 1, 'PRD-SAFARI', 'Safari', 760000),
('BOM-05', 'v1.0', 1, 'PRD-NEXON-EV', 'Nexon EV', 910000);

INSERT INTO mfg_bom_item (bom_id, bom_version, material_item_id, part_name, unit_cost, quantity) VALUES
('BOM-01', 'v1.0', 'PART-ENG-1', '1.2L Revotron Engine', 180000, 1),
('BOM-01', 'v1.0', 'PART-CHA-1', 'Steel A1 Chassis', 90000, 1),
('BOM-01', 'v1.0', 'PART-WHL-1', 'Alloy Wheel Set', 12000, 4),
('BOM-01', 'v1.0', 'PART-BAT-1', '48V Battery', 35000, 1),
('BOM-02', 'v2.1', 'PART-ENG-2', '2.0L Kryotec Engine', 260000, 1),
('BOM-02', 'v2.1', 'PART-CHA-2', 'Alloy B2 Chassis', 140000, 1),
('BOM-02', 'v2.1', 'PART-WHL-2', 'Premium Alloy Wheels', 18000, 4),
('BOM-03', 'v2.0', 'PART-ENG-2', '2.0L Kryotec Engine', 260000, 1),
('BOM-03', 'v2.0', 'PART-CHA-3', 'Reinforced Chassis', 180000, 1),
('BOM-05', 'v1.0', 'PART-BAT-EV', '40 kWh Li-Ion Pack', 520000, 1),
('BOM-05', 'v1.0', 'PART-MOT-EV', 'Permanent Magnet Motor', 180000, 1),
('BOM-05', 'v1.0', 'PART-CHA-4', 'Aluminum Chassis', 110000, 1);

INSERT INTO mfg_material_master (material_item_id, part_name, unit_cost) VALUES
('PART-ENG-1', '1.2L Revotron Engine', 180000),
('PART-CHA-1', 'Steel A1 Chassis', 90000),
('PART-WHL-1', 'Alloy Wheel Set', 12000),
('PART-BAT-1', '48V Battery', 35000),
('PART-ENG-2', '2.0L Kryotec Engine', 260000),
('PART-CHA-2', 'Alloy B2 Chassis', 140000),
('PART-WHL-2', 'Premium Alloy Wheels', 18000),
('PART-CHA-3', 'Reinforced Chassis', 180000),
('PART-BAT-EV', '40 kWh Li-Ion Pack', 520000),
('PART-MOT-EV', 'Permanent Magnet Motor', 180000),
('PART-CHA-4', 'Aluminum Chassis', 110000);

INSERT INTO mfg_component_stock (material_item_id, available_qty) VALUES
('PART-ENG-1', 120),
('PART-CHA-1', 180),
('PART-WHL-1', 400),
('PART-BAT-1', 100),
('PART-ENG-2', 75),
('PART-CHA-2', 70),
('PART-WHL-2', 290),
('PART-CHA-3', 40),
('PART-BAT-EV', 25),
('PART-MOT-EV', 45),
('PART-CHA-4', 120);

INSERT INTO mfg_work_center (wc_id, wc_name, wc_type, capacity_hours, utilization_pct, location) VALUES
('WC-01', 'Body Weld Station', 'WELDING', 160, 82, 'Pune Line-A'),
('WC-02', 'Paint Booth 1', 'PAINT', 160, 75, 'Pune Line-A'),
('WC-03', 'Final Assembly A', 'ASSEMBLY', 160, 95, 'Pune Line-B'),
('WC-04', 'Quality Gate 1', 'TESTING', 120, 68, 'Pune Line-B'),
('WC-05', 'EV Battery Install', 'ASSEMBLY', 160, 88, 'Sanand Line-C'),
('WC-06', 'Packaging and PDI', 'PACKAGING', 160, 60, 'Jamshedpur Line-D');

INSERT INTO mfg_routing_step (routing_id, product_id, operation_id, sequence_number, operation_name, work_center_id, setup_time, run_time) VALUES
('RT-01', 'PRD-NEXON', 'OP-101', 1, 'Weld Chassis', 'WC-01', 0.5, 2.0),
('RT-01', 'PRD-NEXON', 'OP-102', 2, 'Paint Body', 'WC-02', 0.3, 1.5),
('RT-01', 'PRD-NEXON', 'OP-103', 3, 'Final Assembly', 'WC-03', 0.6, 3.2),
('RT-01', 'PRD-NEXON', 'OP-104', 4, 'Quality Gate', 'WC-04', 0.2, 0.8),
('RT-02', 'PRD-NEXON-EV', 'OP-201', 1, 'Install Battery', 'WC-05', 0.7, 2.4),
('RT-02', 'PRD-NEXON-EV', 'OP-202', 2, 'Wire Harness', 'WC-03', 0.4, 1.6),
('RT-02', 'PRD-NEXON-EV', 'OP-204', 4, 'Packaging and PDI', 'WC-06', 0.3, 0.6);

INSERT INTO mfg_gl_account_map (cost_center_code, gl_account) VALUES
('WC-01', '500100'),
('WC-02', '500200'),
('WC-03', '500300'),
('WC-04', '500400'),
('WC-05', '500500'),
('WC-06', '500600');

INSERT INTO mfg_work_center_operator (operator_id, work_center_id, certified, is_available) VALUES
('EMP-001', 'WC-01', 1, 1),
('EMP-002', 'WC-02', 1, 1),
('EMP-003', 'WC-03', 1, 1),
('EMP-004', 'WC-04', 1, 1),
('EMP-005', 'WC-05', 1, 1),
('EMP-006', 'WC-06', 1, 1),
('INSP-01', 'WC-04', 1, 1);

INSERT INTO mfg_machine_heartbeat (machine_id, active, last_seen) VALUES
('MC-01', 1, '2026-04-12T10:00:00'),
('MC-02', 1, '2026-04-12T10:02:00'),
('MC-03', 1, '2026-04-12T10:03:00');

INSERT INTO mfg_production_order (order_id, order_date, planned_start_date, planned_end_date, status, product_id, product_name, priority, actual_start_date, actual_end_date, bom_id, qty_planned, qty_produced, scrap_qty) VALUES
('PO-1001', '2026-04-08', '2026-04-09', '2026-04-20', 'IN_PROGRESS', 'PRD-NEXON', 'Nexon', 'HIGH', '2026-04-09', NULL, 'BOM-01', 50, 22, 1),
('PO-1002', '2026-04-09', '2026-04-11', '2026-04-24', 'PENDING', 'PRD-HARRIER', 'Harrier', 'MEDIUM', NULL, NULL, 'BOM-02', 30, 0, 0),
('PO-1003', '2026-04-10', '2026-04-12', '2026-04-26', 'IN_PROGRESS', 'PRD-NEXON-EV', 'Nexon EV', 'URGENT', '2026-04-12', NULL, 'BOM-05', 15, 6, 0),
('PO-1004', '2026-04-01', '2026-04-02', '2026-04-14', 'COMPLETED', 'PRD-NEXON', 'Nexon', 'LOW', '2026-04-02', '2026-04-14', 'BOM-01', 20, 20, 1);

INSERT INTO mfg_execution_log (log_id, order_id, start_time, end_time, operator_id, qty_produced, scrap_qty, machine_id, note) VALUES
('LOG-1001', 'PO-1001', '2026-04-11T08:00:00', '2026-04-11T16:00:00', 'EMP-001', 8, 1, 'MC-01', 'Normal line run'),
('LOG-1002', 'PO-1003', '2026-04-11T09:00:00', '2026-04-11T15:00:00', 'EMP-005', 4, 0, 'MC-02', 'Battery line run');

INSERT INTO mfg_quality_control (qc_check_id, order_id, inspection_date, sample_size, defects_count, pass_fail_status, operator_id) VALUES
('QC-1001', 'PO-1001', '2026-04-11', 80, 3, 1, 'INSP-01'),
('QC-1002', 'PO-1003', '2026-04-11', 50, 4, 0, 'INSP-01');
