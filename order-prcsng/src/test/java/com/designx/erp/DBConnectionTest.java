package com.designx.erp;

import com.designx.erp.external.db.DBConnection;
import java.sql.Connection;

/**
 * Quick test to verify MySQL database connection with actual credentials.
 */
public class DBConnectionTest {

    public static void main(String[] args) {
        System.out.println("========== DATABASE CONNECTION TEST ==========\n");
        
        try {
            System.out.println("[Test] Attempting to connect to MySQL database...");
            System.out.println("[Test] URL: jdbc:mysql://localhost:3306/polymorphs_db");
            System.out.println("[Test] User: root");
            System.out.println("[Test] Password: ******* (hidden)");
            System.out.println();

            // Get singleton connection
            DBConnection dbConn = DBConnection.getInstance();
            Connection conn = dbConn.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ SUCCESS: Connected to MySQL database!");
                System.out.println("[Test] Connection object: " + conn);
                System.out.println("[Test] Is valid: " + conn.isValid(2));
                
                // Try a simple metadata query
                System.out.println("[Test] Database product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("[Test] Database version: " + conn.getMetaData().getDatabaseProductVersion());
                
                System.out.println("\n========== TEST PASSED ==========");
                System.out.println("Your database credentials are working correctly!");
            } else {
                System.out.println("❌ FAILED: Connection is null or closed");
            }

        } catch (Exception e) {
            System.out.println("❌ FAILED: Could not connect to database");
            System.out.println("\nError Details:");
            System.out.println("  Type: " + e.getClass().getSimpleName());
            System.out.println("  Message: " + e.getMessage());
            
            System.out.println("\nPossible Issues:");
            System.out.println("  1. MySQL server not running on localhost:3306");
            System.out.println("  2. Database 'polymorphs_db' does not exist");
            System.out.println("  3. Username 'root' or password incorrect");
            System.out.println("  4. MySQL JDBC driver not available");
            
            e.printStackTrace();
            System.out.println("\n========== TEST FAILED ==========");
        }
    }
}
