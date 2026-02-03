package com.portfolio.monitor.core;

import com.portfolio.monitor.model.SystemMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe centralized storage for system metrics.
 * Uses ReentrantReadWriteLock for optimal read-heavy workloads.
 * 
 * Key Concurrency Concepts:
 * - ReentrantReadWriteLock: Allows multiple readers OR one writer
 * - ConcurrentHashMap: Thread-safe map for metric history
 * - Volatile: Ensures visibility of latest metrics across threads
 */
public class SharedMetricsStore {

    private static final Logger logger = LoggerFactory.getLogger(SharedMetricsStore.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    // Volatile ensures visibility across threads
    private volatile double latestCpuUsage;
    private volatile double latestMemoryUsage;
    private volatile double latestDiskUsage;
    private volatile int latestProcessCount;
    private volatile Instant lastUpdateTime;

    // ConcurrentHashMap for thread-safe history storage
    private final ConcurrentHashMap<String, SystemMetrics> metricsHistory;
    private static final int MAX_HISTORY_SIZE = 100;

    public SharedMetricsStore() {
        this.metricsHistory = new ConcurrentHashMap<>();
        this.lastUpdateTime = Instant.now();
    }

    /**
     * Updates CPU metrics with write lock.
     */
    public void updateCpuMetrics(double cpuUsage) {
        writeLock.lock();
        try {
            this.latestCpuUsage = cpuUsage;
            this.lastUpdateTime = Instant.now();
            logger.debug("CPU metrics updated: {}%", String.format("%.2f", cpuUsage));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates memory metrics with write lock.
     */
    public void updateMemoryMetrics(double memoryUsage) {
        writeLock.lock();
        try {
            this.latestMemoryUsage = memoryUsage;
            this.lastUpdateTime = Instant.now();
            logger.debug("Memory metrics updated: {}%", String.format("%.2f", memoryUsage));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates disk metrics with write lock.
     */
    public void updateDiskMetrics(double diskUsage) {
        writeLock.lock();
        try {
            this.latestDiskUsage = diskUsage;
            this.lastUpdateTime = Instant.now();
            logger.debug("Disk metrics updated: {}%", String.format("%.2f", diskUsage));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates process count with write lock.
     */
    public void updateProcessCount(int processCount) {
        writeLock.lock();
        try {
            this.latestProcessCount = processCount;
            this.lastUpdateTime = Instant.now();
            logger.debug("Process count updated: {}", processCount);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Stores a complete metrics snapshot in history.
     * Implements automatic cleanup when exceeding max history size.
     */
    public void storeMetricsSnapshot(SystemMetrics metrics) {
        String key = metrics.getTimestamp().toString();
        metricsHistory.put(key, metrics);

        // Cleanup old entries if exceeding max size
        if (metricsHistory.size() > MAX_HISTORY_SIZE) {
            String oldestKey = metricsHistory.keySet().stream()
                    .min(String::compareTo)
                    .orElse(null);
            if (oldestKey != null) {
                metricsHistory.remove(oldestKey);
            }
        }
    }

    /**
     * Reads latest metrics with read lock.
     * Multiple threads can read simultaneously.
     */
    public SystemMetrics getLatestMetrics() {
        readLock.lock();
        try {
            return new SystemMetrics.Builder()
                    .timestamp(lastUpdateTime)
                    .cpuUsage(latestCpuUsage)
                    .memoryUsagePercent(latestMemoryUsage)
                    .diskUsagePercent(latestDiskUsage)
                    .processCount(latestProcessCount)
                    .build();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a snapshot of metrics history.
     */
    public Map<String, SystemMetrics> getMetricsHistory() {
        return new ConcurrentHashMap<>(metricsHistory);
    }

    /**
     * Clears all stored metrics.
     */
    public void clear() {
        writeLock.lock();
        try {
            latestCpuUsage = 0;
            latestMemoryUsage = 0;
            latestDiskUsage = 0;
            latestProcessCount = 0;
            metricsHistory.clear();
            lastUpdateTime = Instant.now();
            logger.info("Metrics store cleared");
        } finally {
            writeLock.unlock();
        }
    }

    // Getters for individual metrics (volatile ensures thread safety)
    public double getLatestCpuUsage() {
        return latestCpuUsage;
    }

    public double getLatestMemoryUsage() {
        return latestMemoryUsage;
    }

    public double getLatestDiskUsage() {
        return latestDiskUsage;
    }

    public int getLatestProcessCount() {
        return latestProcessCount;
    }

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }
}
