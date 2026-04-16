package com.designx.erp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility to initialize the MySQL database schema.
 * Reads create_schema.sql and executes all statements.
 */
public class DBSchemaInitializer {

    public static void main(String[] args) {
        System.out.println("========== DATABASE SCHEMA INITIALIZATION ==========\n");
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[Init] MySQL JDBC driver loaded");

            // Connect to MySQL (without selecting a specific database initially)
            String url = "jdbc:mysql://localhost:3306/";
            String user = "root";
            String password = "huskywater120";
            
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("[Init] Connected to MySQL server");

            // Read and execute SQL script
            String sqlFile = "create_schema.sql";
            StringBuilder sqlScript = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    if (line.startsWith("--") || line.trim().isEmpty()) {
                        continue;
                    }
                    sqlScript.append(line).append("\n");
                }
            }

            // Split SQL statements and execute each
            String[] statements = sqlScript.toString().split(";");
            Statement stmt = conn.createStatement();
            
            int executed = 0;
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try {
                        stmt.execute(sql);
                        executed++;
                        System.out.println("[Init] ✓ Executed statement " + executed);
                    } catch (Exception e) {
                        System.err.println("[Init] ✗ Error executing: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                        System.err.println("       Error: " + e.getMessage());
                    }
                }
            }

            stmt.close();
            conn.close();

            System.out.println("\n[Init] Successfully executed " + executed + " SQL statements");
            System.out.println("[Init] Database 'polymorphs' initialized with all tables");
            System.out.println("\n========== INITIALIZATION COMPLETE ==========");
            System.out.println("✅ Ready to use with Order Processing system!");

        } catch (Exception e) {
            System.err.println("❌ INITIALIZATION FAILED");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
