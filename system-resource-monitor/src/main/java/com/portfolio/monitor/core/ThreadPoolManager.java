package com.portfolio.monitor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Manages a pool of worker threads for monitoring tasks.
 * Demonstrates ExecutorService, ThreadFactory, and thread pool management.
 * 
 * Key Concurrency Concepts:
 * - ExecutorService: Manages thread lifecycle
 * - ScheduledExecutorService: For periodic tasks
 * - ThreadFactory: Custom thread naming
 * - CountDownLatch: Coordination between threads
 */
public class ThreadPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    private final ScheduledExecutorService scheduledExecutor;
    private final ExecutorService workerPool;
    private final List<ScheduledFuture<?>> scheduledTasks;
    private final CountDownLatch shutdownLatch;

    private volatile boolean isRunning;
    private final int poolSize;

    /**
     * Creates a thread pool manager with the specified pool size.
     * 
     * @param poolSize Number of threads in the worker pool
     */
    public ThreadPoolManager(int poolSize) {
        this.poolSize = poolSize;
        this.scheduledExecutor = Executors.newScheduledThreadPool(poolSize, new MonitorThreadFactory("monitor"));
        this.workerPool = Executors.newFixedThreadPool(poolSize, new MonitorThreadFactory("worker"));
        this.scheduledTasks = new ArrayList<>();
        this.shutdownLatch = new CountDownLatch(1);
        this.isRunning = false;

        logger.info("ThreadPoolManager initialized with {} threads", poolSize);
    }

    /**
     * Schedules a periodic monitoring task.
     * 
     * @param task         The runnable task to execute
     * @param initialDelay Initial delay before first execution
     * @param period       Period between executions
     * @param unit         Time unit for delay and period
     * @return ScheduledFuture for the task
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
        if (!isRunning) {
            isRunning = true;
            logger.info("Thread pool started");
        }

        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in scheduled task", e);
            }
        }, initialDelay, period, unit);

        scheduledTasks.add(future);
        logger.debug("Scheduled new task with period {}ms", unit.toMillis(period));

        return future;
    }

    /**
     * Submits a one-time task to the worker pool.
     * 
     * @param task The callable task to execute
     * @return Future representing the pending result
     */
    public <T> Future<T> submit(Callable<T> task) {
        return workerPool.submit(task);
    }

    /**
     * Submits a one-time runnable to the worker pool.
     * 
     * @param task The runnable task to execute
     * @return Future representing the pending completion
     */
    public Future<?> submit(Runnable task) {
        return workerPool.submit(task);
    }

    /**
     * Gracefully shuts down the thread pool.
     * Waits for running tasks to complete before forcing shutdown.
     * 
     * @param timeout Maximum time to wait for shutdown
     * @param unit    Time unit for timeout
     */
    public void shutdown(long timeout, TimeUnit unit) {
        logger.info("Initiating graceful shutdown...");
        isRunning = false;

        // Cancel all scheduled tasks
        for (ScheduledFuture<?> task : scheduledTasks) {
            task.cancel(false);
        }

        // Shutdown scheduled executor
        scheduledExecutor.shutdown();
        workerPool.shutdown();

        try {
            // Wait for tasks to complete
            if (!scheduledExecutor.awaitTermination(timeout, unit)) {
                logger.warn("Scheduled executor did not terminate in time, forcing shutdown");
                scheduledExecutor.shutdownNow();
            }

            if (!workerPool.awaitTermination(timeout, unit)) {
                logger.warn("Worker pool did not terminate in time, forcing shutdown");
                workerPool.shutdownNow();
            }

            logger.info("Thread pool shutdown complete");
        } catch (InterruptedException e) {
            logger.error("Shutdown interrupted", e);
            scheduledExecutor.shutdownNow();
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            shutdownLatch.countDown();
        }
    }

    /**
     * Waits for shutdown to complete.
     * 
     * @param timeout Maximum time to wait
     * @param unit    Time unit for timeout
     * @return true if shutdown completed, false if timeout occurred
     */
    public boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        return shutdownLatch.await(timeout, unit);
    }

    /**
     * Returns whether the pool is currently running.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns the pool size.
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Custom ThreadFactory for creating named threads.
     */
    private static class MonitorThreadFactory implements ThreadFactory {
        private final String prefix;
        private int threadCount = 0;

        public MonitorThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, prefix + "-" + threadCount++);
            thread.setDaemon(true); // Daemon threads don't prevent JVM shutdown
            logger.debug("Created new thread: {}", thread.getName());
            return thread;
        }
    }
}
