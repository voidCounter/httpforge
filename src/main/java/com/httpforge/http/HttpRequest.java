package com.httpforge.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.body = body != null ? body : "";
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public String getHeader(String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return String.format("HttpRequest{method='%s', path='%s', headers=%d, bodyLength=%d}",
                method, path, headers.size(), body.length());
    }
}
