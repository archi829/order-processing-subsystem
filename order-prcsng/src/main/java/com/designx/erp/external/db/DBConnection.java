package com.designx.erp.external.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MEMBER 1 - Database Integration Layer
 * ======================================
 * 
 * Responsibility: Manage JDBC connection to MySQL database
 * GRASP Principle: Low Coupling - abstraction for DB connectivity
 * SOLID Principle: S - Single Responsibility (connection management only)
 * 
 * Connects to polymorphs_db with credentials and URL constants.
 * Thread-safe singleton pattern for connection pooling.
 */
public class DBConnection {

    // Database Configuration Constants
    private static final String DB_URL = "jdbc:mysql://localhost:3306/polymorphs";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "huskywater120";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static DBConnection instance;
    private Connection connection;

    /**
     * Private constructor - prevent direct instantiation.
     */
    private DBConnection() {
    }

    /**
     * Lazy initialization singleton for DB connection.
     * Thread-safe getInstance() for multi-threaded environments.
     * 
     * @return singleton instance of DBConnection
     * @throws SQLException if connection fails
     */
    public static synchronized DBConnection getInstance() throws SQLException {
        if (instance == null) {
            instance = new DBConnection();
            instance.connect();
        }
        return instance;
    }

    /**
     * Establishes JDBC connection to MySQL database.
     * 
     * @throws SQLException if connection cannot be established
     */
    private void connect() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
            this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[DBConnection] Connected to " + DB_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
    }

    /**
     * Returns the active database connection.
     * 
     * @return Connection object for executing queries
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Checks if connection is active.
     * 
     * @return true if connection is valid and open
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Closes the database connection gracefully.
     * 
     * @throws SQLException if closing connection fails
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("[DBConnection] Connection closed");
        }
    }
}
