package com.urlshortener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;
    private Properties properties;

    private AppConfig() {
        EnvLoader.loadEnv();
        loadProperties();
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load " + CONFIG_FILE + ". Using default values.");
        }
        
        properties.putAll(System.getProperties());
    }

    public String getDatabaseUrl() {
        String databaseUrl = System.getenv("DATABASE_URL");
        System.out.println("DEBUG: DATABASE_URL = " + databaseUrl);
        if (databaseUrl != null) {
            // Convert Railway's postgresql:// format to JDBC format
            if (databaseUrl.startsWith("postgresql://")) {
                databaseUrl = "jdbc:" + databaseUrl;
                System.out.println("DEBUG: Converted to JDBC format: " + databaseUrl);
            }
            return databaseUrl;
        }
        String dbUrl = System.getenv("DB_URL");
        System.out.println("DEBUG: DB_URL = " + dbUrl);
        return getProperty("DB_URL", dbUrl);
    }

    public String getDatabaseUsername() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            return null;
        }
        return getProperty("DB_USERNAME", System.getenv("DB_USERNAME"));
    }

    public String getDatabasePassword() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null) {
            return null;
        }
        return getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));
    }

    public String getDatabaseDriver() {
        return getProperty("db.driver", "org.postgresql.Driver");
    }

    public int getMaxConnections() {
        return getIntProperty("db.max.connections", 10);
    }

    public int getConnectionTimeout() {
        return getIntProperty("db.connection.timeout", 30000);
    }

    public String getServerHost() {
        String host = System.getenv("SERVER_HOST");
        if (host != null) {
            return host;
        }
        return getProperty("SERVER_HOST", "0.0.0.0");
    }

    public int getServerPort() {
        String port = System.getenv("PORT");
        if (port != null) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid PORT value: " + port);
            }
        }
        String serverPort = System.getenv("SERVER_PORT");
        if (serverPort != null) {
            try {
                return Integer.parseInt(serverPort);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid SERVER_PORT value: " + serverPort);
            }
        }
        return getIntProperty("SERVER_PORT", 8080);
    }

    public int getServerThreads() {
        return getIntProperty("server.threads", 10);
    }

    public String getBaseUrl() {
        String baseUrl = System.getenv("BASE_URL");
        if (baseUrl != null) {
            return baseUrl;
        }
        
        baseUrl = getProperty("BASE_URL", null);
        if (baseUrl != null) {
            return baseUrl;
        }
        
        String protocol = "https";
        String host = getServerHost();
        int port = getServerPort();
        
        if (host.equals("localhost") || host.equals("127.0.0.1")) {
            protocol = "http";
        }
        
        if ((protocol.equals("https") && port == 443) || (protocol.equals("http") && port == 80)) {
            return String.format("%s://%s", protocol, host);
        } else {
            return String.format("%s://%s:%d", protocol, host, port);
        }
    }

    public int getShortCodeLength() {
        return getIntProperty("app.short.code.length", 6);
    }

    public int getMaxRetryAttempts() {
        return getIntProperty("app.max.retry.attempts", 5);
    }

    public boolean isUrlValidationEnabled() {
        return getBooleanProperty("security.validate.urls", true);
    }

    public boolean isBlockMaliciousEnabled() {
        return getBooleanProperty("security.block.malicious", true);
    }

    public boolean isAnalyticsEnabled() {
        return getBooleanProperty("analytics.enabled", true);
    }

    public boolean isClickTrackingEnabled() {
        return getBooleanProperty("analytics.track.clicks", true);
    }

    public boolean isCleanupEnabled() {
        return getBooleanProperty("cleanup.expired.urls", true);
    }

    public int getCleanupIntervalHours() {
        return getIntProperty("cleanup.interval.hours", 24);
    }

    private String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid integer value for " + key + ": " + value);
            }
        }
        return defaultValue;
    }

    private boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public void printConfiguration() {
        System.out.println("URL Shortener Configuration:");
        System.out.println("- Database URL: " + getDatabaseUrl());
        System.out.println("- Database User: " + getDatabaseUsername());
        System.out.println("- Server Host: " + getServerHost());
        System.out.println("- Server Port: " + getServerPort());
        System.out.println("- Base URL: " + getBaseUrl());
        System.out.println("- Short Code Length: " + getShortCodeLength());
        System.out.println("- URL Validation: " + isUrlValidationEnabled());
        System.out.println("- Analytics: " + isAnalyticsEnabled());
        System.out.println("- Cleanup: " + isCleanupEnabled());
    }
}
