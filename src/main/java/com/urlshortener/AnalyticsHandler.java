package com.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AnalyticsHandler implements HttpHandler {
    private final URLShortenerService urlService;

    public AnalyticsHandler(URLShortenerService urlService) {
        this.urlService = urlService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        if (!"GET".equals(method)) {
            sendErrorResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();
            String shortCode = extractShortCode(path);

            if (shortCode == null || shortCode.isEmpty()) {
                sendErrorResponse(exchange, 400, "Invalid short code");
                return;
            }

            Optional<URLEntry> urlEntryOpt = urlService.getAnalytics(shortCode);

            if (urlEntryOpt.isPresent()) {
                URLEntry urlEntry = urlEntryOpt.get();
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("shortCode", urlEntry.getShortCode());
                response.put("originalUrl", urlEntry.getOriginalUrl());
                response.put("clickCount", urlEntry.getClickCount());
                response.put("createdAt", urlEntry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                if (urlEntry.getExpiresAt() != null) {
                    response.put("expiresAt", urlEntry.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    response.put("isExpired", urlEntry.isExpired());
                }
                
                if (urlEntry.getCustomAlias() != null) {
                    response.put("customAlias", urlEntry.getCustomAlias());
                }

                sendResponse(exchange, 200, response.toString());
            } else {
                sendErrorResponse(exchange, 404, "Short code not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private String extractShortCode(String path) {
        if (path.startsWith("/analytics/")) {
            return path.substring(11);
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        sendResponse(exchange, statusCode, errorResponse.toString());
    }
}
