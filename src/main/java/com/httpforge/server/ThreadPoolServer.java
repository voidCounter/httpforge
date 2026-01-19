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
    private final ThreadPoolConfig config;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ThreadPoolExecutor executorService;

    /**
     * configuration for thread pool behavior
     */
    public static class ThreadPoolConfig {
        private final int corePoolSize;
        private final int maxPoolSize;
        private final int queueSize;
        // rejection policy defines how to handle overload situations
        private final RejectionPolicy rejectionPolicy;

        public enum RejectionPolicy {
            ABORT,          // reject with 503 (fail fast)
            CALLER_RUNS,    // backpressure: slow down client by running in accept thread(the main thread)
            DISCARD_OLDEST  // drop oldest queued request, when newer request is more crucial
        }

        private ThreadPoolConfig(int corePoolSize, int maxPoolSize, int queueSize, RejectionPolicy policy) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.queueSize = queueSize;
            this.rejectionPolicy = policy;
        }

        /**
         * for I/O-bound workloads (database calls, file I/O, network requests)
         * uses many threads since they'll be mostly blocked waiting,
         * so we can utilize more threads to keep CPU busy
         */
        public static ThreadPoolConfig forIOBound() {
            int cores = Runtime.getRuntime().availableProcessors();
            int maxThreads = cores*20;
            return new ThreadPoolConfig(
                    cores * 2,         // start with moderate threads
                    maxThreads,                   // can grow to handle I/O waiting
                    maxThreads*3,
                    RejectionPolicy.CALLER_RUNS  // apply backpressure when overloaded
            );
        }

        /**
         * for CPU-bound workloads (heavy computation, data processing)
         * uses fewer threads to avoid context switching overhead
         */
        public static ThreadPoolConfig forCPUBound() {
            int cores = Runtime.getRuntime().availableProcessors();
            return new ThreadPoolConfig(
                    cores,              // one thread per core
                    cores * 2,
                    50,
                    RejectionPolicy.ABORT  // fail fast when overloaded
            );
        }

        /**
         * fixed-size pool (core = max)
         * predictable resource usage, no elastic scaling
         */
        public static ThreadPoolConfig fixed(int threads, int queueSize) {
            return new ThreadPoolConfig(
                    threads,
                    threads,            // core = max means fixed size
                    queueSize,
                    RejectionPolicy.ABORT
            );
        }

        /**
         * custom configuration for fine-tuning
         */
        public static ThreadPoolConfig custom(int corePoolSize, int maxPoolSize,
                                              int queueSize, RejectionPolicy policy) {
            return new ThreadPoolConfig(corePoolSize, maxPoolSize, queueSize, policy);
        }

        public int getCorePoolSize() { return corePoolSize; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getQueueSize() { return queueSize; }
        public RejectionPolicy getRejectionPolicy() { return rejectionPolicy; }

        public boolean isElastic() {
            return maxPoolSize > corePoolSize;
        }
    }

    public ThreadPoolServer(int port, Router router) {
        this(port, router, ThreadPoolConfig.forIOBound());
    }

    public ThreadPoolServer(int port, Router router, ThreadPoolConfig config) {
        this.port = port;
        this.router = router;
        this.config = config;
    }

    public void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);

        RejectedExecutionHandler rejectionHandler = createRejectionHandler(config.rejectionPolicy);

        executorService = new ThreadPoolExecutor(
                config.corePoolSize,
                config.maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(config.queueSize),
                rejectionHandler
        );

        printStartupInfo();

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                try {
                    executorService.submit(() -> handleRequest(clientSocket));
                } catch (RejectedExecutionException e) {
                    // only happens with AbortPolicy - send 503
                    handleOverload(clientSocket);
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }

    private RejectedExecutionHandler createRejectionHandler(ThreadPoolConfig.RejectionPolicy policy) {
        switch (policy) {
            case ABORT:
                // throws RejectedExecutionException - we handle it with 503
                return new ThreadPoolExecutor.AbortPolicy();

            case CALLER_RUNS:
                // backpressure: execute in the calling thread (accept thread)
                // This slows down accepting new connections when overloaded
                return new ThreadPoolExecutor.CallerRunsPolicy();

            case DISCARD_OLDEST:
                // drop the oldest task in queue and try again
                return new ThreadPoolExecutor.DiscardOldestPolicy();

            default:
                return new ThreadPoolExecutor.AbortPolicy();
        }
    }

    private void printStartupInfo() {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║       Thread Pool Server Started                        ║");
        System.out.println("╠═══════════════════════════════════════════════════════════╣");
        System.out.println("║ Port:              " + String.format("%-35s", port) + "║");
        System.out.println("║ Core Pool Size:    " + String.format("%-35s", config.corePoolSize) + "║");
        System.out.println("║ Max Pool Size:     " + String.format("%-35s", config.maxPoolSize) + "║");
        System.out.println("║ Queue Size:        " + String.format("%-35s", config.queueSize) + "║");
        System.out.println("║ Elastic:           " + String.format("%-35s", config.isElastic()) + "║");
        System.out.println("║ Rejection Policy:  " + String.format("%-35s", config.rejectionPolicy) + "║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
    }

    private void handleRequest(Socket socket) {
        new ConnectionHandler(socket, router).handle();
    }

    private void handleOverload(Socket socket) {
        try (socket; OutputStream out = socket.getOutputStream()) {
            HttpResponse response = HttpResponse.serviceUnavailable();
            out.write(response.toBytes());
            out.flush();
            System.out.println("Rejected request with 503 Service Unavailable");
        } catch (IOException e) {
            System.err.println("Error sending 503: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;

        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        System.err.println("Thread pool did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        System.out.println("Thread pool server stopped");
    }

    @Override
    public String getName() {
        return "Thread Pool Server (pool=" + config.corePoolSize +
                "-" + config.maxPoolSize + ", queue=" + config.queueSize + ")";
    }

    // for monitoring
    public ThreadPoolStats getStats() {
        if (executorService == null) {
            return new ThreadPoolStats(0, 0, 0, 0, 0);
        }
        return new ThreadPoolStats(
                executorService.getPoolSize(),
                executorService.getActiveCount(),
                executorService.getQueue().size(),
                executorService.getCompletedTaskCount(),
                executorService.getTaskCount()
        );
    }

    public static class ThreadPoolStats {
        public final int currentPoolSize;
        public final int activeThreads;
        public final int queuedTasks;
        public final long completedTasks;
        public final long totalTasks;

        public ThreadPoolStats(int currentPoolSize, int activeThreads, int queuedTasks,
                               long completedTasks, long totalTasks) {
            this.currentPoolSize = currentPoolSize;
            this.activeThreads = activeThreads;
            this.queuedTasks = queuedTasks;
            this.completedTasks = completedTasks;
            this.totalTasks = totalTasks;
        }

        @Override
        public String toString() {
            return String.format(
                    "Pool: %d threads (%d active), Queue: %d, Completed: %d/%d",
                    currentPoolSize, activeThreads, queuedTasks, completedTasks, totalTasks
            );
        }
    }
}