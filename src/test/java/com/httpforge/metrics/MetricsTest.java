package com.httpforge.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricsTest {

    @BeforeEach
    void setUp() {
        // Reset metrics before each test
        Metrics.getInstance().reset();
    }

    @Test
    void testInitialMetricsAreZero() {
        Metrics metrics = Metrics.getInstance();

        assertEquals(0, metrics.getTotalRequests());
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(0.0, metrics.getAverageLatency());
    }

    @Test
    void testRecordSingleRequest() {
        Metrics metrics = Metrics.getInstance();

        metrics.recordRequestStart();
        assertEquals(1, metrics.getTotalRequests());
        assertEquals(1, metrics.getActiveConnections());

        metrics.recordRequestEnd(10);
        assertEquals(1, metrics.getTotalRequests());
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(10.0, metrics.getAverageLatency());
    }

    @Test
    void testRecordMultipleRequests() {
        Metrics metrics = Metrics.getInstance();

        // Record 3 requests with different durations
        metrics.recordRequestStart();
        metrics.recordRequestEnd(10);

        metrics.recordRequestStart();
        metrics.recordRequestEnd(20);

        metrics.recordRequestStart();
        metrics.recordRequestEnd(30);

        assertEquals(3, metrics.getTotalRequests());
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(20.0, metrics.getAverageLatency()); // (10+20+30)/3
        assertEquals(10.0, metrics.getMinLatency());
        assertEquals(30.0, metrics.getMaxLatency());
    }

    @Test
    void testLatencyPercentiles() {
        Metrics metrics = Metrics.getInstance();

        // Record 100 requests with durations 1-100ms
        for (int i = 1; i <= 100; i++) {
            metrics.recordRequestStart();
            metrics.recordRequestEnd(i);
        }

        assertEquals(100, metrics.getTotalRequests());

        // Test percentiles
        assertEquals(50.0, metrics.getLatencyPercentile(50), 1.0); // p50 ~50ms
        assertEquals(95.0, metrics.getLatencyPercentile(95), 1.0); // p95 ~95ms
        assertEquals(99.0, metrics.getLatencyPercentile(99), 1.0); // p99 ~99ms
    }

    @Test
    void testActiveConnectionsTracking() {
        Metrics metrics = Metrics.getInstance();

        metrics.recordRequestStart();
        metrics.recordRequestStart();
        metrics.recordRequestStart();

        assertEquals(3, metrics.getActiveConnections());

        metrics.recordRequestEnd(10);
        assertEquals(2, metrics.getActiveConnections());

        metrics.recordRequestEnd(10);
        metrics.recordRequestEnd(10);
        assertEquals(0, metrics.getActiveConnections());
    }

    @Test
    void testPercentileWithEmptyData() {
        Metrics metrics = Metrics.getInstance();

        assertEquals(0.0, metrics.getLatencyPercentile(50));
        assertEquals(0.0, metrics.getLatencyPercentile(95));
        assertEquals(0.0, metrics.getLatencyPercentile(99));
    }

    @Test
    void testPercentileInvalidArguments() {
        Metrics metrics = Metrics.getInstance();

        assertThrows(IllegalArgumentException.class, () -> {
            metrics.getLatencyPercentile(-1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            metrics.getLatencyPercentile(101);
        });
    }

    @Test
    void testConcurrentRequests() throws InterruptedException {
        Metrics metrics = Metrics.getInstance();

        // Simulate 10 concurrent threads making requests
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final int duration = (i + 1) * 10;
            threads[i] = new Thread(() -> {
                metrics.recordRequestStart();
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                metrics.recordRequestEnd(duration);
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(10, metrics.getTotalRequests());
        assertEquals(0, metrics.getActiveConnections());
        assertTrue(metrics.getAverageLatency() > 0);
    }

    @Test
    void testResetMetrics() {
        Metrics metrics = Metrics.getInstance();

        metrics.recordRequestStart();
        metrics.recordRequestEnd(10);

        assertEquals(1, metrics.getTotalRequests());

        metrics.reset();

        assertEquals(0, metrics.getTotalRequests());
        assertEquals(0, metrics.getActiveConnections());
        assertEquals(0.0, metrics.getAverageLatency());
    }
}

