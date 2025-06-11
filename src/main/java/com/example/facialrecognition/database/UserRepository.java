package com.example.facialrecognition.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.example.facialrecognition.model.User;

public class UserRepository {

    /**
     * Insère un nouvel utilisateur dans la base de données
     * @param name Nom de l'utilisateur
     * @param biometricData Données biométriques (image encodée)
     * @return ID de l'utilisateur créé
     * @throws SQLException En cas d'erreur de base de données
     */
    public static int insertUser(String name, byte[] biometricData) throws SQLException {
        String sql = "INSERT INTO users (name, status, biometric_data) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, "active");  // Statut par défaut
            pstmt.setBytes(3, biometricData);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("L'insertion de l'utilisateur a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("L'insertion de l'utilisateur a échoué, aucun ID obtenu.");
                }
            }
        }
    }

    /**
     * Récupère tous les utilisateurs de la base de données
     * @return Liste observable d'utilisateurs pour TableView
     */
    public static ObservableList<User> getAllUsers() throws SQLException {
        String sql = "SELECT id, name, status, biometric_data FROM users";
        ObservableList<User> users = FXCollections.observableArrayList();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String status = rs.getString("status");
                byte[] biometricData = rs.getBytes("biometric_data");

                // Construction de l'objet User et ajout à la liste
                User user = new User(id, name, status, biometricData);
                users.add(user);
            }
        }

        return users;
    }

    /**
     * Vérifie si un utilisateur avec le nom donné existe déjà
     * @param name Nom à vérifier
     * @return true si le nom existe déjà, false sinon
     */
    public static boolean userExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * Supprime un utilisateur de la base de données
     * @param userId ID de l'utilisateur à supprimer
     * @return true si la suppression a réussi, false sinon
     * @throws SQLException En cas d'erreur de base de données
     */
    public static boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Modifie les informations d'un utilisateur dans la base de données
     * @param userId ID de l'utilisateur à modifier
     * @param name Nouveau nom (ou null pour ne pas modifier)
     * @param status Nouveau statut (ou null pour ne pas modifier)
     * @param biometricData Nouvelles données biométriques (ou null pour ne pas modifier)
     * @return true si la modification a réussi, false sinon
     * @throws SQLException En cas d'erreur de base de données
     */
    public static boolean updateUser(int userId, String name, String status, byte[] biometricData) throws SQLException {
        // Construction dynamique de la requête SQL selon les champs à modifier
        StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
        boolean hasUpdate = false;

        if (name != null) {
            sqlBuilder.append("name = ?");
            hasUpdate = true;
        }

        if (status != null) {
            if (hasUpdate) sqlBuilder.append(", ");
            sqlBuilder.append("status = ?");
            hasUpdate = true;
        }

        if (biometricData != null) {
            if (hasUpdate) sqlBuilder.append(", ");
            sqlBuilder.append("biometric_data = ?");
            hasUpdate = true;
        }

        // Si aucun champ à modifier, retourner false
        if (!hasUpdate) {
            return false;
        }

        sqlBuilder.append(" WHERE id = ?");
        String sql = sqlBuilder.toString();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;

            if (name != null) {
                pstmt.setString(paramIndex++, name);
            }

            if (status != null) {
                pstmt.setString(paramIndex++, status);
            }

            if (biometricData != null) {
                pstmt.setBytes(paramIndex++, biometricData);
            }

            // Ajout de l'ID comme dernier paramètre
            pstmt.setInt(paramIndex, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Version simplifiée pour mettre à jour tous les champs d'un utilisateur
     * @param user Objet User contenant toutes les informations à mettre à jour
     * @return true si la modification a réussi, false sinon
     * @throws SQLException En cas d'erreur de base de données
     */
    public static boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, status = ?, biometric_data = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getStatus());
            pstmt.setBytes(3, user.getBiometricData());
            pstmt.setInt(4, user.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Récupère un utilisateur par son ID
     * @param userId ID de l'utilisateur à récupérer
     * @return L'utilisateur correspondant ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur de base de données
     */
    public static User getUserById(int userId) throws SQLException {
        String sql = "SELECT id, name, status, biometric_data FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String status = rs.getString("status");
                    byte[] biometricData = rs.getBytes("biometric_data");

                    return new User(id, name, status, biometricData);
                }
            }
        }

        return null;
    }

    /**
     * Affiche tous les utilisateurs de la base de données dans la console
     * Utile pour le débogage et la vérification des données
     * @throws SQLException En cas d'erreur de base de données
     */
    public static void displayAllUsers() throws SQLException {
        ObservableList<User> users = getAllUsers();

        System.out.println("=== Liste des utilisateurs dans la base de données ===");
        System.out.println("Total: " + users.size() + " utilisateur(s)");
        System.out.println("----------------------------------------------------");

        if (users.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé dans la base de données.");
        } else {
            System.out.printf("%-5s | %-20s | %-10s | %-15s\n", "ID", "Nom", "Statut", "Données biométriques");
            System.out.println("----------------------------------------------------");

            for (User user : users) {
                String biometricInfo = (user.getBiometricData() != null) ?
                        user.getBiometricData().length + " octets" : "Non disponible";

                System.out.printf("%-5d | %-20s | %-10s | %s\n",
                        user.getId(),
                        user.getName(),
                        user.getStatus(),
                        biometricInfo);
            }
        }
        System.out.println("====================================================");
    }

    /**
     * Compte le nombre total d'utilisateurs dans la base de données
     * @return Le nombre d'utilisateurs
     * @throws SQLException En cas d'erreur de base de données
     */
    public static int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }
}