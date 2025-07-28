package com.urlshortener;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HomeHandler implements HttpHandler {
    private final String baseUrl;

    public HomeHandler(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (!"GET".equals(method)) {
            exchange.sendResponseHeaders(405, 0);
            exchange.close();
            return;
        }

        String htmlResponse = createHomePageHtml();
        
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        byte[] responseBytes = htmlResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String createHomePageHtml() {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>URL Shortener</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        min-height: 100vh;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    
                    .container {
                        max-width: 600px;
                        width: 90%%;
                        background: rgba(255, 255, 255, 0.95);
                        border-radius: 20px;
                        padding: 2rem;
                        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
                        backdrop-filter: blur(10px);
                    }
                    
                    .header {
                        text-align: center;
                        margin-bottom: 2rem;
                    }
                    
                    .header h1 {
                        color: #4a5568;
                        margin-bottom: 0.5rem;
                        font-size: 2.5rem;
                    }
                    
                    .header p {
                        color: #718096;
                        font-size: 1.1rem;
                    }
                    
                    .form-group {
                        margin-bottom: 1.5rem;
                    }
                    
                    label {
                        display: block;
                        margin-bottom: 0.5rem;
                        font-weight: 600;
                        color: #4a5568;
                    }
                    
                    input[type="text"], input[type="datetime-local"] {
                        width: 100%%;
                        padding: 0.75rem;
                        border: 2px solid #e2e8f0;
                        border-radius: 10px;
                        font-size: 1rem;
                        transition: border-color 0.3s ease;
                    }
                    
                    input[type="text"]:focus, input[type="datetime-local"]:focus {
                        outline: none;
                        border-color: #667eea;
                        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
                    }
                    
                    .btn {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 0.75rem 2rem;
                        border: none;
                        border-radius: 10px;
                        font-size: 1rem;
                        font-weight: 600;
                        cursor: pointer;
                        transition: transform 0.2s ease, box-shadow 0.2s ease;
                        width: 100%%;
                    }
                    
                    .btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
                    }
                    
                    .btn:active {
                        transform: translateY(0);
                    }
                    
                    .result {
                        margin-top: 2rem;
                        padding: 1rem;
                        border-radius: 10px;
                        display: none;
                    }
                    
                    .result.success {
                        background: #f0fff4;
                        border: 2px solid #9ae6b4;
                        color: #2f855a;
                    }
                    
                    .result.error {
                        background: #fff5f5;
                        border: 2px solid #feb2b2;
                        color: #c53030;
                    }
                    
                    .short-url {
                        margin-top: 1rem;
                        padding: 1rem;
                        background: #edf2f7;
                        border-radius: 8px;
                        word-break: break-all;
                    }
                    
                    .short-url a {
                        color: #667eea;
                        text-decoration: none;
                        font-weight: 600;
                    }
                    
                    .short-url a:hover {
                        text-decoration: underline;
                    }
                    
                    .copy-btn {
                        margin-top: 0.5rem;
                        background: #4a5568;
                        color: white;
                        border: none;
                        padding: 0.5rem 1rem;
                        border-radius: 5px;
                        cursor: pointer;
                        font-size: 0.9rem;
                    }
                    
                    .copy-btn:hover {
                        background: #2d3748;
                    }
                    
                    .features {
                        margin-top: 2rem;
                        text-align: center;
                    }
                    
                    .features h3 {
                        color: #4a5568;
                        margin-bottom: 1rem;
                    }
                    
                    .features ul {
                        list-style: none;
                        color: #718096;
                    }
                    
                    .features li {
                        margin-bottom: 0.5rem;
                    }
                    
                    .features li:before {
                        content: "✓ ";
                        color: #48bb78;
                        font-weight: bold;
                    }
                    
                    .analytics-link {
                        margin-top: 1rem;
                        display: block;
                        color: #667eea;
                        text-decoration: none;
                        font-size: 0.9rem;
                    }
                    
                    .analytics-link:hover {
                        text-decoration: underline;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔗 URL Shortener</h1>
                        <p>Transform long URLs into short, shareable links</p>
                    </div>
                    
                    <form id="shortenForm">
                        <div class="form-group">
                            <label for="url">Long URL:</label>
                            <input type="text" id="url" name="url" placeholder="https://example.com/very/long/url" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="customAlias">Custom Alias (optional):</label>
                            <input type="text" id="customAlias" name="customAlias" placeholder="my-custom-link">
                        </div>
                        
                        <div class="form-group">
                            <label for="expiresAt">Expires At (optional):</label>
                            <input type="datetime-local" id="expiresAt" name="expiresAt">
                        </div>
                        
                        <button type="submit" class="btn">Shorten URL</button>
                    </form>
                    
                    <div id="result" class="result">
                        <div id="resultMessage"></div>
                        <div id="shortUrlContainer" class="short-url" style="display: none;">
                            <div>Short URL: <a id="shortUrlLink" href="#" target="_blank"></a></div>
                            <button class="copy-btn" onclick="copyToClipboard()">Copy URL</button>
                            <a id="analyticsLink" class="analytics-link" href="#" style="display: none;">View Analytics</a>
                        </div>
                    </div>
                    
                    <div class="features">
                        <h3>Features</h3>
                        <ul>
                            <li>Custom aliases for branded links</li>
                            <li>Expiry dates for temporary links</li>
                            <li>Click analytics and tracking</li>
                            <li>Secure redirect validation</li>
                        </ul>
                    </div>
                </div>
                
                <script>
                    document.getElementById('shortenForm').addEventListener('submit', async function(e) {
                        e.preventDefault();
                        
                        const url = document.getElementById('url').value;
                        const customAlias = document.getElementById('customAlias').value;
                        const expiresAt = document.getElementById('expiresAt').value;
                        
                        const requestData = { url };
                        if (customAlias) requestData.customAlias = customAlias;
                        if (expiresAt) requestData.expiresAt = expiresAt;
                        
                        try {
                            const response = await fetch('/shorten', {
                                method: 'POST',
                                headers: {
                                    'Content-Type': 'application/json',
                                },
                                body: JSON.stringify(requestData)
                            });
                            
                            const data = await response.json();
                            const resultDiv = document.getElementById('result');
                            const resultMessage = document.getElementById('resultMessage');
                            const shortUrlContainer = document.getElementById('shortUrlContainer');
                            
                            if (data.success) {
                                resultDiv.className = 'result success';
                                resultMessage.textContent = 'URL shortened successfully!';
                                
                                const shortUrlLink = document.getElementById('shortUrlLink');
                                shortUrlLink.href = data.shortUrl;
                                shortUrlLink.textContent = data.shortUrl;
                                
                                const analyticsLink = document.getElementById('analyticsLink');
                                analyticsLink.href = '/analytics/' + data.shortCode;
                                analyticsLink.style.display = 'block';
                                
                                shortUrlContainer.style.display = 'block';
                            } else {
                                resultDiv.className = 'result error';
                                resultMessage.textContent = 'Error: ' + data.error;
                                shortUrlContainer.style.display = 'none';
                            }
                            
                            resultDiv.style.display = 'block';
                            
                        } catch (error) {
                            const resultDiv = document.getElementById('result');
                            resultDiv.className = 'result error';
                            document.getElementById('resultMessage').textContent = 'Network error: ' + error.message;
                            document.getElementById('shortUrlContainer').style.display = 'none';
                            resultDiv.style.display = 'block';
                        }
                    });
                    
                    function copyToClipboard() {
                        const shortUrl = document.getElementById('shortUrlLink').textContent;
                        navigator.clipboard.writeText(shortUrl).then(function() {
                            const copyBtn = document.querySelector('.copy-btn');
                            const originalText = copyBtn.textContent;
                            copyBtn.textContent = 'Copied!';
                            setTimeout(() => {
                                copyBtn.textContent = originalText;
                            }, 2000);
                        });
                    }
                </script>
            </body>
            </html>
            """, baseUrl);
    }
}
