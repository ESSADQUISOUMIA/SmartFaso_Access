package com.example.facialrecognition.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.io.File;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:facial_recognition.db";
    private static final String DB_FILE = "facial_recognition.db";

    public static void initializeDatabase() {
        File dbFile = new File(DB_FILE);
        boolean dbExists = dbFile.exists();

        try (Connection conn = getConnection()) {
            if (conn != null) {
                DatabaseMetaData metaData = conn.getMetaData();
                System.out.println("Driver name: " + metaData.getDriverName());
                System.out.println("Driver version: " + metaData.getDriverVersion());

                if (!dbExists) { // Only create tables if the database didn't exist
                    System.out.println("Creating new database tables.");
                    createTables(conn);
                } else {
                    System.out.println("Database already exists, checking table structure.");
                    verifyAndUpdateTables(conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL UNIQUE," +
                "status TEXT NOT NULL, " +
                "biometric_data BLOB," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                ");";
        stmt.execute(createUserTableSQL);

        String createAccessLogTableSQL = "CREATE TABLE IF NOT EXISTS access_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "timestamp TEXT DEFAULT CURRENT_TIMESTAMP," +
                "user_id INTEGER," +
                "status TEXT NOT NULL," +
                "face_vector BLOB," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ");";
        stmt.execute(createAccessLogTableSQL);

        System.out.println("Database initialized successfully.");
    }

    private static void verifyAndUpdateTables(Connection conn) throws SQLException {
        // Vérifier si la colonne 'status' existe dans la table 'users'
        boolean statusColumnExists = false;
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, "users", "status")) {
            statusColumnExists = rs.next();
        }

        // Si la colonne n'existe pas, l'ajouter
        if (!statusColumnExists) {
            System.out.println("Column 'status' does not exist in table 'users'. Adding it now.");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'active'");
                System.out.println("Column 'status' added successfully.");
            }
        } else {
            System.out.println("Table structure is up to date.");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(DB_URL);
            // Activer l'application des clés étrangères pour cette connexion
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Pilote JDBC SQLite non trouvé", e);
        }
    }
}