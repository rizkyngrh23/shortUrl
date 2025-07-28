package com.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class RedirectHandler implements HttpHandler {
    private final URLShortenerService urlService;

    public RedirectHandler(URLShortenerService urlService) {
        this.urlService = urlService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

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

            URLShortenerService.RedirectResult result = urlService.redirect(shortCode);

            if (result.isSuccess()) {
                exchange.getResponseHeaders().set("Location", result.getRedirectUrl());
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
            } else {
                sendNotFoundResponse(exchange, result.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private String extractShortCode(String path) {
        if (path.startsWith("/u/")) {
            return path.substring(3);
        }
        return null;
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String htmlResponse = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Error %d</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }
                    .error { color: #d32f2f; }
                </style>
            </head>
            <body>
                <h1 class="error">Error %d</h1>
                <p>%s</p>
                <a href="/">Go Home</a>
            </body>
            </html>
            """, statusCode, statusCode, message);

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        byte[] responseBytes = htmlResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendNotFoundResponse(HttpExchange exchange, String message) throws IOException {
        String htmlResponse = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>URL Not Found</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 50px; text-align: center; }
                    .error { color: #d32f2f; }
                    .message { color: #666; margin: 20px 0; }
                </style>
            </head>
            <body>
                <h1 class="error">404 - URL Not Found</h1>
                <p class="message">%s</p>
                <p>The short URL you're looking for doesn't exist or may have expired.</p>
                <a href="/">Create a new short URL</a>
            </body>
            </html>
            """, message);

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        byte[] responseBytes = htmlResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
