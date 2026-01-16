package com.httpforge.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * thread-safe singleton for tracking global server metrics.
 * Tracks requests, active connections, and latency statistics.
 */
public class Metrics {
    private static final Metrics INSTANCE = new Metrics();

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final List<Long> requestDurations = Collections.synchronizedList(new ArrayList<>());

    // Keep last N request durations for percentile calculation
    private static final int MAX_DURATION_SAMPLES = 10000;

    private Metrics() {
        // Private constructor for singleton
    }

    public static Metrics getInstance() {
        return INSTANCE;
    }

    /**
     * Records the start of a new request.
     */
    public void recordRequestStart() {
        totalRequests.incrementAndGet();
        activeConnections.incrementAndGet();
    }

    /**
     * Records the completion of a request with its duration.
     * @param durationMs Request duration in milliseconds
     */
    public void recordRequestEnd(long durationMs) {
        activeConnections.decrementAndGet();

        // Keep a limited history to avoid memory issues
        synchronized (requestDurations) {
            if (requestDurations.size() >= MAX_DURATION_SAMPLES) {
                requestDurations.remove(0);
            }
            requestDurations.add(durationMs);
        }
    }

    /**
     * Gets total number of requests processed.
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }

    /**
     * Gets current number of active connections.
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * Calculates the specified percentile of request durations.
     * @param percentile Value between 0 and 100
     * @return Duration in milliseconds at the specified percentile
     */
    public double getLatencyPercentile(int percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }

        synchronized (requestDurations) {
            if (requestDurations.isEmpty()) {
                return 0.0;
            }

            List<Long> sorted = new ArrayList<>(requestDurations);
            Collections.sort(sorted);

            int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
            if (index < 0) {
                index = 0;
            }

            return sorted.get(index);
        }
    }

    /**
     * Calculates average request duration.
     * @return Average duration in milliseconds
     */
    public double getAverageLatency() {
        synchronized (requestDurations) {
            if (requestDurations.isEmpty()) {
                return 0.0;
            }

            long sum = 0;
            for (Long duration : requestDurations) {
                sum += duration;
            }

            return (double) sum / requestDurations.size();
        }
    }

    /**
     * Gets minimum request duration.
     * @return Minimum duration in milliseconds
     */
    public double getMinLatency() {
        synchronized (requestDurations) {
            if (requestDurations.isEmpty()) {
                return 0.0;
            }
            return Collections.min(requestDurations);
        }
    }

    /**
     * Gets maximum request duration.
     * @return Maximum duration in milliseconds
     */
    public double getMaxLatency() {
        synchronized (requestDurations) {
            if (requestDurations.isEmpty()) {
                return 0.0;
            }
            return Collections.max(requestDurations);
        }
    }

    /**
     * Resets all metrics (useful for testing).
     */
    public void reset() {
        totalRequests.set(0);
        activeConnections.set(0);
        synchronized (requestDurations) {
            requestDurations.clear();
        }
    }
}

