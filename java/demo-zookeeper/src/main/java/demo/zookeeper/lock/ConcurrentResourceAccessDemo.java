package demo.zookeeper.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发资源访问演示类
 * 演示使用锁和不使用锁时，多个并发任务访问共享资源（累加）的区别
 */
public class ConcurrentResourceAccessDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentResourceAccessDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 锁的路径
    private static final String LOCK_PATH = "/zk-locks/resource-lock";
    
    // 共享资源（模拟计数器）
    private static class SharedResource {
        private int value = 0;
        
        public void increment() {
            // 模拟一些计算时间
            int temp = value;
            try {
                Thread.sleep(1); // 模拟业务处理时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            value = temp + 1;
        }
        
        public int getValue() {
            return value;
        }
        
        public void reset() {
            value = 0;
        }
    }
    
    public static void main(String[] args) throws Exception {
        logger.info("========== 并发资源访问演示开始 ==========");
        
        // 演示 1: 不使用锁的情况（会出现数据不一致）
        demonstrateWithoutLock();
        
        // 演示 2: 使用锁的情况（数据一致）
        demonstrateWithLock();
        
        // 演示 3: 使用原子类的情况（对比）
        demonstrateWithAtomic();
        
        logger.info("========== 并发资源访问演示结束 ==========");
    }
    
    /**
     * 演示 1: 不使用锁的情况
     * 多个线程并发累加，会出现数据丢失和不一致
     */
    private static void demonstrateWithoutLock() throws Exception {
        logger.info("\n--- 演示 1: 不使用锁的情况（会出现数据不一致） ---");
        
        SharedResource resource = new SharedResource();
        resource.reset();
        
        int threadCount = 10;
        int incrementsPerThread = 100;
        int expectedValue = threadCount * incrementsPerThread;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        // 多个线程并发累加，不使用锁
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // 不使用锁，直接访问共享资源
                        resource.increment();
                    }
                    logger.debug("线程 {} 完成累加", threadId);
                } catch (Exception e) {
                    logger.error("线程 {} 执行失败", threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        int actualValue = resource.getValue();
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("不使用锁的结果：");
        logger.info("  期望值: {}", expectedValue);
        logger.info("  实际值: {}", actualValue);
        logger.info("  丢失: {} 次累加", expectedValue - actualValue);
        logger.info("  耗时: {} ms", endTime - startTime);
        logger.info("  ✗ 数据不一致！多个线程同时修改共享资源导致数据丢失");
    }
    
    /**
     * 演示 2: 使用锁的情况
     * 多个线程使用分布式锁保护共享资源，数据一致
     */
    private static void demonstrateWithLock() throws Exception {
        logger.info("\n--- 演示 2: 使用分布式锁的情况（数据一致） ---");
        
        SharedResource resource = new SharedResource();
        resource.reset();
        
        // 创建 ZooKeeper 客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
        client.start();
        
        try {
            int threadCount = 10;
            int incrementsPerThread = 100;
            int expectedValue = threadCount * incrementsPerThread;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            long startTime = System.currentTimeMillis();
            
            // 多个线程使用分布式锁保护共享资源
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    // 每个线程创建自己的锁实例（也可以共享同一个锁实例）
                    ZkLockV2 lock = new ZkLockV2(client, LOCK_PATH);
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            // 获取锁
                            lock.lock();
                            try {
                                // 在锁保护下访问共享资源
                                resource.increment();
                            } finally {
                                // 释放锁
                                lock.unlock();
                            }
                        }
                        logger.debug("线程 {} 完成累加", threadId);
                    } catch (Exception e) {
                        logger.error("线程 {} 执行失败", threadId, e);
                    } finally {
                        try {
                            lock.close();
                        } catch (Exception e) {
                            logger.warn("线程 {} 关闭锁失败", threadId, e);
                        }
                        latch.countDown();
                    }
                });
            }
            
            // 等待所有线程完成
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long endTime = System.currentTimeMillis();
            int actualValue = resource.getValue();
            
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            logger.info("使用分布式锁的结果：");
            logger.info("  期望值: {}", expectedValue);
            logger.info("  实际值: {}", actualValue);
            logger.info("  是否一致: {}", actualValue == expectedValue ? "✓ 是" : "✗ 否");
            logger.info("  耗时: {} ms", endTime - startTime);
            logger.info("  ✓ 数据一致！分布式锁保证了共享资源的安全访问");
            
        } finally {
            client.close();
        }
    }
    
    /**
     * 演示 3: 使用原子类的情况（对比）
     * 使用 AtomicInteger 作为对比，展示单机场景下的线程安全方案
     */
    private static void demonstrateWithAtomic() throws Exception {
        logger.info("\n--- 演示 3: 使用原子类的情况（单机线程安全方案对比） ---");
        
        AtomicInteger atomicValue = new AtomicInteger(0);
        
        int threadCount = 10;
        int incrementsPerThread = 100;
        int expectedValue = threadCount * incrementsPerThread;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        // 多个线程使用原子类累加
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        // 使用原子类，无需锁
                        atomicValue.incrementAndGet();
                    }
                    logger.debug("线程 {} 完成累加", threadId);
                } catch (Exception e) {
                    logger.error("线程 {} 执行失败", threadId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // 等待所有线程完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        int actualValue = atomicValue.get();
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("使用原子类的结果：");
        logger.info("  期望值: {}", expectedValue);
        logger.info("  实际值: {}", actualValue);
        logger.info("  是否一致: {}", actualValue == expectedValue ? "✓ 是" : "✗ 否");
        logger.info("  耗时: {} ms", endTime - startTime);
        logger.info("  ✓ 数据一致！原子类提供了单机场景下的线程安全");
        logger.info("  注意：原子类只适用于单机场景，分布式场景需要使用分布式锁");
    }
}
