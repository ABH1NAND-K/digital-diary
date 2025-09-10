package com.loginapp;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String URL = "jdbc:sqlite:users.db";
    private static String currentUser = null;

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Users table
            String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)";
            
            // Diary entries table
            String entriesTable = "CREATE TABLE IF NOT EXISTS diary_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "title TEXT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (user_id) REFERENCES users(id))"
                    ;
            
            stmt.execute(usersTable);
            stmt.execute(entriesTable);
            
            // Create trigger to update updated_at timestamp
            String trigger = "CREATE TRIGGER IF NOT EXISTS update_diary_timestamp " +
                           "AFTER UPDATE ON diary_entries " +
                           "BEGIN " +
                           "  UPDATE diary_entries SET updated_at = CURRENT_TIMESTAMP WHERE id = NEW.id; " +
                           "END;";
            stmt.execute(trigger);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setCurrentUser(String username) {
        currentUser = username;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        System.out.println("Getting user ID for current user: " + currentUser);
        if (currentUser == null) {
            System.err.println("No current user is set!");
            return -1;
        }
        
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser);
            System.out.println("Executing query: " + sql + " with username: " + currentUser);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                System.out.println("Found user ID: " + userId + " for username: " + currentUser);
                return userId;
            } else {
                System.err.println("No user found with username: " + currentUser);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID for " + currentUser + ": " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    public static void addDiaryEntry(String title, String content, LocalDateTime timestamp) {
        String sql = "INSERT INTO diary_entries (user_id, title, content, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, getCurrentUserId());
            pstmt.setString(2, title);
            pstmt.setString(3, content);
            pstmt.setString(4, timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateDiaryEntry(int id, String title, String content, LocalDateTime timestamp) {
        String sql = "UPDATE diary_entries SET title = ?, content = ?, updated_at = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setString(3, timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pstmt.setInt(4, id);
            pstmt.setInt(5, getCurrentUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDiaryEntry(int id) {
        String sql = "DELETE FROM diary_entries WHERE id = ? AND user_id = ?";
        Connection conn = null;
        try {
            conn = getConnection();
            // Disable auto-commit to control the transaction
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                pstmt.setInt(2, getCurrentUserId());
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Commit the transaction if rows were affected
                    conn.commit();
                    System.out.println("Successfully deleted diary entry with ID: " + id);
                } else {
                    // Rollback if no rows were affected (entry didn't exist or wasn't owned by user)
                    conn.rollback();
                    System.out.println("No diary entry found with ID: " + id + " for current user");
                }
            } catch (SQLException e) {
                // Rollback on any error
                if (conn != null) conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting diary entry with ID: " + id);
            e.printStackTrace();
        } finally {
            // Make sure to close the connection
            if (conn != null) {
                try {
                    // Reset auto-commit to true for the connection pool
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static List<DiaryEntry> getDiaryEntries(int limit, int offset) {
        List<DiaryEntry> entries = new ArrayList<>();
        int userId = getCurrentUserId();
        System.out.println("=== DATABASE DEBUG ===");
        System.out.println("Getting diary entries for user ID: " + userId);
        
        String sql = "SELECT id, user_id, title, content, " +
                   "strftime('%Y-%m-%d %H:%M:%S', created_at) as created_at " +
                   "FROM diary_entries WHERE user_id = ? " +
                   "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        System.out.println("Executing query: " + sql);
        System.out.println("Parameters - limit: " + limit + ", offset: " + offset);
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            
            System.out.println("Executing query...");
            ResultSet rs = pstmt.executeQuery();
            
            // Debug: Print column names
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            System.out.println("\nTable columns:");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("  " + metaData.getColumnName(i) + " (" + metaData.getColumnTypeName(i) + ")");
            }
            
            DateTimeFormatter dbDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                int id = rs.getInt("id");
                int entryUserId = rs.getInt("user_id");
                String title = rs.getString("title");
                String content = rs.getString("content");
                String timestampStr = rs.getString("created_at");
                LocalDateTime createdAt;
                
                try {
                    createdAt = LocalDateTime.parse(timestampStr, dbDateTimeFormatter);
                } catch (Exception e) {
                    System.err.println("Error parsing date: " + timestampStr + ", using current time");
                    e.printStackTrace();
                    createdAt = LocalDateTime.now();
                }
                
                System.out.println("Found entry: ID=" + id + ", Title: " + title + ", Created: " + createdAt);
                entries.add(new DiaryEntry(id, entryUserId, title, content, createdAt));
            }
            
            if (entries.size() == 0) {
                System.out.println("No entries found for user ID: " + userId);
                
                // Debug: Check if there are any entries in the table at all
                try (Statement stmt = conn.createStatement();
                     ResultSet allEntries = stmt.executeQuery("SELECT id, user_id, title, created_at FROM diary_entries")) {
                    System.out.println("\nAll entries in diary_entries table:");
                    int totalEntries = 0;
                    while (allEntries.next()) {
                        totalEntries++;
                        System.out.println("  Entry ID: " + allEntries.getInt("id") + 
                                        ", User ID: " + allEntries.getInt("user_id") +
                                        ", Title: " + allEntries.getString("title") +
                                        ", Created: " + allEntries.getTimestamp("created_at"));
                    }
                    System.out.println("Total entries in table: " + totalEntries);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting diary entries: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Returning " + entries.size() + " entries\n");
        return entries;
    }

    public static class DiaryEntry {
        private final int id;
        private final int userId;
        private final String title;
        private final String content;
        private final LocalDateTime createdAt;

        public DiaryEntry(int id, String title, String content, LocalDateTime createdAt) {
            this(id, -1, title, content, createdAt);
        }

        public DiaryEntry(int id, int userId, String title, String content, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.title = title;
            this.content = content;
            this.createdAt = createdAt;
        }

        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        @Override
        public String toString() {
            return title;
        }
    }
}
