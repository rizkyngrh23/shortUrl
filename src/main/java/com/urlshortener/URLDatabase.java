package com.urlshortener;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class URLDatabase {
    private final AppConfig config;
    private Connection connection;

    public URLDatabase() throws SQLException {
        this.config = AppConfig.getInstance();
        initializeDatabase();
    }

    public URLDatabase(AppConfig config) throws SQLException {
        this.config = config;
        initializeDatabase();
    }

    private void initializeDatabase() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("DEBUG: PostgreSQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: PostgreSQL driver not found in classpath");
            throw new SQLException("PostgreSQL driver not found", e);
        }
        
        String databaseUrl = config.getDatabaseUrl();
        String username = config.getDatabaseUsername();
        String password = config.getDatabasePassword();
        
        System.out.println("DEBUG: Attempting to connect with URL: " + databaseUrl);
        System.out.println("DEBUG: Username: " + username);
        System.out.println("DEBUG: Password: " + (password != null ? "[HIDDEN]" : "null"));
        
        if (username != null && password != null) {
            connection = DriverManager.getConnection(databaseUrl, username, password);
        } else {
            connection = DriverManager.getConnection(databaseUrl);
        }
        System.out.println("DEBUG: Database connection successful");
        createTables();
    }

    private void createTables() throws SQLException {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS urls (
                id SERIAL PRIMARY KEY,
                short_code VARCHAR(255) UNIQUE NOT NULL,
                original_url TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expires_at TIMESTAMP,
                click_count INTEGER DEFAULT 0,
                custom_alias VARCHAR(255)
            )
            """;

        String createIndexSQL = "CREATE INDEX IF NOT EXISTS idx_short_code ON urls(short_code)";
        String createAliasIndexSQL = "CREATE INDEX IF NOT EXISTS idx_custom_alias ON urls(custom_alias)";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            stmt.execute(createIndexSQL);
            stmt.execute(createAliasIndexSQL);
        }
    }

    public boolean saveURL(URLEntry urlEntry) throws SQLException {
        String insertSQL = """
            INSERT INTO urls (short_code, original_url, created_at, expires_at, click_count, custom_alias)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, urlEntry.getShortCode());
            pstmt.setString(2, urlEntry.getOriginalUrl());
            pstmt.setTimestamp(3, urlEntry.getCreatedAt() != null ? 
                Timestamp.valueOf(urlEntry.getCreatedAt()) : null);
            pstmt.setTimestamp(4, urlEntry.getExpiresAt() != null ? 
                Timestamp.valueOf(urlEntry.getExpiresAt()) : null);
            pstmt.setInt(5, urlEntry.getClickCount());
            pstmt.setString(6, urlEntry.getCustomAlias());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        urlEntry.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public Optional<URLEntry> findByShortCode(String shortCode) throws SQLException {
        String selectSQL = "SELECT * FROM urls WHERE short_code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, shortCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToURLEntry(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<URLEntry> findByCustomAlias(String alias) throws SQLException {
        String selectSQL = "SELECT * FROM urls WHERE custom_alias = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, alias);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToURLEntry(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean incrementClickCount(String shortCode) throws SQLException {
        String updateSQL = "UPDATE urls SET click_count = click_count + 1 WHERE short_code = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, shortCode);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Optional<URLEntry> getAnalytics(String shortCode) throws SQLException {
        return findByShortCode(shortCode);
    }

    public List<URLEntry> getAllUrls() throws SQLException {
        List<URLEntry> urls = new ArrayList<>();
        String selectSQL = "SELECT * FROM urls ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            
            while (rs.next()) {
                urls.add(mapResultSetToURLEntry(rs));
            }
        }
        return urls;
    }

    public int deleteExpiredUrls() throws SQLException {
        String deleteSQL = "DELETE FROM urls WHERE expires_at < ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            return pstmt.executeUpdate();
        }
    }

    public boolean shortCodeExists(String shortCode) throws SQLException {
        String selectSQL = "SELECT 1 FROM urls WHERE short_code = ? LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, shortCode);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean customAliasExists(String alias) throws SQLException {
        String selectSQL = "SELECT 1 FROM urls WHERE custom_alias = ? LIMIT 1";
        
        try (PreparedStatement pstmt = connection.prepareStatement(selectSQL)) {
            pstmt.setString(1, alias);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private URLEntry mapResultSetToURLEntry(ResultSet rs) throws SQLException {
        URLEntry entry = new URLEntry();
        entry.setId(rs.getInt("id"));
        entry.setShortCode(rs.getString("short_code"));
        entry.setOriginalUrl(rs.getString("original_url"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            entry.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) {
            entry.setExpiresAt(expiresAt.toLocalDateTime());
        }
        
        entry.setClickCount(rs.getInt("click_count"));
        entry.setCustomAlias(rs.getString("custom_alias"));
        
        return entry;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public long getNextId() throws SQLException {
        String selectSQL = "SELECT nextval('urls_id_seq')";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 1;
    }
}
