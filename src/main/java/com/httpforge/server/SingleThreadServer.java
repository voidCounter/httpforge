package com.httpforge.server;

import com.httpforge.routing.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleThreadServer implements ServerStrategy {
    private final int port;
    private final Router router;
    // running flag is volatile, because it may be accessed by multiple threads
    // volatile ensures visibility of updates across threads. so changes made in stop() are seen by the start() loop
    private volatile boolean running = false;
    // ServerSocket listens on a port and accepts new connections
    // it's different from regular Socket, which represents a single connection
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
                // internally, a file descriptor is binded to the port
                // and it has a backlog queue of incoming connections
                // when a client connects(SYN -> SYC+ACK -> ACK), accept() returns a new Socket for that connection
                // but the serverSocket continues to listen for new connections
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

