package com.loginapp;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 */
public class PasswordHasher {
    // Define the workload for the BCrypt hashing (4-31, default is 10)
    private static final int WORKLOAD = 12;

    /**
     * Hashes a password using BCrypt.
     * @param password The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(password, salt);
    }

    /**
     * Verifies a password against a hashed password.
     * @param password The plain text password to verify
     * @param hashedPassword The hashed password to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || password.isEmpty() || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
