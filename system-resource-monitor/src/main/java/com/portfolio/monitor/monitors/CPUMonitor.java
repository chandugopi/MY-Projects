package com.portfolio.monitor.monitors;

import com.portfolio.monitor.core.SharedMetricsStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

/**
 * Monitors CPU usage using OSHI library.
 * Runs as a scheduled task in the thread pool.
 * 
 * Key Concepts:
 * - Implements Runnable for thread execution
 * - Uses volatile for thread-safe state
 * - Demonstrates OSHI CPU metrics collection
 */
public class CPUMonitor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CPUMonitor.class);

    private final SharedMetricsStore metricsStore;
    private final CentralProcessor processor;

    private volatile long[] prevTicks;
    private volatile boolean initialized = false;

    public CPUMonitor(SharedMetricsStore metricsStore) {
        this.metricsStore = metricsStore;
        SystemInfo si = new SystemInfo();
        this.processor = si.getHardware().getProcessor();
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    @Override
    public void run() {
        try {
            double cpuLoad;

            if (!initialized) {
                // First run - need baseline ticks
                Thread.sleep(1000); // Wait for accurate first reading
                initialized = true;
            }

            // Calculate CPU load between tick readings
            cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
            prevTicks = processor.getSystemCpuLoadTicks();

            // Ensure valid reading
            if (cpuLoad < 0) {
                cpuLoad = 0;
            }

            metricsStore.updateCpuMetrics(cpuLoad);

            logger.info("📊 CPU Usage: {}", formatCpuBar(cpuLoad));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("CPU monitor interrupted");
        } catch (Exception e) {
            logger.error("Error monitoring CPU", e);
        }
    }

    /**
     * Creates a visual bar representation of CPU usage.
     */
    private String formatCpuBar(double cpuLoad) {
        int bars = (int) (cpuLoad / 5); // Each bar represents 5%
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 20; i++) {
            sb.append(i < bars ? "█" : "░");
        }
        sb.append("] ").append(String.format("%.1f%%", cpuLoad));

        // Add core info
        sb.append(" (").append(processor.getLogicalProcessorCount()).append(" cores)");

        return sb.toString();
    }

    /**
     * Returns the processor info string.
     */
    public String getProcessorInfo() {
        return processor.getProcessorIdentifier().getName();
    }
}
