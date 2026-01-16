package com.httpforge.server;

import com.httpforge.http.HttpParser;
import com.httpforge.http.HttpRequest;
import com.httpforge.http.HttpResponse;
import com.httpforge.metrics.Metrics;
import com.httpforge.routing.Router;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * Implements persistent connections per HTTP/1.1 specification.
 */
public class ConnectionHandler {
    private static final int IDLE_TIMEOUT_MS = 5000; // 5 seconds

    private final Router router;
    private final Socket socket;

    public ConnectionHandler(Socket socket, Router router) {
        this.socket = socket;
        this.router = router;
    }

    /**
     * Parse → Route → Respond → Check Keep-Alive → Repeat or Close
     */
    public void handle() {
        Metrics metrics = Metrics.getInstance();

        try {
            socket.setSoTimeout(IDLE_TIMEOUT_MS); // idle timeout for keep-alive
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            boolean keepAlive = true;
            while (keepAlive) {
                try {
                    // parse request - don't start timing until we have a valid request
                    HttpRequest request = HttpParser.parse(in);

                    long startTime = System.currentTimeMillis();
                    metrics.recordRequestStart();

                    // Check if client wants to keep connection alive
                    String connectionHeader = request.getHeader("Connection");
                    boolean clientWantsKeepAlive = connectionHeader != null &&
                                                   connectionHeader.equalsIgnoreCase("keep-alive");

                    HttpResponse response = router.route(request);

                    // Add Connection header to response based on client's request
                    if (clientWantsKeepAlive) {
                        response = addConnectionHeader(response, "keep-alive");
                    } else {
                        response = addConnectionHeader(response, "close");
                        keepAlive = false;
                    }

                    out.write(response.toBytes());
                    out.flush();

                    // Record successful request completion
                    long duration = System.currentTimeMillis() - startTime;
                    metrics.recordRequestEnd(duration);

                    // Log request details: method, path, status code, duration
                    logRequest(request, response, duration);

                } catch (java.net.SocketTimeoutException e) {
                    // Idle timeout reached, close connection
                    // don't record metrics - this is just an idle keep-alive timeout
                    System.out.println("Connection idle timeout, closing");
                    keepAlive = false;
                } catch (HttpParser.HttpParseException e) {
                    // parse error - don't record metrics, this isn't a valid request
                    System.err.println("Parse error: " + e.getMessage());
                    keepAlive = false;
                } catch (IOException e) {
                    // Connection closed by client or other I/O error
                    // Don't record metrics - this is a connection failure
                    keepAlive = false;
                }
            }
        } catch (IOException e) {
            System.err.println("Error setting up connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private HttpResponse addConnectionHeader(HttpResponse response, String value) {
        Map<String, String> headers = response.getHeaders();
        headers.put("Connection", value);
        return new HttpResponse(
            response.getStatusCode(),
            response.getReasonPhrase(),
            headers,
            response.getBody()
        );
    }

    /**
     * log req
     * Format: [METHOD] /path -> STATUS (duration ms)
     */
    private void logRequest(HttpRequest request, HttpResponse response, long durationMs) {
        System.out.printf("[%s] %s -> %d %s (%dms)%n",
            request.getMethod(),
            request.getPath(),
            response.getStatusCode(),
            response.getReasonPhrase(),
            durationMs
        );
    }
}

