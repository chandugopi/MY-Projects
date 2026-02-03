package com.portfolio.monitor;

import com.portfolio.monitor.core.SharedMetricsStore;
import com.portfolio.monitor.core.ThreadPoolManager;
import com.portfolio.monitor.monitors.*;
import com.portfolio.monitor.utils.GracefulShutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.concurrent.TimeUnit;

/**
 * Main entry point for the Multithreaded System Resource Monitor.
 * 
 * This application demonstrates key concurrency concepts:
 * - Thread pools (ExecutorService, ScheduledExecutorService)
 * - Synchronization (ReentrantReadWriteLock, synchronized blocks)
 * - Thread-safe data structures (ConcurrentHashMap, volatile)
 * - Graceful shutdown (Shutdown hooks, CountDownLatch)
 * 
 * Course: CSCI 6638 + CSCI 7645 (Operating Systems)
 * 
 * @author Portfolio Project
 * @version 1.0.0
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    // Configuration
    private static final int THREAD_POOL_SIZE = 4;
    private static final long MONITORING_INTERVAL_MS = 5000; // 5 seconds

    public static void main(String[] args) {
        printBanner();
        printSystemInfo();

        // Initialize shared components
        SharedMetricsStore metricsStore = new SharedMetricsStore();
        ThreadPoolManager threadPool = new ThreadPoolManager(THREAD_POOL_SIZE);

        // Initialize monitors
        CPUMonitor cpuMonitor = new CPUMonitor(metricsStore);
        MemoryMonitor memoryMonitor = new MemoryMonitor(metricsStore);
        DiskMonitor diskMonitor = new DiskMonitor(metricsStore);
        ProcessMonitor processMonitor = new ProcessMonitor(metricsStore);

        // Register graceful shutdown
        GracefulShutdown shutdown = new GracefulShutdown(threadPool, () -> {
            logger.info("Saving final metrics snapshot...");
            // Could persist metrics here
        });

        logger.info("Starting monitoring threads...");
        logger.info("Refresh interval: {} seconds", MONITORING_INTERVAL_MS / 1000);
        logger.info("Press Ctrl+C to stop.\n");

        // Schedule monitoring tasks at fixed intervals
        // Stagger start times to prevent thundering herd
        threadPool.scheduleAtFixedRate(cpuMonitor, 0, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS);
        threadPool.scheduleAtFixedRate(memoryMonitor, 500, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS);
        threadPool.scheduleAtFixedRate(diskMonitor, 1000, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS);
        threadPool.scheduleAtFixedRate(processMonitor, 1500, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // Print separator for readability
        threadPool.scheduleAtFixedRate(() -> {
            logger.info("─────────────────────────────────────────────────");
        }, MONITORING_INTERVAL_MS - 100, MONITORING_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // Keep main thread alive
        try {
            // Wait indefinitely until shutdown signal
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("Main thread interrupted");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Prints the application banner.
     */
    private static void printBanner() {
        logger.info("");
        logger.info("╔═══════════════════════════════════════════════════════════╗");
        logger.info("║                                                           ║");
        logger.info("║   🖥️  MULTITHREADED SYSTEM RESOURCE MONITOR               ║");
        logger.info("║                                                           ║");
        logger.info("║   Demonstrating Java Concurrency Concepts:                ║");
        logger.info("║   • Thread Pools (ExecutorService)                        ║");
        logger.info("║   • Synchronization (ReentrantReadWriteLock)              ║");
        logger.info("║   • Thread-Safe Collections (ConcurrentHashMap)           ║");
        logger.info("║   • Graceful Shutdown (ShutdownHook)                      ║");
        logger.info("║                                                           ║");
        logger.info("╚═══════════════════════════════════════════════════════════╝");
        logger.info("");
    }

    /**
     * Prints system information on startup.
     */
    private static void printSystemInfo() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();
            OperatingSystem os = si.getOperatingSystem();

            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.info("  SYSTEM INFORMATION");
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            logger.info("  OS:        {} {}", os.getFamily(), os.getVersionInfo());
            logger.info("  CPU:       {}", hal.getProcessor().getProcessorIdentifier().getName());
            logger.info("  Cores:     {} physical, {} logical",
                    hal.getProcessor().getPhysicalProcessorCount(),
                    hal.getProcessor().getLogicalProcessorCount());
            logger.info("  Memory:    {} GB total",
                    String.format("%.2f", hal.getMemory().getTotal() / (1024.0 * 1024 * 1024)));
            logger.info("  JVM:       {} ({})",
                    System.getProperty("java.version"),
                    System.getProperty("java.vendor"));
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        } catch (Exception e) {
            logger.warn("Could not retrieve full system info: {}", e.getMessage());
        }
    }
}
