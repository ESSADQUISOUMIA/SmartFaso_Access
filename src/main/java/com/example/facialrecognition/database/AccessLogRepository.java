package com.example.facialrecognition.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.facialrecognition.model.AccessLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AccessLogRepository {

    /**
     * Enregistre un accès dans la table des journaux
     *
     * @param userId ID de l'utilisateur (null si non reconnu)
     * @param status GRANTED ou DENIED
     * @param faceVector Données biométriques du visage capturé (pour les accès refusés)
     * @return ID du journal créé
     * @throws SQLException en cas d'erreur de base de données
     */
    public static int logAccess(Integer userId, String status, byte[] faceVector) throws SQLException {
        String sql = "INSERT INTO access_logs (user_id, status, face_vector) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // userId peut être null pour un accès refusé
            if (userId != null) {
                pstmt.setInt(1, userId);
            } else {
                pstmt.setNull(1, java.sql.Types.INTEGER);
            }

            pstmt.setString(2, status);

            // faceVector peut être null pour un accès autorisé
            if (faceVector != null) {
                pstmt.setBytes(3, faceVector);
            } else {
                pstmt.setNull(3, java.sql.Types.BLOB);
            }

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("L'enregistrement du log d'accès a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("L'enregistrement du log d'accès a échoué, aucun ID obtenu.");
                }
            }
        }
    }

    /**
     * Récupère tous les journaux d'accès
     *
     * @return Liste de journaux d'accès
     * @throws SQLException en cas d'erreur de base de données
     */
    public static List<AccessLog> getAllAccessLogs() throws SQLException {
        String sql = "SELECT al.id, al.timestamp, al.status, al.user_id, u.name " +
                "FROM access_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.timestamp DESC";

        List<AccessLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String timestamp = rs.getString("timestamp");
                String status = rs.getString("status");
                Integer userId = rs.getInt("user_id");
                if (rs.wasNull()) {
                    userId = null;
                }
                String userName = rs.getString("name");

                AccessLog log = new AccessLog(id, timestamp, status, userId, userName);
                logs.add(log);
            }
        }

        return logs;
    }

    /**
     * Récupère les journaux d'accès récents (limités à un certain nombre)
     *
     * @param limit Nombre maximum de journaux à récupérer
     * @return Liste limitée de journaux d'accès
     * @throws SQLException en cas d'erreur de base de données
     */
    public static List<AccessLog> getRecentAccessLogs(int limit) throws SQLException {
        String sql = "SELECT al.id, al.timestamp, al.status, al.user_id, u.name " +
                "FROM access_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.timestamp DESC " +
                "LIMIT ?";

        List<AccessLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String timestamp = rs.getString("timestamp");
                    String status = rs.getString("status");
                    Integer userId = rs.getInt("user_id");
                    if (rs.wasNull()) {
                        userId = null;
                    }
                    String userName = rs.getString("name");

                    AccessLog log = new AccessLog(id, timestamp, status, userId, userName);
                    logs.add(log);
                }
            }
        }

        return logs;
    }

    /**
     * Récupère les journaux d'accès pour un utilisateur spécifique
     *
     * @param userId ID de l'utilisateur
     * @return Liste de journaux d'accès pour l'utilisateur
     * @throws SQLException en cas d'erreur de base de données
     */
    public static List<AccessLog> getAccessLogsByUser(int userId) throws SQLException {
        String sql = "SELECT al.id, al.timestamp, al.status, al.user_id, u.name " +
                "FROM access_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE al.user_id = ? " +
                "ORDER BY al.timestamp DESC";

        List<AccessLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String timestamp = rs.getString("timestamp");
                    String status = rs.getString("status");
                    String userName = rs.getString("name");

                    AccessLog log = new AccessLog(id, timestamp, status, userId, userName);
                    logs.add(log);
                }
            }
        }

        return logs;
    }

    /**
     * Convertit une liste de journaux d'accès en liste observable pour JavaFX
     * @param logs Liste de journaux d'accès
     * @return Liste observable
     */
    public static ObservableList<AccessLog> toObservableList(List<AccessLog> logs) {
        return FXCollections.observableArrayList(logs);
    }

    /**
     * Récupère les journaux d'accès dans la période spécifiée
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @return Liste observable de journaux d'accès
     * @throws SQLException En cas d'erreur de base de données
     */
    public static ObservableList<AccessLog> getAccessLogs(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT al.id, al.timestamp, al.status, al.user_id, u.name " +
                "FROM access_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE DATE(al.timestamp) BETWEEN ? AND ? " +
                "ORDER BY al.timestamp DESC";

        List<AccessLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String timestamp = rs.getString("timestamp");
                    String status = rs.getString("status");
                    Integer userId = rs.getInt("user_id");
                    if (rs.wasNull()) {
                        userId = null;
                    }
                    String userName = rs.getString("name");
                    if (userName == null) {
                        userName = "Inconnu";
                    }

                    AccessLog log = new AccessLog(id, timestamp, status, userId, userName);
                    logs.add(log);
                }
            }
        }

        return FXCollections.observableArrayList(logs);
    }

    /**
     * Récupère le journal d'accès le plus récent
     * @return Le journal d'accès le plus récent ou null si la table est vide
     * @throws SQLException En cas d'erreur de base de données
     */
    public static AccessLog getMostRecentAccessLog() throws SQLException {
        String sql = "SELECT al.id, al.timestamp, al.status, al.user_id, u.name " +
                "FROM access_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.timestamp DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int id = rs.getInt("id");
                String timestamp = rs.getString("timestamp");
                String status = rs.getString("status");
                Integer userId = rs.getInt("user_id");
                if (rs.wasNull()) {
                    userId = null;
                }
                String userName = rs.getString("name");
                if (userName == null) {
                    userName = "Inconnu";
                }

                return new AccessLog(id, timestamp, status, userId, userName);
            }
        }

        return null;
    }

    /**
     * Compte le nombre total de journaux d'accès dans la période spécifiée
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @return Nombre de journaux d'accès
     * @throws SQLException En cas d'erreur de base de données
     */
    public static int countAccessLogs(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM access_logs WHERE DATE(timestamp) BETWEEN ? AND ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Compte le nombre de journaux d'accès par statut dans la période spécifiée
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @param status Statut à compter
     * @return Nombre de journaux d'accès avec le statut spécifié
     * @throws SQLException En cas d'erreur de base de données
     */
    public static int countAccessLogsByStatus(LocalDate startDate, LocalDate endDate, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM access_logs WHERE DATE(timestamp) BETWEEN ? AND ? AND status = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            pstmt.setString(3, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Récupère le nombre d'accès par statut pour la période spécifiée
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @return Map avec le statut comme clé et le nombre d'accès comme valeur
     * @throws SQLException En cas d'erreur de base de données
     */
    public static Map<String, Integer> getAccessStatusCounts(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM access_logs " +
                "WHERE DATE(timestamp) BETWEEN ? AND ? GROUP BY status";

        Map<String, Integer> statusCounts = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("count");
                    statusCounts.put(status, count);
                }
            }
        }

        return statusCounts;
    }

    /**
     * Récupère le nombre d'accès par jour pour la période spécifiée
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @return Map avec la date comme clé et le nombre d'accès comme valeur
     * @throws SQLException En cas d'erreur de base de données
     */
    public static Map<String, Integer> getDailyAccessCounts(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT DATE(timestamp) as date, COUNT(*) as count FROM access_logs " +
                "WHERE DATE(timestamp) BETWEEN ? AND ? GROUP BY DATE(timestamp)";

        Map<String, Integer> dailyCounts = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    int count = rs.getInt("count");
                    dailyCounts.put(date, count);
                }
            }
        }

        return dailyCounts;
    }

    /**
     * Récupère le nombre d'accès par jour et par statut pour la période spécifiée
     * @param startDate Date de début (incluse)
     * @param endDate Date de fin (incluse)
     * @param status Statut à filtrer
     * @return Map avec la date comme clé et le nombre d'accès comme valeur
     * @throws SQLException En cas d'erreur de base de données
     */
    public static Map<String, Integer> getAccessTrendByStatus(LocalDate startDate, LocalDate endDate, String status) throws SQLException {
        String sql = "SELECT DATE(timestamp) as date, COUNT(*) as count FROM access_logs " +
                "WHERE DATE(timestamp) BETWEEN ? AND ? AND status = ? GROUP BY DATE(timestamp)";

        Map<String, Integer> trendCounts = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            pstmt.setString(3, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    int count = rs.getInt("count");
                    trendCounts.put(date, count);
                }
            }
        }

        return trendCounts;
    }
}