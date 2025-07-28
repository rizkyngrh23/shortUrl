package com.urlshortener;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class URLShortenerServer {
    private final HttpServer server;
    private final URLShortenerService urlService;
    private final AppConfig config;
    private final String baseUrl;

    public URLShortenerServer(int port, String host) throws IOException, SQLException {
        this.config = AppConfig.getInstance();
        this.baseUrl = String.format("http://%s:%d", host, port);
        this.urlService = new URLShortenerService();
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        
        setupRoutes();
        setupServer();
    }

    public URLShortenerServer() throws IOException, SQLException {
        this.config = AppConfig.getInstance();
        this.baseUrl = config.getBaseUrl();
        this.urlService = new URLShortenerService();
        this.server = HttpServer.create(new InetSocketAddress(
            config.getServerHost(), 
            config.getServerPort()
        ), 0);
        
        setupRoutes();
        setupServer();
    }

    private void setupRoutes() {
        server.createContext("/", new HomeHandler(baseUrl));
        
        server.createContext("/shorten", new ShortenHandler(urlService, baseUrl));
        server.createContext("/u/", new RedirectHandler(urlService));
        server.createContext("/analytics/", new AnalyticsHandler(urlService));
        
        server.createContext("/health", exchange -> {
            String response = "{\"status\":\"healthy\",\"timestamp\":\"" + 
                java.time.LocalDateTime.now().toString() + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });
    }

    private void setupServer() {
        server.setExecutor(Executors.newFixedThreadPool(config.getServerThreads()));
    }

    public void start() {
        server.start();
        
        config.printConfiguration();
        
        System.out.println("\nURL Shortener Server started successfully!");
        System.out.println("Server running on: " + baseUrl);
        System.out.println("Health check: " + baseUrl + "/health");
        System.out.println("Press Ctrl+C to stop the server");
        
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        System.out.println("\nShutting down server...");
        server.stop(1);
        
        try {
            urlService.close();
            System.out.println("Database connection closed.");
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
        
        System.out.println("Server stopped successfully.");
    }

    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.getInstance();
            
            int port = config.getServerPort();
            String host = config.getServerHost();
            
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--port":
                    case "-p":
                        if (i + 1 < args.length) {
                            try {
                                port = Integer.parseInt(args[i + 1]);
                                i++;
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port number: " + args[i + 1]);
                                System.exit(1);
                            }
                        }
                        break;
                    case "--host":
                    case "-h":
                        if (i + 1 < args.length) {
                            host = args[i + 1];
                            i++;
                        }
                        break;
                    case "--help":
                        printUsage();
                        return;
                    default:
                        if (args[i].startsWith("-")) {
                            System.err.println("Unknown option: " + args[i]);
                            printUsage();
                            System.exit(1);
                        }
                }
            }
            
            URLShortenerServer server = new URLShortenerServer(port, host);
            server.start();
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar url-shortener.jar [OPTIONS]");
        System.out.println("Options:");
        System.out.println("  -p, --port <port>    Server port (default: 8080)");
        System.out.println("  -h, --host <host>    Server host (default: localhost)");
        System.out.println("  --help               Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar url-shortener.jar");
        System.out.println("  java -jar url-shortener.jar --port 9000");
        System.out.println("  java -jar url-shortener.jar --host 0.0.0.0 --port 8080");
    }
}
