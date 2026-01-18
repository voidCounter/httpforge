package com.httpforge.server;

import com.httpforge.routing.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ThreadPerRequestServer implements ServerStrategy {
    private final int port;
    private final Router router;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public ThreadPerRequestServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);
        System.out.println(">> Thread-per-request server started on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // we spawn a new thread for each incoming connection
                new Thread(() -> handleRequest(clientSocket)).start();
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

    public void stop() {
        running = false;
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
        return "Thread-per-request Server";
    }
}
