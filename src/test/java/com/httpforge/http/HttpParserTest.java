package com.httpforge.http;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpParserTest {

    @Test
    void testParseSimpleGetRequest() throws IOException, HttpParser.HttpParseException {
        String rawRequest = "GET /hello HTTP/1.1\r\n" +
                           "Host: localhost\r\n" +
                           "User-Agent: test\r\n" +
                           "\r\n";

        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpParser.parse(input);

        assertEquals("GET", request.getMethod());
        assertEquals("/hello", request.getPath());
        assertEquals("localhost", request.getHeader("Host"));
        assertEquals("test", request.getHeader("User-Agent"));
        assertEquals("", request.getBody());
    }

    @Test
    void testParsePostRequestWithBody() throws IOException, HttpParser.HttpParseException {
        String body = "test data";
        String rawRequest = "POST /data HTTP/1.1\r\n" +
                           "Host: localhost\r\n" +
                           "Content-Length: " + body.length() + "\r\n" +
                           "\r\n" +
                           body;

        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpParser.parse(input);

        assertEquals("POST", request.getMethod());
        assertEquals("/data", request.getPath());
        assertEquals(body, request.getBody());
    }

    @Test
    void testParseCaseInsensitiveHeaders() throws IOException, HttpParser.HttpParseException {
        String rawRequest = "GET / HTTP/1.1\r\n" +
                           "Content-Length: 0\r\n" +
                           "\r\n";

        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpParser.parse(input);

        // Should be accessible with different casing
        assertNotNull(request.getHeader("Content-Length"));
        assertNotNull(request.getHeader("content-length"));
    }

    @Test
    void testParseEmptyRequestThrowsException() {
        String rawRequest = "\r\n";
        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));

        assertThrows(HttpParser.HttpParseException.class, () -> {
            HttpParser.parse(input);
        });
    }

    @Test
    void testParseInvalidRequestLineThrowsException() {
        String rawRequest = "INVALID\r\n\r\n";
        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));

        assertThrows(HttpParser.HttpParseException.class, () -> {
            HttpParser.parse(input);
        });
    }

    @Test
    void testParseInvalidHttpVersionThrowsException() {
        String rawRequest = "GET /hello HTTP/2.0\r\n\r\n";
        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));

        assertThrows(HttpParser.HttpParseException.class, () -> {
            HttpParser.parse(input);
        });
    }

    @Test
    void testParseRequestWithMultipleHeaders() throws IOException, HttpParser.HttpParseException {
        String rawRequest = "GET /test HTTP/1.1\r\n" +
                           "Host: localhost:8080\r\n" +
                           "User-Agent: Mozilla/5.0\r\n" +
                           "Accept: */*\r\n" +
                           "Connection: keep-alive\r\n" +
                           "\r\n";

        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpParser.parse(input);

        assertEquals("GET", request.getMethod());
        assertEquals("/test", request.getPath());
        assertEquals(4, request.getHeaders().size());
        assertEquals("keep-alive", request.getHeader("Connection"));
    }
}

