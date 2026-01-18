package com.httpforge;

import com.httpforge.http.HttpResponse;
import com.httpforge.metrics.Metrics;
import com.httpforge.routing.Router;
import com.httpforge.server.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        Router router = new Router();

        // routes
        router.addRoute("GET", "/", request ->
            HttpResponse.ok("Welcome to HTTPForge!\n")
        );

        router.addRoute("GET", "/hello", request -> {
            try { Thread.sleep(20); } catch (InterruptedException ignored) {} // Simulate a DB call
            return HttpResponse.ok("Hello, World!\n");
        });

        router.addRoute("GET", "/echo", request -> {
            String body = "Method: " + request.getMethod() + "\n" +
                         "Path: " + request.getPath() + "\n" +
                         "Headers: " + request.getHeaders().size() + "\n";
            return HttpResponse.ok(body);
        });

        router.addRoute("POST", "/data", request -> {
            String body = "Received POST data:\n" + request.getBody();
            return HttpResponse.ok(body);
        });

        router.addRoute("GET", "/metrics", request -> {
            Metrics metrics = Metrics.getInstance();

            // Build JSON manually (avoiding external dependencies)
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"totalRequests\": ").append(metrics.getTotalRequests()).append(",\n");
            json.append("  \"activeConnections\": ").append(metrics.getActiveConnections()).append(",\n");
            json.append("  \"latency\": {\n");
            json.append("    \"min\": ").append(String.format("%.2f", metrics.getMinLatency())).append(",\n");
            json.append("    \"max\": ").append(String.format("%.2f", metrics.getMaxLatency())).append(",\n");
            json.append("    \"avg\": ").append(String.format("%.2f", metrics.getAverageLatency())).append(",\n");
            json.append("    \"p50\": ").append(String.format("%.2f", metrics.getLatencyPercentile(50))).append(",\n");
            json.append("    \"p95\": ").append(String.format("%.2f", metrics.getLatencyPercentile(95))).append(",\n");
            json.append("    \"p99\": ").append(String.format("%.2f", metrics.getLatencyPercentile(99))).append("\n");
            json.append("  }\n");
            json.append("}");

            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            return new HttpResponse(200, "OK", headers, json.toString());
        });

        int port = 8080;

        // Choose server type from command line or default to thread
        String serverType = args.length > 0 ? args[0].toLowerCase() : "thread";
        ServerStrategy server;

        switch (serverType) {
            case "single":
                server = new SingleThreadServer(port, router);
                break;
            case "thread":
                server = new ThreadPerRequestServer(port, router);
                break;
            case "pool":
                server = new ThreadPoolServer(port, router);
                break;
            case "nio":
                server = new NioServer(port, router);
                break;
            default:
                System.err.println("Unknown server type: " + serverType);
                System.err.println("Usage: java Main [single|thread|pool|nio]");
                System.exit(1);
                return;
        }

        System.out.println("Starting: " + server.getName());

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop();
        }));

        server.start();
    }
}
