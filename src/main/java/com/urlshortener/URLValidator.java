package com.urlshortener;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class URLValidator {
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("^javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PATTERN = Pattern.compile("^data:", Pattern.CASE_INSENSITIVE);
    private static final Pattern FILE_PATTERN = Pattern.compile("^file:", Pattern.CASE_INSENSITIVE);
    
    private static final Pattern[] MALICIOUS_PATTERNS = {
        JAVASCRIPT_PATTERN,
        DATA_PATTERN,
        FILE_PATTERN
    };

    public static boolean isValidUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return false;
        }
        
        try {
            String normalizedUrl = normalizeUrl(urlString);
            
            URI uri = new URI(normalizedUrl);
            URL url = uri.toURL();
            
            String protocol = url.getProtocol().toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                return false;
            }
            
            for (Pattern pattern : MALICIOUS_PATTERNS) {
                if (pattern.matcher(normalizedUrl).find()) {
                    return false;
                }
            }
            
            String host = url.getHost();
            if (host == null || host.trim().isEmpty()) {
                return false;
            }
            
            if (!isValidHostname(host)) {
                return false;
            }
            
            return true;
            
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private static boolean isValidHostname(String hostname) {
        if (hostname == null || hostname.trim().isEmpty()) {
            return false;
        }
        
        if (hostname.equals("localhost") || hostname.equals("127.0.0.1")) {
            return true;
        }
        
        if (hostname.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            return true;
        }
        
        if (!hostname.contains(".")) {
            return false;
        }
        
        return hostname.matches("^[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?)*$");
    }

    public static String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }
        
        url = url.trim();
        
        if (!url.matches("^https?://.*")) {
            url = "http://" + url;
        }
        
        return url;
    }

    public static boolean isValidAlias(String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return false;
        }
        
        return alias.matches("^[a-zA-Z0-9_-]{3,50}$");
    }
}
