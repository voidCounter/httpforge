package com.httpforge.routing;

import com.httpforge.http.HttpRequest;
import com.httpforge.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Router {
    // using Function functional interface to represent route handlers
    private final Map<String, Function<HttpRequest, HttpResponse>> routes = new HashMap<>();

    /**
     * register a route handler for a specific HTTP method and path.
     *
     * @param method HTTP method (GET, POST, etc.)
     * @param path   Request path
     * @param handler Function that processes the request and returns a response
     */
    public void addRoute(String method, String path, Function<HttpRequest, HttpResponse> handler) {
        String key = routeKey(method, path);
        routes.put(key, handler);
    }

    /**
     * Route an incoming request to the appropriate handler.
     * Returns 404 if no matching route is found.
     *
     * @param request The HTTP request to route
     * @return HTTP response from the handler or 404
     */
    public HttpResponse route(HttpRequest request) {
        String key = routeKey(request.getMethod(), request.getPath());
        Function<HttpRequest, HttpResponse> handler = routes.get(key);

        if (handler != null) {
            return handler.apply(request);
        }

        return notFound(request.getPath());
    }

    private String routeKey(String method, String path) {
        return method.toUpperCase() + " " + path;
    }

    /**
     * Default 404 handler.
     */
    private HttpResponse notFound(String path) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/plain");

        String body = "404 Not Found: " + path;

        return new HttpResponse(404, "Not Found", headers, body);
    }
}

