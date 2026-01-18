package com.httpforge.server;

import com.httpforge.http.HttpResponse;
import com.httpforge.routing.Router;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class ThreadPoolServer implements ServerStrategy {
    private final int port;
    private final Router router;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public ThreadPoolServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);

        // here we're setting pool size to double the number of available processors
        int poolSize = Runtime.getRuntime().availableProcessors() * 2;
        // queue size determines how many requests can wait when all threads are busy
        int queueSize = 100;

        executorService = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new ThreadPoolExecutor.AbortPolicy() // reject when overloaded
        );

        System.out.println(">> Thread pool server started on port " + port + " (pool=" + poolSize + ", queue=" + queueSize + ")");

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                try {
                    // submit the request handling to the thread pool
                    executorService.submit(() -> handleRequest(clientSocket));
                } catch (RejectedExecutionException e) {
                    // when the pool is overloaded, we reject the request with 503
                    handleOverload(clientSocket);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private void handleRequest(Socket socket) {
        new ConnectionHandler(socket, router).handle();
    }

    private void handleOverload(Socket socket) {
        // using try-with-resources to ensure socket is closed after sending response
        try (socket;
             OutputStream out = socket.getOutputStream()) {

            HttpResponse response = HttpResponse.serviceUnavailable();
            out.write(response.toBytes());
            out.flush();

            System.out.println("Rejected overload request with 503");

        } catch (IOException e) {
            System.err.println("Error sending 503: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "Thread Pool Server";
    }
}
