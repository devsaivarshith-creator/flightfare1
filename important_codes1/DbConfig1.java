package com.flightfare.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages JDBC connection to MySQL and database schema initialization.
 */
public class DbConfig {

    private static String url = "jdbc:mysql://localhost:3306/flight_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "root";

    static {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DbConfig] Warning: MySQL JDBC Driver class not found in standard loader: " + e.getMessage());
        }
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = DbConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                File f = new File("db.properties");
                if (f.exists()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        props.load(fis);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[DbConfig] Using default DB settings: " + e.getMessage());
        }

        if (props.getProperty("db.url") != null) {
            url = props.getProperty("db.url");
        }
        if (props.getProperty("db.user") != null) {
            user = props.getProperty("db.user");
        }
        if (props.getProperty("db.password") != null) {
            password = props.getProperty("db.password");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static String getDbUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    /**
     * Initializes the MySQL schema and required tables if they don't already exist.
     */
    public static boolean initDatabase() {
        String sqlFlights = "CREATE TABLE IF NOT EXISTS flights (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "flight_number VARCHAR(30) NOT NULL, " +
                "airline VARCHAR(100) NOT NULL, " +
                "origin_code VARCHAR(10) NOT NULL, " +
                "origin_airport VARCHAR(150), " +
                "destination_code VARCHAR(10) NOT NULL, " +
                "destination_airport VARCHAR(150), " +
                "departure_time VARCHAR(50), " +
                "arrival_time VARCHAR(50), " +
                "duration VARCHAR(30), " +
                "stops INT DEFAULT 0, " +
                "cabin_class VARCHAR(50) DEFAULT 'Economy', " +
                "price DECIMAL(10,2) NOT NULL, " +
                "currency VARCHAR(10) DEFAULT 'USD', " +
                "data_source VARCHAR(50), " +
                "status VARCHAR(50) DEFAULT 'Scheduled', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String sqlComparisons = "CREATE TABLE IF NOT EXISTS flight_comparisons (" +
                "comparison_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "route VARCHAR(50) NOT NULL, " +
                "flight1_number VARCHAR(30) NOT NULL, " +
                "flight1_airline VARCHAR(100), " +
                "flight1_price DECIMAL(10,2), " +
                "flight2_number VARCHAR(30) NOT NULL, " +
                "flight2_airline VARCHAR(100), " +
                "flight2_price DECIMAL(10,2), " +
                "price_difference DECIMAL(10,2), " +
                "cheaper_flight VARCHAR(255), " +
                "duration_difference VARCHAR(255), " +
                "verdict TEXT, " +
                "compared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String sqlPriceHistory = "CREATE TABLE IF NOT EXISTS price_history (" +
                "history_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "token_batch INT DEFAULT 1, " +
                "flight_number VARCHAR(30) NOT NULL, " +
                "airline VARCHAR(100) NOT NULL, " +
                "route VARCHAR(50) NOT NULL, " +
                "old_price DECIMAL(10,2) NOT NULL, " +
                "new_price DECIMAL(10,2) NOT NULL, " +
                "price_change DECIMAL(10,2) NOT NULL, " +
                "percent_change DECIMAL(6,2) NOT NULL, " +
                "change_type VARCHAR(30) NOT NULL, " +
                "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "source VARCHAR(100) DEFAULT 'Live API'" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlFlights);
            stmt.execute(sqlComparisons);
            stmt.execute(sqlPriceHistory);
            try {
                stmt.execute("ALTER TABLE price_history ADD COLUMN token_batch INT DEFAULT 1 AFTER history_id;");
            } catch (SQLException ignored) {}
            System.out.println("[DbConfig] Database schema initialized successfully in MySQL (flight_db).");
            return true;
        } catch (SQLException e) {
            System.err.println("[DbConfig] Error initializing MySQL database: " + e.getMessage());
            return false;
        }
    }
}
