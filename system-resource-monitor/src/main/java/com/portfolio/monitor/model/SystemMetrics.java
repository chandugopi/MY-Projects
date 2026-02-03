package com.portfolio.monitor.model;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Thread-safe container for system metrics.
 * Uses immutable snapshots for thread safety.
 */
public class SystemMetrics {

    private final Instant timestamp;
    private final double cpuUsage;
    private final long totalMemory;
    private final long usedMemory;
    private final long freeMemory;
    private final double memoryUsagePercent;
    private final long totalDiskSpace;
    private final long usedDiskSpace;
    private final long freeDiskSpace;
    private final double diskUsagePercent;
    private final int processCount;
    private final Map<String, Long> topProcesses;

    private SystemMetrics(Builder builder) {
        this.timestamp = builder.timestamp;
        this.cpuUsage = builder.cpuUsage;
        this.totalMemory = builder.totalMemory;
        this.usedMemory = builder.usedMemory;
        this.freeMemory = builder.freeMemory;
        this.memoryUsagePercent = builder.memoryUsagePercent;
        this.totalDiskSpace = builder.totalDiskSpace;
        this.usedDiskSpace = builder.usedDiskSpace;
        this.freeDiskSpace = builder.freeDiskSpace;
        this.diskUsagePercent = builder.diskUsagePercent;
        this.processCount = builder.processCount;
        this.topProcesses = new ConcurrentHashMap<>(builder.topProcesses);
    }

    // Getters
    public Instant getTimestamp() {
        return timestamp;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public long getTotalDiskSpace() {
        return totalDiskSpace;
    }

    public long getUsedDiskSpace() {
        return usedDiskSpace;
    }

    public long getFreeDiskSpace() {
        return freeDiskSpace;
    }

    public double getDiskUsagePercent() {
        return diskUsagePercent;
    }

    public int getProcessCount() {
        return processCount;
    }

    public Map<String, Long> getTopProcesses() {
        return new ConcurrentHashMap<>(topProcesses);
    }

    @Override
    public String toString() {
        return String.format(
                "SystemMetrics{timestamp=%s, cpu=%.2f%%, memory=%.2f%%, disk=%.2f%%, processes=%d}",
                timestamp, cpuUsage, memoryUsagePercent, diskUsagePercent, processCount);
    }

    /**
     * Builder pattern for constructing immutable SystemMetrics instances.
     */
    public static class Builder {
        private Instant timestamp = Instant.now();
        private double cpuUsage;
        private long totalMemory;
        private long usedMemory;
        private long freeMemory;
        private double memoryUsagePercent;
        private long totalDiskSpace;
        private long usedDiskSpace;
        private long freeDiskSpace;
        private double diskUsagePercent;
        private int processCount;
        private Map<String, Long> topProcesses = new ConcurrentHashMap<>();

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder cpuUsage(double cpuUsage) {
            this.cpuUsage = cpuUsage;
            return this;
        }

        public Builder totalMemory(long totalMemory) {
            this.totalMemory = totalMemory;
            return this;
        }

        public Builder usedMemory(long usedMemory) {
            this.usedMemory = usedMemory;
            return this;
        }

        public Builder freeMemory(long freeMemory) {
            this.freeMemory = freeMemory;
            return this;
        }

        public Builder memoryUsagePercent(double memoryUsagePercent) {
            this.memoryUsagePercent = memoryUsagePercent;
            return this;
        }

        public Builder totalDiskSpace(long totalDiskSpace) {
            this.totalDiskSpace = totalDiskSpace;
            return this;
        }

        public Builder usedDiskSpace(long usedDiskSpace) {
            this.usedDiskSpace = usedDiskSpace;
            return this;
        }

        public Builder freeDiskSpace(long freeDiskSpace) {
            this.freeDiskSpace = freeDiskSpace;
            return this;
        }

        public Builder diskUsagePercent(double diskUsagePercent) {
            this.diskUsagePercent = diskUsagePercent;
            return this;
        }

        public Builder processCount(int processCount) {
            this.processCount = processCount;
            return this;
        }

        public Builder topProcesses(Map<String, Long> topProcesses) {
            this.topProcesses = new ConcurrentHashMap<>(topProcesses);
            return this;
        }

        public SystemMetrics build() {
            return new SystemMetrics(this);
        }
    }
}
