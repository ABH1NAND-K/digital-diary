package com.loginapp;

import java.sql.*;

public class UserDAO {
    public static boolean signup(String username, String password) {
        String hashedPassword = PasswordHasher.hashPassword(password);
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean login(String username, String password) {
        String sql = "SELECT id, username, password FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (PasswordHasher.verifyPassword(password, storedHash)) {
                    // Set the current user in the Database class
                    Database.setCurrentUser(username);
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
