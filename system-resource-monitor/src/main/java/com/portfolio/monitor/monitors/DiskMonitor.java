package com.portfolio.monitor.monitors;

import com.portfolio.monitor.core.SharedMetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;

import java.util.List;

/**
 * Monitors disk usage across all mounted file systems.
 * Runs as a scheduled task in the thread pool.
 * 
 * Key Concepts:
 * - File system iteration
 * - Aggregate disk metrics
 * - Per-partition reporting
 */
public class DiskMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DiskMonitor.class);

    private final SharedMetricsStore metricsStore;
    private final FileSystem fileSystem;

    public DiskMonitor(SharedMetricsStore metricsStore) {
        this.metricsStore = metricsStore;
        SystemInfo si = new SystemInfo();
        this.fileSystem = si.getOperatingSystem().getFileSystem();
    }

    @Override
    public void run() {
        try {
            List<OSFileStore> fileStores = fileSystem.getFileStores();

            long totalSpace = 0;
            long usedSpace = 0;

            for (OSFileStore store : fileStores) {
                long storeTotal = store.getTotalSpace();
                long storeUsable = store.getUsableSpace();
                long storeUsed = storeTotal - storeUsable;

                totalSpace += storeTotal;
                usedSpace += storeUsed;

                // Log individual partitions (debug level)
                if (storeTotal > 0) {
                    double storePercent = (double) storeUsed / storeTotal * 100;
                    logger.debug("   {} ({}): {} / {} ({}%)",
                            store.getName(),
                            store.getMount(),
                            formatBytes(storeUsed),
                            formatBytes(storeTotal),
                            String.format("%.1f", storePercent));
                }
            }

            double diskUsagePercent = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
            metricsStore.updateDiskMetrics(diskUsagePercent);

            logger.info("💿 Disk:   {} {} / {} ({}%)",
                    formatDiskBar(diskUsagePercent),
                    formatBytes(usedSpace),
                    formatBytes(totalSpace),
                    String.format("%.1f", diskUsagePercent));

        } catch (Exception e) {
            logger.error("Error monitoring disk", e);
        }
    }

    /**
     * Creates a visual bar representation of disk usage.
     */
    private String formatDiskBar(double diskPercent) {
        int bars = (int) (diskPercent / 5); // Each bar represents 5%
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                // Color based on usage level
                if (diskPercent > 90) {
                    sb.append("█"); // Critical
                } else if (diskPercent > 75) {
                    sb.append("▓"); // Warning
                } else {
                    sb.append("█"); // Normal
                }
            } else {
                sb.append("░");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Formats bytes into human-readable format (KB, MB, GB, TB).
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else if (bytes < 1024L * 1024 * 1024 * 1024) {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        } else {
            return String.format("%.2f TB", bytes / (1024.0 * 1024 * 1024 * 1024));
        }
    }
}
