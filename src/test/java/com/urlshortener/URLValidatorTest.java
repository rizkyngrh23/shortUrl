package com.urlshortener;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class URLValidatorTest {

    @Test
    void testValidUrls() {
        assertTrue(URLValidator.isValidUrl("http://example.com"));
        assertTrue(URLValidator.isValidUrl("https://example.com"));
        assertTrue(URLValidator.isValidUrl("https://www.example.com/path"));
        assertTrue(URLValidator.isValidUrl("http://example.com:8080/path?query=value"));
        assertTrue(URLValidator.isValidUrl("https://sub.domain.example.com/path#fragment"));
    }

    @Test
    void testInvalidUrls() {
        assertFalse(URLValidator.isValidUrl(null));
        assertFalse(URLValidator.isValidUrl(""));
        assertFalse(URLValidator.isValidUrl("   "));
        assertFalse(URLValidator.isValidUrl("not-a-url"));
        assertFalse(URLValidator.isValidUrl("ftp://example.com"));
        assertFalse(URLValidator.isValidUrl("javascript:alert('xss')"));
        assertFalse(URLValidator.isValidUrl("data:text/html,<script>alert('xss')</script>"));
        assertFalse(URLValidator.isValidUrl("file:///etc/passwd"));
    }

    @Test
    void testNormalizeUrl() {
        assertEquals("http://example.com", URLValidator.normalizeUrl("example.com"));
        assertEquals("http://www.example.com", URLValidator.normalizeUrl("www.example.com"));
        assertEquals("https://example.com", URLValidator.normalizeUrl("https://example.com"));
        assertEquals("http://example.com", URLValidator.normalizeUrl("http://example.com"));
        
        assertEquals("http://example.com", URLValidator.normalizeUrl("  example.com  "));
        
        assertNull(URLValidator.normalizeUrl(null));
        assertEquals("", URLValidator.normalizeUrl(""));
    }

    @Test
    void testValidAlias() {
        assertTrue(URLValidator.isValidAlias("abc"));
        assertTrue(URLValidator.isValidAlias("abc123"));
        assertTrue(URLValidator.isValidAlias("my-link"));
        assertTrue(URLValidator.isValidAlias("my_link"));
        assertTrue(URLValidator.isValidAlias("ABC123def"));
        assertTrue(URLValidator.isValidAlias("a".repeat(50)));
    }

    @Test
    void testInvalidAlias() {
        assertFalse(URLValidator.isValidAlias(null));
        assertFalse(URLValidator.isValidAlias(""));
        assertFalse(URLValidator.isValidAlias("  "));
        assertFalse(URLValidator.isValidAlias("ab"));
        assertFalse(URLValidator.isValidAlias("a".repeat(51)));
        assertFalse(URLValidator.isValidAlias("abc def"));
        assertFalse(URLValidator.isValidAlias("abc@def"));
        assertFalse(URLValidator.isValidAlias("abc.def"));
    }
}
