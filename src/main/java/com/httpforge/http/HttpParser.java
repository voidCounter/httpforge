package com.httpforge.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple HTTP request parser that reads from an InputStream and constructs an HttpRequest object.
 * It supports parsing the request line, headers, and body (if present).
 * an example of a valid HTTP request:
 * GET /index.html HTTP/1.1
 * Host: www.example.com
 * User-Agent: Mozilla/5.0
 * Content-Length: 13
 *
 * Hello, world!
 *
 */
public class HttpParser {

    /**
     * Parses an HTTP request from an input stream.
     *
     * @param inputStream the input stream containing the HTTP request
     * @return parsed HttpRequest object
     * @throws IOException if I/O error occurs
     * @throws HttpParseException if the request is malformed
     */
    public static HttpRequest parse(InputStream inputStream) throws IOException, HttpParseException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) {
            throw new HttpParseException("Empty request line");
        }

        // expecting exactly 3 parts: METHOD PATH HTTP_VERSION
        // e.g., "GET /index.html HTTP/1.1"
        String[] requestParts = requestLine.split("\\s+");
        if (requestParts.length != 3) {
            throw new HttpParseException("Invalid request line format: " + requestLine);
        }

        String method = requestParts[0].toUpperCase();
        String path = requestParts[1];
        String httpVersion = requestParts[2];

        // validate HTTP version
        if (!httpVersion.startsWith("HTTP/1.1")) {
            throw new HttpParseException("Invalid HTTP version: " + httpVersion);
        }

        // parse headers
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                // empty line indicates end of headers
                break;
            }

            int colonIndex = line.indexOf(':');
            if (colonIndex == -1) {
                throw new HttpParseException("Invalid header format: " + line);
            }

            String headerName = line.substring(0, colonIndex).trim();
            String headerValue = line.substring(colonIndex + 1).trim();

            // we've getHeaderCaseInsensitive to handle case-insensitivity of HTTP headers
            headers.put(headerName, headerValue);
        }

        // Parse body (if present)
        String body = "";
        String contentLengthStr = getHeaderCaseInsensitive(headers, "Content-Length");

        if (contentLengthStr != null) {
            try {
                // content length indicates how many bytes to read for the body
                int contentLength = Integer.parseInt(contentLengthStr);
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int totalRead = 0;
                    while (totalRead < contentLength) {
                        // IMPORTANT: read may return less than requested, so we loop until we read enough
                        // thh source of SLOWLORIS attacks
                        // it happens when the client sends data very slowly, so we must keep reading until we get all the expected bytes
                        int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                        if (read == -1) {
                            throw new HttpParseException("Unexpected end of stream while reading body");
                        }
                        totalRead += read;
                    }
                    body = new String(bodyChars);
                }
            } catch (NumberFormatException e) {
                throw new HttpParseException("Invalid Content-Length value: " + contentLengthStr);
            }
        }

        return new HttpRequest(method, path, headers, body);
    }

    /**
     * retrieves header value in a case-insensitive manner.
     * HTTP header names are case-insensitive per RFC 7230.
     */
    private static String getHeaderCaseInsensitive(Map<String, String> headers, String headerName) {
        // we could have just lowercased all headers when parsing, but this is more general approach
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(headerName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static class HttpParseException extends Exception {
        public HttpParseException(String message) {
            super(message);
        }

        public HttpParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

