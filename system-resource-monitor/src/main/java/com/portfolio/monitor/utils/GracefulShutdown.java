package com.portfolio.monitor.utils;

import com.portfolio.monitor.core.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Handles graceful shutdown of the application.
 * Registers a JVM shutdown hook to properly cleanup resources.
 * 
 * Key Concepts:
 * - Shutdown Hook: JVM callback before termination
 * - Graceful degradation: Proper resource cleanup
 * - Signal handling: Responds to SIGTERM/SIGINT
 */
public class GracefulShutdown {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdown.class);
    private static final long DEFAULT_TIMEOUT_SECONDS = 10;

    private final ThreadPoolManager threadPoolManager;
    private final Runnable onShutdown;
    private volatile boolean shuttingDown = false;

    /**
     * Creates a graceful shutdown handler.
     * 
     * @param threadPoolManager The thread pool to shutdown
     * @param onShutdown        Optional callback to execute during shutdown
     */
    public GracefulShutdown(ThreadPoolManager threadPoolManager, Runnable onShutdown) {
        this.threadPoolManager = threadPoolManager;
        this.onShutdown = onShutdown;
        registerShutdownHook();
    }

    public GracefulShutdown(ThreadPoolManager threadPoolManager) {
        this(threadPoolManager, null);
    }

    /**
     * Registers the JVM shutdown hook.
     * This hook is called when:
     * - The program ends normally
     * - User interrupts (Ctrl+C)
     * - System shutdown
     * - Runtime.exit() is called
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            performShutdown();
        }, "shutdown-hook"));

        logger.info("✅ Shutdown hook registered");
    }

    /**
     * Performs the actual shutdown sequence.
     */
    private void performShutdown() {
        if (shuttingDown) {
            return; // Already shutting down
        }

        shuttingDown = true;

        logger.info("");
        logger.info("═══════════════════════════════════════════════");
        logger.info("  🛑 GRACEFUL SHUTDOWN INITIATED");
        logger.info("═══════════════════════════════════════════════");

        try {
            // Execute custom shutdown callback if provided
            if (onShutdown != null) {
                logger.info("Executing shutdown callback...");
                onShutdown.run();
            }

            // Shutdown thread pool
            logger.info("Shutting down thread pool...");
            threadPoolManager.shutdown(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Wait for shutdown to complete
            if (threadPoolManager.awaitShutdown(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.info("All threads terminated successfully");
            } else {
                logger.warn("Shutdown timeout - some threads may not have terminated");
            }

        } catch (InterruptedException e) {
            logger.error("Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }

        logger.info("═══════════════════════════════════════════════");
        logger.info("  ✅ SHUTDOWN COMPLETE");
        logger.info("═══════════════════════════════════════════════");
    }

    /**
     * Manually triggers shutdown (for programmatic use).
     */
    public void initiateShutdown() {
        performShutdown();
    }

    /**
     * Returns whether shutdown is in progress.
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }
}
