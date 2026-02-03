package com.portfolio.monitor.monitors;

import com.portfolio.monitor.core.SharedMetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;

/**
 * Monitors memory usage (RAM and Swap) using OSHI library.
 * Runs as a scheduled task in the thread pool.
 * 
 * Key Concepts:
 * - Memory metrics collection
 * - Human-readable byte formatting
 * - Thread-safe metric updates
 */
public class MemoryMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);

    private final SharedMetricsStore metricsStore;
    private final GlobalMemory memory;

    public MemoryMonitor(SharedMetricsStore metricsStore) {
        this.metricsStore = metricsStore;
        SystemInfo si = new SystemInfo();
        this.memory = si.getHardware().getMemory();
    }

    @Override
    public void run() {
        try {
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

            metricsStore.updateMemoryMetrics(memoryUsagePercent);

            logger.info("💾 Memory: {} {} / {} ({}%)",
                    formatMemoryBar(memoryUsagePercent),
                    formatBytes(usedMemory),
                    formatBytes(totalMemory),
                    String.format("%.1f", memoryUsagePercent));

            // Log swap if available
            long swapTotal = memory.getVirtualMemory().getSwapTotal();
            if (swapTotal > 0) {
                long swapUsed = memory.getVirtualMemory().getSwapUsed();
                double swapPercent = (double) swapUsed / swapTotal * 100;
                logger.debug("   Swap: {} / {} ({}%)",
                        formatBytes(swapUsed),
                        formatBytes(swapTotal),
                        String.format("%.1f", swapPercent));
            }

        } catch (Exception e) {
            logger.error("Error monitoring memory", e);
        }
    }

    /**
     * Creates a visual bar representation of memory usage.
     */
    private String formatMemoryBar(double memoryPercent) {
        int bars = (int) (memoryPercent / 5); // Each bar represents 5%
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < bars ? "█" : "░");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Formats bytes into human-readable format (KB, MB, GB).
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Returns total physical memory.
     */
    public long getTotalMemory() {
        return memory.getTotal();
    }
}
