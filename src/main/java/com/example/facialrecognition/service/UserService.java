package com.example.facialrecognition.service;

import com.example.facialrecognition.database.DatabaseManager;
import com.example.facialrecognition.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserService {

    /**
     * Supprime un utilisateur de la base de données en fonction de son ID.
     */
    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
        }
    }

    /**
     * Met à jour les informations d'un utilisateur (nom, statut) dans la base de données.
     */
    public void updateUser(User user) {
        String sql = "UPDATE users SET name = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getStatus());
            stmt.setInt(3, user.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
        }
    }
}
