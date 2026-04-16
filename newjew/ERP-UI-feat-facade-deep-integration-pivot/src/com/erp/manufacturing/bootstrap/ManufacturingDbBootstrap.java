package com.erp.manufacturing.bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the demo SQL database from a .sql script.
 */
public final class ManufacturingDbBootstrap {

    private ManufacturingDbBootstrap() {}

    public static void main(String[] args) throws Exception {
        String db = args.length > 0 ? args[0] : "data/mfg_demo.db";
        String script = args.length > 1 ? args[1] : "sql/manufacturing_demo.sql";

        String jdbc = db.startsWith("jdbc:") ? db : "jdbc:sqlite:" + db;
        ensureParentDirectory(db);

        String sql = readSql(script);
        runScript(jdbc, sql);

        System.out.println("Manufacturing demo DB initialized at " + db);
    }

    private static String readSql(String scriptPath) throws IOException {
        Path path = Paths.get(scriptPath);
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static void runScript(String jdbcUrl, String sqlScript) throws SQLException {
        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement st = c.createStatement()) {
            c.setAutoCommit(false);
            for (String statement : splitStatements(sqlScript)) {
                if (statement.trim().isEmpty()) continue;
                st.execute(statement);
            }
            c.commit();
        }
    }

    private static void ensureParentDirectory(String dbPath) throws IOException {
        if (dbPath.startsWith("jdbc:")) return;
        Path p = Paths.get(dbPath).toAbsolutePath().normalize();
        Path parent = p.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    private static String[] splitStatements(String script) {
        StringBuilder current = new StringBuilder();
        java.util.List<String> statements = new java.util.ArrayList<>();
        boolean inSingleQuote = false;

        for (int i = 0; i < script.length(); i++) {
            char ch = script.charAt(i);
            if (ch == '\'' && (i == 0 || script.charAt(i - 1) != '\\')) {
                inSingleQuote = !inSingleQuote;
            }
            if (ch == ';' && !inSingleQuote) {
                statements.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements.toArray(new String[0]);
    }
}
