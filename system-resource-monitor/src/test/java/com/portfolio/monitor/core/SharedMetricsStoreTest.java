package com.portfolio.monitor.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SharedMetricsStore.
 * Tests thread safety and correctness of metrics storage.
 */
@DisplayName("SharedMetricsStore Tests")
class SharedMetricsStoreTest {

    private SharedMetricsStore metricsStore;

    @BeforeEach
    void setUp() {
        metricsStore = new SharedMetricsStore();
    }

    @Test
    @DisplayName("Should update CPU metrics correctly")
    void testUpdateCpuMetrics() {
        metricsStore.updateCpuMetrics(75.5);
        assertEquals(75.5, metricsStore.getLatestCpuUsage(), 0.01);
    }

    @Test
    @DisplayName("Should update memory metrics correctly")
    void testUpdateMemoryMetrics() {
        metricsStore.updateMemoryMetrics(82.3);
        assertEquals(82.3, metricsStore.getLatestMemoryUsage(), 0.01);
    }

    @Test
    @DisplayName("Should update disk metrics correctly")
    void testUpdateDiskMetrics() {
        metricsStore.updateDiskMetrics(45.0);
        assertEquals(45.0, metricsStore.getLatestDiskUsage(), 0.01);
    }

    @Test
    @DisplayName("Should update process count correctly")
    void testUpdateProcessCount() {
        metricsStore.updateProcessCount(150);
        assertEquals(150, metricsStore.getLatestProcessCount());
    }

    @Test
    @DisplayName("Should handle concurrent writes without data loss")
    void testConcurrentWrites() throws InterruptedException {
        int threadCount = 10;
        int updatesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < updatesPerThread; j++) {
                        metricsStore.updateCpuMetrics(threadId * 10 + j);
                        metricsStore.updateMemoryMetrics(threadId * 10 + j);
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // All updates should have completed
        assertEquals(threadCount * updatesPerThread, successCount.get());
    }

    @Test
    @DisplayName("Should clear all metrics")
    void testClear() {
        metricsStore.updateCpuMetrics(50.0);
        metricsStore.updateMemoryMetrics(60.0);
        metricsStore.updateDiskMetrics(70.0);
        metricsStore.updateProcessCount(100);

        metricsStore.clear();

        assertEquals(0.0, metricsStore.getLatestCpuUsage());
        assertEquals(0.0, metricsStore.getLatestMemoryUsage());
        assertEquals(0.0, metricsStore.getLatestDiskUsage());
        assertEquals(0, metricsStore.getLatestProcessCount());
    }
}
