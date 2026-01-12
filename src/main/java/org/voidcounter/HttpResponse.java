package org.voidcounter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
                .append("\r\n");
        
        // headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            response.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        
        // empty line separating headers from body
        response.append("\r\n");
        
        // body
        response.append(body);
        
        return response.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return String.format("HttpResponse{statusCode=%d, reasonPhrase='%s', headers=%d, bodyLength=%d}",
                statusCode, reasonPhrase, headers.size(), body.length());
    }

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

