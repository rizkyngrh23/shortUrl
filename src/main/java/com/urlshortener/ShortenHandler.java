package com.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ShortenHandler implements HttpHandler {
    private final URLShortenerService urlService;
    private final String baseUrl;

    public ShortenHandler(URLShortenerService urlService, String baseUrl) {
        this.urlService = urlService;
        this.baseUrl = baseUrl;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(200, 0);
            exchange.close();
            return;
        }

        if (!"POST".equals(method)) {
            sendErrorResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String requestBody = readRequestBody(exchange);
            JSONObject jsonRequest = new JSONObject(requestBody);

            String originalUrl = jsonRequest.optString("url", "");
            String customAlias = jsonRequest.optString("customAlias", null);
            String expiryDateStr = jsonRequest.optString("expiresAt", null);

            if (originalUrl.isEmpty()) {
                sendErrorResponse(exchange, 400, "URL is required");
                return;
            }

            LocalDateTime expiresAt = null;
            if (expiryDateStr != null && !expiryDateStr.trim().isEmpty()) {
                try {
                    expiresAt = LocalDateTime.parse(expiryDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    sendErrorResponse(exchange, 400, "Invalid expiry date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
                    return;
                }
            }

            URLShortenerService.ShortenResult result = urlService.shortenUrl(originalUrl, expiresAt, customAlias);

            if (result.isSuccess()) {
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("shortUrl", baseUrl + "/u/" + result.getUrlEntry().getShortCode());
                response.put("shortCode", result.getUrlEntry().getShortCode());
                response.put("originalUrl", result.getUrlEntry().getOriginalUrl());
                response.put("createdAt", result.getUrlEntry().getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                
                if (result.getUrlEntry().getExpiresAt() != null) {
                    response.put("expiresAt", result.getUrlEntry().getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }

                sendResponse(exchange, 200, response.toString());
            } else {
                sendErrorResponse(exchange, 400, result.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
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
