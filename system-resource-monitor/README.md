# 🖥️ Multithreaded System Resource Monitor

A Java application demonstrating key **operating system** and **concurrency** concepts by monitoring CPU, memory, disk, and process metrics in real-time.

**Course Context**: CSCI 6638 + CSCI 7645 (Operating Systems)

## 🎯 Features

- **Real-time Monitoring**: CPU, Memory, Disk, and Process metrics updated every 5 seconds
- **Thread Pool Management**: Uses `ExecutorService` and `ScheduledExecutorService`
- **Thread Safety**: Demonstrates `ReentrantReadWriteLock`, `volatile`, and `ConcurrentHashMap`
- **Graceful Shutdown**: JVM shutdown hook for proper resource cleanup
- **Visual Output**: Progress bars and formatted metrics in the console

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Main.java                           │
│                    (Application Entry)                       │
└───────────────────────────┬─────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐   ┌───────────────┐   ┌───────────────┐
│ ThreadPool    │   │ SharedMetrics │   │ Graceful      │
│ Manager       │   │ Store         │   │ Shutdown      │
│               │   │               │   │               │
│ • Executor    │   │ • RWLock      │   │ • Shutdown    │
│ • Scheduling  │   │ • Volatile    │   │   Hook        │
│ • Thread      │   │ • Concurrent  │   │ • Cleanup     │
│   Factory     │   │   HashMap     │   │               │
└───────┬───────┘   └───────────────┘   └───────────────┘
        │
        ├────────────┬────────────┬────────────┐
        ▼            ▼            ▼            ▼
┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐
│ CPU       │ │ Memory    │ │ Disk      │ │ Process   │
│ Monitor   │ │ Monitor   │ │ Monitor   │ │ Monitor   │
│           │ │           │ │           │ │           │
│ Runnable  │ │ Runnable  │ │ Runnable  │ │ Runnable  │
└───────────┘ └───────────┘ └───────────┘ └───────────┘
```

## 📁 Project Structure

```
system-resource-monitor/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/portfolio/monitor/
    │   │   ├── Main.java                    # Entry point
    │   │   ├── core/
    │   │   │   ├── ThreadPoolManager.java   # Thread pool management
    │   │   │   └── SharedMetricsStore.java  # Thread-safe storage
    │   │   ├── monitors/
    │   │   │   ├── CPUMonitor.java          # CPU tracking
    │   │   │   ├── MemoryMonitor.java       # Memory tracking
    │   │   │   ├── DiskMonitor.java         # Disk tracking
    │   │   │   └── ProcessMonitor.java      # Process tracking
    │   │   ├── model/
    │   │   │   └── SystemMetrics.java       # Metrics POJO
    │   │   └── utils/
    │   │       └── GracefulShutdown.java    # Shutdown handler
    │   └── resources/
    │       └── logback.xml                  # Logging config
    └── test/java/com/portfolio/monitor/
        ├── core/
        │   ├── SharedMetricsStoreTest.java
        │   └── ThreadPoolManagerTest.java
        └── model/
            └── SystemMetricsTest.java
```

## 🔧 Technology Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Core language with modern concurrency APIs |
| OSHI | Operating system & hardware information |
| SLF4J + Logback | Logging framework |
| JUnit 5 | Unit testing |
| Mockito | Mocking framework |
| Maven | Build tool |

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.8+

### Build & Run

```bash
# Clone and navigate to project
cd system-resource-monitor

# Build the project
mvn clean package

# Run the application
java -jar target/system-resource-monitor-1.0.0.jar

# Or use Maven
mvn exec:java -Dexec.mainClass="com.portfolio.monitor.Main"
```

### Run Tests

```bash
mvn test
```

## 🧠 Key Concurrency Concepts Demonstrated

### 1. Thread Pools (`ThreadPoolManager.java`)
```java
// ScheduledExecutorService for periodic tasks
ScheduledExecutorService scheduledExecutor = 
    Executors.newScheduledThreadPool(poolSize, new MonitorThreadFactory("monitor"));

// Schedule at fixed rate
scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
```

### 2. Read-Write Locks (`SharedMetricsStore.java`)
```java
private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

// Write lock for exclusive access
public void updateCpuMetrics(double cpuUsage) {
    writeLock.lock();
    try {
        this.latestCpuUsage = cpuUsage;
    } finally {
        writeLock.unlock();
    }
}

// Read lock for concurrent reads
public SystemMetrics getLatestMetrics() {
    readLock.lock();
    try {
        return buildMetrics();
    } finally {
        readLock.unlock();
    }
}
```

### 3. Volatile Variables
```java
// Ensures visibility across threads without full synchronization
private volatile double latestCpuUsage;
private volatile boolean isRunning;
```

### 4. Thread-Safe Collections
```java
// ConcurrentHashMap for thread-safe storage
private final ConcurrentHashMap<String, SystemMetrics> metricsHistory;
```

### 5. Graceful Shutdown
```java
// JVM shutdown hook
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    performShutdown();
}, "shutdown-hook"));
```

## 💡 Interview Questions This Project Answers

| Question | Covered In |
|----------|------------|
| What is the difference between a thread and a process? | ProcessMonitor, README |
| How do you prevent deadlock? | SharedMetricsStore lock ordering |
| What is a thread pool and why use one? | ThreadPoolManager |
| How do you synchronize access to shared data? | SharedMetricsStore (RWLock) |
| What does `volatile` do? | SharedMetricsStore fields |
| How does the JVM manage memory? | MemoryMonitor output |
| How do you implement graceful shutdown? | GracefulShutdown |

## 📊 Sample Output

```
╔═══════════════════════════════════════════════════════════╗
║   🖥️  MULTITHREADED SYSTEM RESOURCE MONITOR               ║
╚═══════════════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  SYSTEM INFORMATION
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  OS:        macOS 14.0
  CPU:       Apple M1 Pro
  Cores:     10 physical, 10 logical
  Memory:    32.00 GB total
  JVM:       17.0.8 (Eclipse Adoptium)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📊 CPU Usage: [████████░░░░░░░░░░░░] 38.2% (10 cores)
💾 Memory:    [██████████████░░░░░░] 13.2 GB / 32.0 GB (41.3%)
💿 Disk:      [████████████████░░░░] 412.5 GB / 500.0 GB (82.5%)
⚙️  Processes: 425 running
─────────────────────────────────────────────────
```

## 📝 License

MIT License - See LICENSE file for details.
