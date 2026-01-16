package com.httpforge.server;

import com.httpforge.http.HttpParser;
import com.httpforge.http.HttpRequest;
import com.httpforge.http.HttpResponse;
import com.httpforge.metrics.Metrics;
import com.httpforge.routing.Router;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Non-blocking HTTP server using Java NIO and the Reactor pattern.
 * Single-threaded event loop handles all I/O operations asynchronously.
 */
public class NioServer implements ServerStrategy {
    private final int port;
    private final Router router;
    private volatile boolean running = false;
    private Selector selector;
    private ServerSocketChannel serverChannel;

    // Worker thread pool for offloading request processing from I/O thread
    // This allows the I/O thread to stay responsive and handle multiple concurrent requests
    private final ExecutorService workerPool;

    // Buffer size for reading requests
    private static final int BUFFER_SIZE = 8192;

    public NioServer(int port, Router router) {
        this.port = port;
        this.router = router;
        // Create worker pool - sized for handling blocking operations
        int workerThreads = Runtime.getRuntime().availableProcessors() * 4;
        this.workerPool = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void start() throws IOException {
        running = true;
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new java.net.InetSocketAddress(port));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println(">> NIO server started on port " + port + " (Reactor pattern)");

        // Event loop
        while (running) {
            try {
                // Block until at least one channel is ready
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        } else if (key.isWritable()) {
                            handleWrite(key);
                        }
                    } catch (IOException e) {
                        System.err.println("Error handling key: " + e.getMessage());
                        closeChannel(key);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error in event loop: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handles new incoming connections.
     */
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            // Register for read operations, attach a new connection context
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new ConnectionContext());
            System.out.println("Accepted connection from: " + clientChannel.getRemoteAddress());
        }
    }

    /**
     * Handles readable channels - reads HTTP request data.
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectionContext context = (ConnectionContext) key.attachment();

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            // Client closed connection
            closeChannel(key);
            return;
        }

        if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            context.appendRequest(data);

            // Try to parse the request
            if (context.isRequestComplete()) {
                processRequest(key, context);
            }
        }
    }

    /**
     * Processes a complete HTTP request and prepares the response.
     *
     * Architecture note: We use a two-phase approach:
     * 1. Completeness Check (Hot Path): ConnectionContext.isRequestComplete()
     *    - Runs on every OP_READ event (many times per request)
     *    - Uses optimized byte-level operations to avoid String allocations
     *    - Caches parsed values (header position, content-length)
     *
     * 2. Request Parsing (Cold Path): HttpParser.parse()
     *    - Runs once when request is complete
     *    - Reuses existing, well-tested parsing logic
     *    - Works with ByteArrayInputStream created from buffered data
     *
     * This separation optimizes the hot path while maintaining code reuse.
     *
     * Worker Thread Pattern:
     * The I/O thread (Selector loop) only handles I/O operations.
     * Request processing (routing, business logic) is offloaded to worker threads.
     * This keeps the I/O thread responsive and allows handling many concurrent requests.
     */
    private void processRequest(SelectionKey key, ConnectionContext context) {
        // Offload processing to worker thread - don't block the I/O thread!
        workerPool.submit(() -> {
            Metrics metrics = Metrics.getInstance();
            long startTime = System.currentTimeMillis();

            try {
                // Parse HTTP request
                ByteArrayInputStream inputStream = new ByteArrayInputStream(context.getRequestData());
                HttpRequest request = HttpParser.parse(inputStream);

                metrics.recordRequestStart();

                // Route request to handler (this may take time, e.g., database queries, sleep)
                // But we're on a worker thread, so the I/O thread stays responsive!
                HttpResponse response = router.route(request);

                // Add Connection: close header (simplification - no keep-alive for NIO yet)
                response = addConnectionHeader(response, "close");

                // Convert response to bytes
                byte[] responseBytes = response.toBytes();
                context.setResponse(responseBytes);

                // Record metrics
                long duration = System.currentTimeMillis() - startTime;
                metrics.recordRequestEnd(duration);

                // Log request
                logRequest(request, response, duration);

                // Switch to write mode and wake up the selector
                // (We're on a worker thread, so we must wake the selector to notice the change)
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();

            } catch (HttpParser.HttpParseException e) {
                System.err.println("Parse error: " + e.getMessage());
                closeChannel(key);
                selector.wakeup();
            } catch (IOException e) {
                System.err.println("I/O error processing request: " + e.getMessage());
                closeChannel(key);
                selector.wakeup();
            }
        });
    }

    /**
     * Handles writable channels - writes HTTP response data.
     */
    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectionContext context = (ConnectionContext) key.attachment();

        ByteBuffer buffer = context.getResponseBuffer();
        channel.write(buffer);

        if (!buffer.hasRemaining()) {
            // Response fully written, close connection
            // (In a real implementation, we'd check for keep-alive here)
            closeChannel(key);
        }
    }

    /**
     * Closes a channel and cancels its key.
     */
    private void closeChannel(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException e) {
            System.err.println("Error closing channel: " + e.getMessage());
        }
    }

    /**
     * Adds Connection header to response.
     */
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
     * Logs request details.
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

    @Override
    public void stop() {
        running = false;

        // Shutdown worker pool
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Close selector and server channel
        try {
            if (selector != null && selector.isOpen()) {
                selector.wakeup();
                selector.close();
            }
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        } catch (IOException e) {
            System.err.println("Error stopping NIO server: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "NIO Event-Driven Server (Reactor)";
    }

    /**
     * Context object attached to each client connection.
     * Stores partial request data and response buffer.
     * Optimized to work with byte arrays directly to avoid String allocation overhead.
     */
    private static class ConnectionContext {
        private ByteBuffer requestBuffer = ByteBuffer.allocate(BUFFER_SIZE * 2);
        private ByteBuffer responseBuffer;
        private int contentLength = -1; // -1 means not yet parsed
        private int headerEndPos = -1;  // -1 means not yet found

        public void appendRequest(byte[] data) {
            // Expand buffer if needed
            if (requestBuffer.remaining() < data.length) {
                ByteBuffer newBuffer = ByteBuffer.allocate(requestBuffer.capacity() * 2);
                requestBuffer.flip();
                newBuffer.put(requestBuffer);
                requestBuffer = newBuffer;
            }
            requestBuffer.put(data);
        }

        public byte[] getRequestData() {
            byte[] data = new byte[requestBuffer.position()];
            requestBuffer.flip();
            requestBuffer.get(data);
            requestBuffer.rewind(); // Reset for potential reuse
            return data;
        }

        /**
         * Checks if we've received a complete HTTP request.
         * Works directly with bytes to avoid String allocation overhead.
         * Uses Boyer-Moore-style pattern matching for efficiency.
         */
        public boolean isRequestComplete() {
            int position = requestBuffer.position();
            if (position < 4) {
                return false; // Need at least "\r\n\r\n"
            }

            // Find end of headers (\r\n\r\n) if not already found
            if (headerEndPos == -1) {
                headerEndPos = findHeaderEnd(requestBuffer, position);
                if (headerEndPos == -1) {
                    return false; // Headers not complete yet
                }
            }

            // Parse Content-Length if not already parsed
            if (contentLength == -1) {
                contentLength = extractContentLength(requestBuffer, headerEndPos);
            }

            // Check if we have the complete body
            int bodyStart = headerEndPos + 4; // Skip \r\n\r\n
            int bodyReceived = position - bodyStart;
            return bodyReceived >= contentLength;
        }

        /**
         * Finds the position of "\r\n\r\n" in the buffer.
         * Returns -1 if not found.
         */
        private int findHeaderEnd(ByteBuffer buffer, int limit) {
            byte[] pattern = {'\r', '\n', '\r', '\n'};
            for (int i = 0; i <= limit - 4; i++) {
                if (buffer.get(i) == pattern[0] &&
                    buffer.get(i + 1) == pattern[1] &&
                    buffer.get(i + 2) == pattern[2] &&
                    buffer.get(i + 3) == pattern[3]) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Extracts Content-Length from headers.
         * Works directly with bytes to avoid String allocations.
         * Returns 0 if Content-Length header is not found.
         */
        private int extractContentLength(ByteBuffer buffer, int headerEnd) {
            // Pattern: "Content-Length:" (case-insensitive)
            byte[] clPattern = "content-length:".getBytes(StandardCharsets.UTF_8);

            for (int i = 0; i < headerEnd - clPattern.length; i++) {
                // Check if this position matches "content-length:" (case-insensitive)
                boolean matches = true;
                for (int j = 0; j < clPattern.length; j++) {
                    byte b = buffer.get(i + j);
                    byte p = clPattern[j];
                    // Case-insensitive comparison
                    if (Character.toLowerCase(b) != p) {
                        matches = false;
                        break;
                    }
                }

                if (matches) {
                    // Found Content-Length header, parse the value
                    int valueStart = i + clPattern.length;

                    // Skip whitespace
                    while (valueStart < headerEnd && (buffer.get(valueStart) == ' ' || buffer.get(valueStart) == '\t')) {
                        valueStart++;
                    }

                    // Find end of line
                    int valueEnd = valueStart;
                    while (valueEnd < headerEnd && buffer.get(valueEnd) != '\r' && buffer.get(valueEnd) != '\n') {
                        valueEnd++;
                    }

                    // Parse integer from bytes
                    int length = 0;
                    for (int pos = valueStart; pos < valueEnd; pos++) {
                        byte digit = buffer.get(pos);
                        if (digit >= '0' && digit <= '9') {
                            length = length * 10 + (digit - '0');
                        } else {
                            // Invalid digit, stop parsing
                            break;
                        }
                    }
                    return length;
                }
            }

            return 0; // No Content-Length header found
        }

        public void setResponse(byte[] data) {
            this.responseBuffer = ByteBuffer.wrap(data);
        }

        public ByteBuffer getResponseBuffer() {
            return responseBuffer;
        }
    }
}

