# URL Shortener Service

A lightweight, native Java URL shortener service with REST API and web interface.

## Features

- Shorten long URLs with unique short codes
- Redirect to original URLs
- Analytics and click tracking
- Custom aliases for branded links
- Expiry dates for temporary links

## Tech Stack

- **Java 17**
- **PostgreSQL**
- **Maven**
- **Native HTTP Server**

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL

### Build and Run
```bash
mvn clean package
java -jar target/url-shortener-1.0.0-shaded.jar
```

The service starts on `http://localhost:8080`

## API Endpoints

- **POST** `/shorten` - Create short URL
- **GET** `/u/{code}` - Redirect to original URL
- **GET** `/analytics/{code}` - Get analytics data
- **GET** `/health` - Health check
