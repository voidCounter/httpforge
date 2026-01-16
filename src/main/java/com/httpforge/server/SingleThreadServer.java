package com.httpforge.server;

import com.httpforge.routing.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleThreadServer implements ServerStrategy {
    private final int port;
    private final Router router;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public SingleThreadServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);
        System.out.println(">> Single-threaded server started on port " + port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handles HTTP requests with Keep-Alive support.
     */
    private void handleRequest(Socket socket) {
        new ConnectionHandler(socket, router).handle();
    }

    /**
     * stops the server gracefully.
     */
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
        return "Single-threaded Server";
    }
}

