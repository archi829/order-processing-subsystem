-- =========================
-- DATABASE SCHEMA: POLYMORPHS ERP
-- =========================
-- Order Processing Subsystem with Sales Integration
-- Created for 4-member team implementation with design patterns
-- =========================

CREATE DATABASE IF NOT EXISTS polymorphs;
USE polymorphs;

-- =========================
-- SALES SUBSYSTEM TABLES (READ-ONLY)
-- =========================
-- These tables are managed by the Sales subsystem
-- Order Processing reads from these tables only

CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    region VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS leads (
    lead_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    company VARCHAR(100),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deals (
    deal_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    amount DOUBLE,
    stage VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

CREATE TABLE IF NOT EXISTS quotes (
    quote_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    deal_id INT,
    total_amount DOUBLE,
    discount DOUBLE,
    final_amount DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (deal_id) REFERENCES deals(deal_id)
);

CREATE TABLE IF NOT EXISTS quote_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    quote_id INT,
    product_name VARCHAR(100),
    quantity INT,
    price DOUBLE,
    FOREIGN KEY (quote_id) REFERENCES quotes(quote_id)
);

CREATE TABLE IF NOT EXISTS sales_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    month VARCHAR(20),
    revenue DOUBLE
);

CREATE TABLE IF NOT EXISTS reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    report_type VARCHAR(50),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    summary TEXT
);

-- =========================
-- ORDER PROCESSING SUBSYSTEM TABLES (READ + WRITE)
-- =========================
-- Tables owned by Order Processing subsystem
-- MEMBER 3 (Controller) orchestrates writes to these tables
-- MEMBER 4 (Tracking Service) reads from these tables

-- ORDERS: Core order lifecycle tracking
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    quote_id INT,
    customer_id INT,
    customer_name VARCHAR(100),
    contact_details VARCHAR(100),
    vehicle_model VARCHAR(100),
    vehicle_variant VARCHAR(100),
    vehicle_color VARCHAR(50),
    custom_features VARCHAR(255),
    order_value DOUBLE,
    order_date DATE,
    order_details VARCHAR(255),
    current_status VARCHAR(50),
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (quote_id) REFERENCES quotes(quote_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    INDEX (current_status),
    INDEX (customer_id)
);

-- ORDER_HISTORY: Audit trail for order status changes
CREATE TABLE IF NOT EXISTS order_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    status VARCHAR(50) NOT NULL,
    message VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX (order_id),
    INDEX (timestamp)
);

-- FULFILLMENT: Inventory allocation and shipment tracking
CREATE TABLE IF NOT EXISTS fulfillment (
    fulfillment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    allocation_id INT,
    shipment_id VARCHAR(50),
    dispatch_date DATE,
    delivery_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX (order_id)
);

-- BILLING: Invoice details and amounts
CREATE TABLE IF NOT EXISTS billing (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    base_amount DOUBLE,
    tax_amount DOUBLE,
    total_amount DOUBLE,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX (order_id)
);

-- PAYMENTS: Payment processing and transaction records
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT,
    order_id INT,
    payment_method VARCHAR(50),
    payment_status VARCHAR(50),
    transaction_details VARCHAR(255),
    amount DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (invoice_id) REFERENCES billing(invoice_id),
    INDEX (order_id),
    INDEX (payment_status)
);

-- ORDER_ANALYTICS: Performance metrics and reporting
CREATE TABLE IF NOT EXISTS order_analytics (
    analytics_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    vehicle_model VARCHAR(100),
    order_value DOUBLE,
    payment_method VARCHAR(50),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    INDEX (vehicle_model),
    INDEX (status)
);

-- =========================
-- SAMPLE DATA FOR TESTING
-- =========================

-- Insert sample customers (Sales subsystem)
INSERT INTO customers (name, email, phone, region) VALUES
    ('Ravi Kumar', 'ravi.kumar@example.com', '9876543210', 'South'),
    ('Priya Sharma', 'priya.sharma@example.com', '9876543211', 'West'),
    ('Arjun Nair', 'arjun.nair@example.com', '9876543212', 'North');

-- Insert sample deals (Sales subsystem)
INSERT INTO deals (customer_id, amount, stage, status) VALUES
    (1, 1450000.0, 'Negotiation', 'Active'),
    (2, 3200000.0, 'Proposal', 'Active'),
    (3, 900000.0, 'Prospecting', 'Active');

-- Insert sample quotes (Sales subsystem)
INSERT INTO quotes (customer_id, deal_id, total_amount, discount, final_amount) VALUES
    (1, 1, 1500000.0, 50000.0, 1450000.0),
    (2, 2, 3300000.0, 100000.0, 3200000.0),
    (3, 3, 950000.0, 50000.0, 900000.0);

-- Insert sample quote items (Sales subsystem)
INSERT INTO quote_items (quote_id, product_name, quantity, price) VALUES
    (1, 'Tata Nexon XZ+', 1, 1450000.0),
    (2, 'Tata Harrier XZA', 1, 3200000.0),
    (3, 'Tata Altroz XZ', 1, 900000.0);

-- =========================
-- INDEXES FOR PERFORMANCE
-- =========================
-- Already created on foreign key and frequently queried columns

-- =========================
-- VALIDATION NOTES
-- =========================
-- 1. total_amount and order_value must be >= 0
-- 2. discount must be <= 50% of total_amount
-- 3. customer_id must exist in customers table
-- 4. All dates must be valid YYYY-MM-DD format
-- 5. Enum values for status must match OrderStatus enum:
--    CAPTURED, VALIDATED, APPROVED, REJECTED, ALLOCATED, 
--    DISPATCHED, INVOICED, PAYMENT_PENDING, PAYMENT_SUCCESS, FAILED, CANCELLED

-- =========================
-- REFERENTIAL INTEGRITY RULES
-- =========================
-- Sales Tables (READ-ONLY):
--   customers <- leads, deals, quotes
--   deals <- quotes
--   quotes <- quote_items, orders
-- 
-- Order Processing Tables (READ+WRITE):
--   orders <- fulfillment, billing, payments, order_history, order_analytics
--   billing <- payments
--   orders (can read) <- quotes, customers (from Sales subsystem)

-- =========================
-- SUBSYSTEM BOUNDARIES
-- =========================
-- Sales Subsystem manages: customers, leads, deals, quotes, quote_items, sales_data, reports
-- Order Processing manages: orders, order_history, fulfillment, billing, payments, order_analytics
-- 
-- MEMBER 1 (DB Integration): READ access to quotes, quote_items, customers
-- MEMBER 2 (Lifecycle): Writes to order_history table
-- MEMBER 3 (Controller): Writes to orders, fulfillment, billing, payments, order_analytics
-- MEMBER 4 (Tracking): READ access to orders, order_history, order_analytics
