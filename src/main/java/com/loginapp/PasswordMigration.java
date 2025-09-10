package com.loginapp;

import java.sql.*;

/**
 * Utility class for migrating existing plaintext passwords to hashed passwords.
 * This should be run once after deploying the password hashing update.
 */
public class PasswordMigration {
    
    /**
     * Migrates all plaintext passwords in the database to hashed passwords.
     * This is a one-time operation that should be run after deploying the password hashing update.
     */
    public static void migratePasswords() {
        String selectSql = "SELECT id, username, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            conn.setAutoCommit(false);
            int updatedCount = 0;
            
            while (rs.next()) {
                String password = rs.getString("password");
                
                // Skip if already hashed (BCrypt hashes start with $2a$)
                if (password.startsWith("$2a$")) {
                    continue;
                }
                
                // Hash the plaintext password
                String hashedPassword = PasswordHasher.hashPassword(password);
                
                // Update the password in the database
                try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, hashedPassword);
                    pstmt.setInt(2, rs.getInt("id"));
                    pstmt.executeUpdate();
                    updatedCount++;
                }
            }
            
            conn.commit();
            System.out.println("Password migration complete. Updated " + updatedCount + " users.");
            
        } catch (SQLException e) {
            System.err.println("Error during password migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method to run the password migration.
     * This should be run once after deploying the password hashing update.
     */
    public static void main(String[] args) {
        System.out.println("Starting password migration...");
        migratePasswords();
    }
}
