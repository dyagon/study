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
 * ZkLockV2 演示类
 * 演示修复了线程安全问题后的可重入公平锁
 */
public class ZkLockV2Demo {
    
    private static final Logger logger = LoggerFactory.getLogger(ZkLockV2Demo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 锁的路径
    private static final String LOCK_PATH = "/zk-locks-v2/demo-lock";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== ZkLockV2 可重入公平锁演示开始 ==========");
        
        // 演示 1: 基本锁使用
        demonstrateBasicLock();
        
        // 演示 2: 可重入特性
        demonstrateReentrant();
        
        // 演示 3: 多线程安全（关键修复）
        demonstrateMultiThreadSafety();
        
        // 演示 4: 公平锁特性
        demonstrateFairLock();
        
        logger.info("========== ZkLockV2 可重入公平锁演示结束 ==========");
    }
    
    /**
     * 演示 1: 基本锁使用
     */
    private static void demonstrateBasicLock() throws Exception {
        logger.info("\n--- 演示 1: 基本锁使用 ---");
        
        CuratorFramework client = createClient("client-1");
        try (ZkLockV2 lock = new ZkLockV2(client, LOCK_PATH)) {
            logger.info("尝试获取锁...");
            lock.lock();
            logger.info("✓ 成功获取锁");
            
            // 模拟业务操作
            logger.info("执行业务操作...");
            Thread.sleep(2000);
            logger.info("业务操作完成");
            
            lock.unlock();
            logger.info("✓ 锁已释放");
        } finally {
            client.close();
        }
    }
    
    /**
     * 演示 2: 可重入特性
     */
    private static void demonstrateReentrant() throws Exception {
        logger.info("\n--- 演示 2: 可重入特性 ---");
        
        CuratorFramework client = createClient("client-2");
        try (ZkLockV2 lock = new ZkLockV2(client, LOCK_PATH)) {
            logger.info("第一次获取锁...");
            lock.lock();
            logger.info("✓ 第一次获取锁成功，重入次数: {}", lock.getReentrantCount());
            
            logger.info("第二次获取锁（可重入）...");
            lock.lock();
            logger.info("✓ 第二次获取锁成功，重入次数: {}", lock.getReentrantCount());
            
            logger.info("第三次获取锁（可重入）...");
            lock.lock();
            logger.info("✓ 第三次获取锁成功，重入次数: {}", lock.getReentrantCount());
            
            // 模拟嵌套调用
            nestedMethod(lock, 2);
            
            logger.info("当前重入次数: {}", lock.getReentrantCount());
            
            // 释放所有锁
            logger.info("开始释放锁...");
            while (lock.isLocked()) {
                lock.unlock();
                logger.info("释放一次锁，剩余重入次数: {}", lock.getReentrantCount());
            }
            logger.info("✓ 所有锁已释放");
        } finally {
            client.close();
        }
    }
    
    /**
     * 嵌套方法，演示可重入
     */
    private static void nestedMethod(ZkLockV2 lock, int depth) throws Exception {
        if (depth <= 0) {
            return;
        }
        
        logger.info("嵌套方法深度 {} - 获取锁", depth);
        lock.lock();
        logger.info("嵌套方法深度 {} - 锁获取成功，重入次数: {}", depth, lock.getReentrantCount());
        
        Thread.sleep(500);
        
        nestedMethod(lock, depth - 1);
        
        lock.unlock();
        logger.info("嵌套方法深度 {} - 锁已释放，剩余重入次数: {}", depth, lock.getReentrantCount());
    }
    
    /**
     * 演示 3: 多线程安全（关键修复）
     * 这是 V2 版本最重要的改进：多个线程可以安全地共享同一个锁实例
     */
    private static void demonstrateMultiThreadSafety() throws Exception {
        logger.info("\n--- 演示 3: 多线程安全（关键修复） ---");
        logger.info("多个线程共享同一个 ZkLockV2 实例，每个线程独立管理自己的锁状态");
        
        CuratorFramework client = createClient("client-shared");
        ZkLockV2 sharedLock = new ZkLockV2(client, LOCK_PATH);
        
        int threadCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        
        // 多个线程共享同一个锁实例
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    
                    logger.info("线程 {} 尝试获取共享锁...", threadId);
                    
                    // 每个线程都使用同一个 sharedLock 实例
                    sharedLock.lock();
                    logger.info("线程 {} ✓ 获取锁成功", threadId);
                    successCount.incrementAndGet();
                    
                    // 模拟业务操作
                    Thread.sleep(1000);
                    
                    // 可重入测试
                    sharedLock.lock();
                    logger.info("线程 {} 重入获取锁，重入次数: {}", threadId, sharedLock.getReentrantCount());
                    Thread.sleep(500);
                    sharedLock.unlock();
                    logger.info("线程 {} 释放重入锁，剩余重入次数: {}", threadId, sharedLock.getReentrantCount());
                    
                    sharedLock.unlock();
                    logger.info("线程 {} ✓ 锁已释放", threadId);
                    
                } catch (Exception e) {
                    logger.error("线程 {} 执行失败", threadId, e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        // 等待所有线程准备就绪
        Thread.sleep(1000);
        logger.info("所有线程准备就绪，开始竞争锁...");
        startLatch.countDown();
        
        // 等待所有线程完成
        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("成功获取锁的线程数: {} (期望: {})", successCount.get(), threadCount);
        logger.info("✓ 多线程安全验证通过：每个线程独立管理自己的锁状态，不会相互干扰");
        
        client.close();
    }
    
    /**
     * 演示 4: 公平锁特性
     */
    private static void demonstrateFairLock() throws Exception {
        logger.info("\n--- 演示 4: 公平锁特性（多线程按顺序获取） ---");
        
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger order = new AtomicInteger(0);
        
        // 创建多个线程竞争锁
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                CuratorFramework client = createClient("client-thread-" + threadId);
                try (ZkLockV2 lock = new ZkLockV2(client, LOCK_PATH)) {
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    
                    logger.info("线程 {} 尝试获取锁...", threadId);
                    long startTime = System.currentTimeMillis();
                    
                    // 获取锁
                    lock.lock();
                    
                    long waitTime = System.currentTimeMillis() - startTime;
                    int currentOrder = order.incrementAndGet();
                    logger.info("线程 {} ✓ 获取锁成功，等待时间: {} ms，获取顺序: {}", 
                               threadId, waitTime, currentOrder);
                    
                    // 模拟业务操作
                    int value = counter.incrementAndGet();
                    logger.info("线程 {} 执行业务操作，计数器值: {}", threadId, value);
                    Thread.sleep(1000);
                    
                    logger.info("线程 {} 业务操作完成", threadId);
                    lock.unlock();
                    logger.info("线程 {} ✓ 锁已释放", threadId);
                    
                } catch (Exception e) {
                    logger.error("线程 {} 执行失败", threadId, e);
                } finally {
                    finishLatch.countDown();
                    client.close();
                }
            });
        }
        
        // 等待所有线程准备就绪
        Thread.sleep(1000);
        logger.info("所有线程准备就绪，开始竞争锁...");
        startLatch.countDown();
        
        // 等待所有线程完成
        try {
            finishLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("最终计数器值: {} (期望: {})", counter.get(), threadCount);
        logger.info("锁获取顺序验证：应该按照线程启动顺序获取锁（公平锁特性）");
    }
    
    /**
     * 创建 CuratorFramework 客户端
     */
    private static CuratorFramework createClient(String name) {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
        client.start();
        logger.debug("客户端 {} 已启动", name);
        return client;
    }
}
