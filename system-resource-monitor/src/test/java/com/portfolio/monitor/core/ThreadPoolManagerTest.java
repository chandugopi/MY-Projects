package com.portfolio.monitor.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ThreadPoolManager.
 * Tests thread pool lifecycle and task scheduling.
 */
@DisplayName("ThreadPoolManager Tests")
class ThreadPoolManagerTest {

    private ThreadPoolManager poolManager;

    @BeforeEach
    void setUp() {
        poolManager = new ThreadPoolManager(4);
    }

    @AfterEach
    void tearDown() {
        if (poolManager.isRunning()) {
            poolManager.shutdown(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Should create pool with correct size")
    void testPoolSize() {
        assertEquals(4, poolManager.getPoolSize());
    }

    @Test
    @DisplayName("Should execute scheduled task")
    void testScheduledTask() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        poolManager.scheduleAtFixedRate(() -> {
            counter.incrementAndGet();
            latch.countDown();
        }, 0, 100, TimeUnit.MILLISECONDS);

        latch.await(2, TimeUnit.SECONDS);

        assertTrue(counter.get() >= 3);
        assertTrue(poolManager.isRunning());
    }

    @Test
    @DisplayName("Should submit and execute callable task")
    void testSubmitCallable() throws ExecutionException, InterruptedException, TimeoutException {
        Future<Integer> future = poolManager.submit(() -> 42);
        assertEquals(42, future.get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should submit and execute runnable task")
    void testSubmitRunnable() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        poolManager.submit(latch::countDown);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should shutdown gracefully")
    void testGracefulShutdown() throws InterruptedException {
        // Start some tasks
        AtomicInteger counter = new AtomicInteger(0);
        poolManager.scheduleAtFixedRate(counter::incrementAndGet, 0, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(300);

        // Shutdown
        poolManager.shutdown(5, TimeUnit.SECONDS);

        assertFalse(poolManager.isRunning());
    }

    @Test
    @DisplayName("Should handle multiple concurrent tasks")
    void testConcurrentTasks() throws InterruptedException {
        int taskCount = 10;
        CountDownLatch latch = new CountDownLatch(taskCount);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            poolManager.submit(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        assertEquals(taskCount, counter.get());
    }
}
