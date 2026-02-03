package com.portfolio.monitor.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SystemMetrics.
 * Tests builder pattern and immutability.
 */
@DisplayName("SystemMetrics Tests")
class SystemMetricsTest {

    @Test
    @DisplayName("Should build metrics with all values")
    void testBuilderWithAllValues() {
        Instant now = Instant.now();
        Map<String, Long> processes = new ConcurrentHashMap<>();
        processes.put("java", 1024L);
        processes.put("chrome", 2048L);

        SystemMetrics metrics = new SystemMetrics.Builder()
                .timestamp(now)
                .cpuUsage(75.5)
                .totalMemory(16_000_000_000L)
                .usedMemory(8_000_000_000L)
                .freeMemory(8_000_000_000L)
                .memoryUsagePercent(50.0)
                .totalDiskSpace(500_000_000_000L)
                .usedDiskSpace(250_000_000_000L)
                .freeDiskSpace(250_000_000_000L)
                .diskUsagePercent(50.0)
                .processCount(150)
                .topProcesses(processes)
                .build();

        assertEquals(now, metrics.getTimestamp());
        assertEquals(75.5, metrics.getCpuUsage(), 0.01);
        assertEquals(16_000_000_000L, metrics.getTotalMemory());
        assertEquals(8_000_000_000L, metrics.getUsedMemory());
        assertEquals(8_000_000_000L, metrics.getFreeMemory());
        assertEquals(50.0, metrics.getMemoryUsagePercent(), 0.01);
        assertEquals(500_000_000_000L, metrics.getTotalDiskSpace());
        assertEquals(250_000_000_000L, metrics.getUsedDiskSpace());
        assertEquals(250_000_000_000L, metrics.getFreeDiskSpace());
        assertEquals(50.0, metrics.getDiskUsagePercent(), 0.01);
        assertEquals(150, metrics.getProcessCount());
        assertEquals(2, metrics.getTopProcesses().size());
    }

    @Test
    @DisplayName("Should return defensive copy of top processes")
    void testDefensiveCopy() {
        Map<String, Long> processes = new ConcurrentHashMap<>();
        processes.put("java", 1024L);

        SystemMetrics metrics = new SystemMetrics.Builder()
                .topProcesses(processes)
                .build();

        // Modify the returned map
        Map<String, Long> returnedProcesses = metrics.getTopProcesses();
        returnedProcesses.put("hacker", 9999L);

        // Original should be unchanged
        assertFalse(metrics.getTopProcesses().containsKey("hacker"));
    }

    @Test
    @DisplayName("Should generate readable toString")
    void testToString() {
        SystemMetrics metrics = new SystemMetrics.Builder()
                .cpuUsage(75.5)
                .memoryUsagePercent(50.0)
                .diskUsagePercent(25.0)
                .processCount(100)
                .build();

        String str = metrics.toString();
        assertTrue(str.contains("75.50%"));
        assertTrue(str.contains("50.00%"));
        assertTrue(str.contains("25.00%"));
        assertTrue(str.contains("100"));
    }

    @Test
    @DisplayName("Should use current timestamp by default")
    void testDefaultTimestamp() {
        Instant before = Instant.now();
        SystemMetrics metrics = new SystemMetrics.Builder().build();
        Instant after = Instant.now();

        assertNotNull(metrics.getTimestamp());
        assertTrue(!metrics.getTimestamp().isBefore(before));
        assertTrue(!metrics.getTimestamp().isAfter(after));
    }
}
