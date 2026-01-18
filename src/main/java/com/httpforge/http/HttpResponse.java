package com.httpforge.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * every HTTP response sent by the server is represented by this class.
 * every response has a status code (200, 404, etc), reason phrase (OK, Not Found, etc), headers, and an optional body.
 * for example, a typical HTTP response might look like this:
 */
public class HttpResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(int statusCode, String reasonPhrase, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.body = body != null ? body : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        // return a soft copy to prevent external modification
        return new HashMap<>(headers);
    }

    public String getBody() {
        return body;
    }

    /**
     * serializes the HTTP response to raw bytes following HTTP/1.1 specification.
     * format:
     * HTTP/1.1 {statusCode} {reasonPhrase}\r\n
     * {Header-Name}: {Header-Value}\r\n
     * ...
     * \r\n
     * {body}
     */
    public byte[] toBytes() {
        StringBuilder response = new StringBuilder();
        
        // status line
        response.append("HTTP/1.1 ")
                .append(statusCode)
                .append(" ")
                .append(reasonPhrase)
                .append("\r\n"); // \r\n means new line in HTTP or CRLF (Carriage Return Line Feed)
        
        // headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        
        // empty line separating headers from body
        response.append("\r\n");
        
        response.append(body);

        // tcp/ip works with bytes, not strings
        return response.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return String.format("HttpResponse{statusCode=%d, reasonPhrase='%s', headers=%d, bodyLength=%d}",
                statusCode, reasonPhrase, headers.size(), body.length());
    }

    /**
     * builder pattern for constructing HttpResponse instances.
     * allows for flexible and readable response creation.
     * example usage:
     * HttpResponse response = HttpResponse.builder()
     *     .status(200, "OK")
     *     .header("Content-Type", "text/plain")
     *     .body("Hello, world!")
     *     .build();
     */
    public static class Builder {
        private int statusCode = 200;
        private String reasonPhrase = "OK";
        private Map<String, String> headers = new HashMap<>();
        private String body = "";

        public Builder status(int code, String phrase) {
            this.statusCode = code;
            this.reasonPhrase = phrase;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            if (body != null && !body.isEmpty()) {
                // here we're calculating the byte length of the body in UTF-8 encoding
                // lets say the body is "hello, 世界"("hello, " + "world" in Chinese)
                // "hello, " is 7 bytes, "世" is 3 bytes, "界" is 3 bytes
                // total 13 bytes, so Content-Length should be 13.
                this.headers.put("Content-Length", String.valueOf(body.getBytes(StandardCharsets.UTF_8).length));

            }
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(statusCode, reasonPhrase, headers, body);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // some common responses for convenience
    public static HttpResponse ok(String body) {
        return builder()
                .status(200, "OK")
                .header("Content-Type", "text/plain")
                .body(body)
                .build();
    }

    public static HttpResponse notFound() {
        return builder()
                .status(404, "Not Found")
                .header("Content-Type", "text/plain")
                .body("404 Not Found")
                .build();
    }

    public static HttpResponse internalServerError() {
        return builder()
                .status(500, "Internal Server Error")
                .header("Content-Type", "text/plain")
                .body("500 Internal Server Error")
                .build();
    }

    public static HttpResponse serviceUnavailable() {
        return builder()
                .status(503, "Service Unavailable")
                .header("Content-Type", "text/plain")
                .body("503 Service Unavailable")
                .build();
    }
}

