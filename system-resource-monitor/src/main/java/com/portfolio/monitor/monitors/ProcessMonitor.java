package com.portfolio.monitor.monitors;

import com.portfolio.monitor.core.SharedMetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Monitors running processes and tracks top CPU/memory consumers.
 * Runs as a scheduled task in the thread pool.
 * 
 * Key Concepts:
 * - Process enumeration
 * - Sorting and limiting results
 * - Stream API usage
 */
public class ProcessMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ProcessMonitor.class);
    private static final int TOP_PROCESSES_COUNT = 5;

    private final SharedMetricsStore metricsStore;
    private final OperatingSystem os;

    private volatile Map<Integer, long[]> previousProcessTicks = new HashMap<>();

    public ProcessMonitor(SharedMetricsStore metricsStore) {
        this.metricsStore = metricsStore;
        SystemInfo si = new SystemInfo();
        this.os = si.getOperatingSystem();
    }

    @Override
    public void run() {
        try {
            List<OSProcess> processes = os.getProcesses(
                    null,
                    OperatingSystem.ProcessSorting.CPU_DESC,
                    0);

            int processCount = processes.size();
            metricsStore.updateProcessCount(processCount);

            logger.info("⚙️  Processes: {} running", processCount);

            // Get top CPU consuming processes
            List<OSProcess> topCpuProcesses = processes.stream()
                    .filter(p -> p.getProcessCpuLoadCumulative() >= 0)
                    .sorted((p1, p2) -> Double.compare(
                            p2.getProcessCpuLoadCumulative(),
                            p1.getProcessCpuLoadCumulative()))
                    .limit(TOP_PROCESSES_COUNT)
                    .collect(Collectors.toList());

            if (!topCpuProcesses.isEmpty()) {
                logger.info("   Top {} by CPU:", TOP_PROCESSES_COUNT);
                for (int i = 0; i < topCpuProcesses.size(); i++) {
                    OSProcess p = topCpuProcesses.get(i);
                    double cpuPercent = p.getProcessCpuLoadCumulative() * 100;
                    logger.info("      {}. {} (PID {}): {:.1f}% CPU, {} memory",
                            i + 1,
                            truncateName(p.getName(), 20),
                            p.getProcessID(),
                            cpuPercent,
                            formatBytes(p.getResidentSetSize()));
                }
            }

            // Get top memory consuming processes
            List<OSProcess> topMemProcesses = processes.stream()
                    .sorted((p1, p2) -> Long.compare(
                            p2.getResidentSetSize(),
                            p1.getResidentSetSize()))
                    .limit(TOP_PROCESSES_COUNT)
                    .collect(Collectors.toList());

            if (!topMemProcesses.isEmpty()) {
                logger.debug("   Top {} by Memory:", TOP_PROCESSES_COUNT);
                for (int i = 0; i < topMemProcesses.size(); i++) {
                    OSProcess p = topMemProcesses.get(i);
                    logger.debug("      {}. {} (PID {}): {}",
                            i + 1,
                            truncateName(p.getName(), 20),
                            p.getProcessID(),
                            formatBytes(p.getResidentSetSize()));
                }
            }

        } catch (Exception e) {
            logger.error("Error monitoring processes", e);
        }
    }

    /**
     * Truncates a process name to the specified length.
     */
    private String truncateName(String name, int maxLength) {
        if (name == null) {
            return "Unknown";
        }
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, maxLength - 3) + "...";
    }

    /**
     * Formats bytes into human-readable format.
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
}
