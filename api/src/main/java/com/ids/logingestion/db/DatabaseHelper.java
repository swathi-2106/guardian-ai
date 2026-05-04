package com.ids.logingestion.db;

import com.ids.logingestion.LogEntry;
import com.ids.logingestion.detection.Alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:logsystem.db";

    public Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC"); // 🔥 IMPORTANT
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found", e);
        }
        return DriverManager.getConnection(DB_URL);
    }

    public void initializeDatabase() {

        String createLogsTable = """
                CREATE TABLE IF NOT EXISTS logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    ip_address TEXT NOT NULL,
                    event_type TEXT NOT NULL,
                    description TEXT
                );
                """;

        String createAlertsTable = """
                CREATE TABLE IF NOT EXISTS alerts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT,
                    severity TEXT,
                    message TEXT,
                    source_type TEXT,
                    data_source TEXT
                );
                """;

        String createIncidentsTable = """
                CREATE TABLE IF NOT EXISTS incidents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    attack_type TEXT,
                    confidence REAL
                );
                """;

        String createIndexes = """
                CREATE INDEX IF NOT EXISTS idx_logs_time ON logs(timestamp);
                CREATE INDEX IF NOT EXISTS idx_logs_ip ON logs(ip_address);
                CREATE INDEX IF NOT EXISTS idx_logs_event ON logs(event_type);
                """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createLogsTable);
            stmt.execute(createAlertsTable);
            ensureColumn(conn, "alerts", "source_type", "TEXT");
            ensureColumn(conn, "alerts", "data_source", "TEXT");
            stmt.execute(createIncidentsTable);
            stmt.execute(createIndexes);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

    public void insertAlert(Alert alert) {

        String sql = "INSERT INTO alerts(timestamp, severity, message, source_type, data_source) VALUES(?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alert.getTimestamp());
            pstmt.setString(2, alert.getSeverity());
            pstmt.setString(3, alert.getMessage());
            pstmt.setString(4, alert.getDataMode());
            pstmt.setString(5, alert.getDataSource());
            pstmt.executeUpdate();

            System.out.println("Alert inserted successfully.");

        } catch (SQLException e) {
            System.err.println("Alert insert error: " + e.getMessage());
        }
    }

    public void insertLogs(List<LogEntry> logs) {

        String sql = "INSERT INTO logs(timestamp, ip_address, event_type, description) VALUES(?,?,?,?)";

        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (com.ids.logingestion.LogEntry log : logs) {
                pstmt.setString(1, log.getTimestamp());
                pstmt.setString(2, log.getIpAddress());
                pstmt.setString(3, log.getEventType());
                pstmt.setString(4, log.getDescription());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("Logs inserted successfully.");

        } catch (SQLException e) {
            System.err.println("Insert error: " + e.getMessage());
        }
    }

    public List<com.ids.logingestion.LogEntry> getLogsByIP(String ip) {

        List<com.ids.logingestion.LogEntry> logs = new ArrayList<>();

        String sql = "SELECT * FROM logs WHERE ip_address = ?";

        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);

            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(new com.ids.logingestion.LogEntry(
                        rs.getString("timestamp"),
                        rs.getString("ip_address"),
                        rs.getString("event_type"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }

        return logs;
    }

    public List<com.ids.logingestion.LogEntry> getLogsByEventType(String type) {

        List<com.ids.logingestion.LogEntry> logs = new ArrayList<>();

        String sql = "SELECT * FROM logs WHERE event_type = ?";

        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);

            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(new com.ids.logingestion.LogEntry(
                        rs.getString("timestamp"),
                        rs.getString("ip_address"),
                        rs.getString("event_type"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }

        return logs;
    }

    public List<com.ids.logingestion.LogEntry> getLogsByTimeRange(String start, String end) {

        List<com.ids.logingestion.LogEntry> logs = new ArrayList<>();

        String sql = "SELECT * FROM logs WHERE timestamp BETWEEN ? AND ?";

        try (Connection conn = connect();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, start);
            pstmt.setString(2, end);

            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(new com.ids.logingestion.LogEntry(
                        rs.getString("timestamp"),
                        rs.getString("ip_address"),
                        rs.getString("event_type"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }

        return logs;
    }

    public List<com.ids.logingestion.LogEntry> fetchAllLogs() {

        List<com.ids.logingestion.LogEntry> logs = new ArrayList<>();

        String sql = "SELECT * FROM logs";

        try (Connection conn = connect();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(new com.ids.logingestion.LogEntry(
                        rs.getString("timestamp"),
                        rs.getString("ip_address"),
                        rs.getString("event_type"),
                        rs.getString("description")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Fetch error: " + e.getMessage());
        }

        return logs;
    }

    private void ensureColumn(Connection conn, String tableName, String columnName, String columnType) throws SQLException {
        String pragmaSql = "PRAGMA table_info(" + tableName + ")";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(pragmaSql)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        }
    }


    public List<Alert> fetchAllAlerts() {

        List<Alert> alerts = new ArrayList<>();

        String sql = "SELECT * FROM alerts ORDER BY timestamp DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                alerts.add(new Alert(
                        rs.getString("timestamp"),
                        rs.getString("severity"),
                        rs.getString("message"),
                        List.of(),
                        rs.getString("source_type"),
                        rs.getString("data_source")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Fetch alerts error: " + e.getMessage());
        }

        return alerts;
    }

}
