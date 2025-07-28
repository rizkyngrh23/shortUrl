package com.urlshortener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static AppConfig instance;
    private Properties properties;

    private AppConfig() {
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
        return getProperty("db.url", "jdbc:postgresql://localhost:5432/url-shortener");
    }

    public String getDatabaseUsername() {
        return getProperty("db.username", "postgres");
    }

    public String getDatabasePassword() {
        return getProperty("db.password", "kiki2002");
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
        return getProperty("server.host", "localhost");
    }

    public int getServerPort() {
        return getIntProperty("server.port", 8080);
    }

    public int getServerThreads() {
        return getIntProperty("server.threads", 10);
    }

    public String getBaseUrl() {
        String baseUrl = getProperty("app.base.url", null);
        if (baseUrl == null) {
            baseUrl = String.format("http://%s:%d", getServerHost(), getServerPort());
        }
        return baseUrl;
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
