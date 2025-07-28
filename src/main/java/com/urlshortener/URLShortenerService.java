package com.urlshortener;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class URLShortenerService {
    private final URLDatabase database;
    private static final int DEFAULT_SHORT_CODE_LENGTH = 6;
    private static final int MAX_RETRY_ATTEMPTS = 5;

    public URLShortenerService() throws SQLException {
        this.database = new URLDatabase();
    }

    public URLShortenerService(URLDatabase database) {
        this.database = database;
    }

    public ShortenResult shortenUrl(String originalUrl, LocalDateTime expiresAt, String customAlias) throws SQLException {
        originalUrl = URLValidator.normalizeUrl(originalUrl);
        if (!URLValidator.isValidUrl(originalUrl)) {
            return new ShortenResult(false, "Invalid URL format", null);
        }

        if (customAlias != null && !customAlias.trim().isEmpty()) {
            if (!URLValidator.isValidAlias(customAlias)) {
                return new ShortenResult(false, "Invalid custom alias format", null);
            }
            
            if (database.customAliasExists(customAlias)) {
                return new ShortenResult(false, "Custom alias already exists", null);
            }
        }

        String shortCode;
        
        if (customAlias != null && !customAlias.trim().isEmpty()) {
            shortCode = customAlias;
        } else {
            shortCode = generateUniqueShortCode();
            if (shortCode == null) {
                return new ShortenResult(false, "Failed to generate unique short code", null);
            }
        }

        URLEntry urlEntry = new URLEntry(shortCode, originalUrl, expiresAt);
        if (customAlias != null && !customAlias.trim().isEmpty()) {
            urlEntry.setCustomAlias(customAlias);
        }

        if (database.saveURL(urlEntry)) {
            return new ShortenResult(true, "URL shortened successfully", urlEntry);
        } else {
            return new ShortenResult(false, "Failed to save URL", null);
        }
    }

    public ShortenResult shortenUrl(String originalUrl) throws SQLException {
        return shortenUrl(originalUrl, null, null);
    }

    public RedirectResult redirect(String shortCode) throws SQLException {
        Optional<URLEntry> urlEntryOpt = database.findByShortCode(shortCode);
        
        if (urlEntryOpt.isEmpty()) {
            urlEntryOpt = database.findByCustomAlias(shortCode);
        }
        
        if (urlEntryOpt.isEmpty()) {
            return new RedirectResult(false, "Short code not found", null);
        }

        URLEntry urlEntry = urlEntryOpt.get();

        if (urlEntry.isExpired()) {
            return new RedirectResult(false, "URL has expired", null);
        }

        database.incrementClickCount(urlEntry.getShortCode());

        return new RedirectResult(true, "Redirect successful", urlEntry.getOriginalUrl());
    }

    public Optional<URLEntry> getAnalytics(String shortCode) throws SQLException {
        Optional<URLEntry> urlEntryOpt = database.findByShortCode(shortCode);
        
        if (urlEntryOpt.isEmpty()) {
            urlEntryOpt = database.findByCustomAlias(shortCode);
        }
        
        return urlEntryOpt;
    }

    public int cleanupExpiredUrls() throws SQLException {
        return database.deleteExpiredUrls();
    }

    private String generateUniqueShortCode() throws SQLException {
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                long nextId = database.getNextId();
                String shortCode = Base62Encoder.encode(nextId);
                
                if (shortCode.length() < DEFAULT_SHORT_CODE_LENGTH) {
                    shortCode = Base62Encoder.generateRandomCode(DEFAULT_SHORT_CODE_LENGTH);
                }
                
                if (!database.shortCodeExists(shortCode)) {
                    return shortCode;
                }
            } catch (Exception e) {
                String randomCode = Base62Encoder.generateRandomCode(DEFAULT_SHORT_CODE_LENGTH);
                if (!database.shortCodeExists(randomCode)) {
                    return randomCode;
                }
            }
        }
        return null;
    }

    public void close() throws SQLException {
        if (database != null) {
            database.close();
        }
    }

    public static class ShortenResult {
        private final boolean success;
        private final String message;
        private final URLEntry urlEntry;

        public ShortenResult(boolean success, String message, URLEntry urlEntry) {
            this.success = success;
            this.message = message;
            this.urlEntry = urlEntry;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public URLEntry getUrlEntry() { return urlEntry; }
    }

    public static class RedirectResult {
        private final boolean success;
        private final String message;
        private final String redirectUrl;

        public RedirectResult(boolean success, String message, String redirectUrl) {
            this.success = success;
            this.message = message;
            this.redirectUrl = redirectUrl;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getRedirectUrl() { return redirectUrl; }
    }
}
