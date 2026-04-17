package demo.zookeeper.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可重入分布式锁演示类
 * 
 * 使用 Curator 的 InterProcessMutex 实现基于 ZooKeeper 的可重入分布式锁
 * 
 * 特性：
 * 1. 可重入：同一线程可以多次获取锁
 * 2. 分布式：支持跨进程、跨机器的锁
 * 3. 自动释放：连接断开时自动释放锁
 * 4. 公平锁：按照获取锁的顺序分配
 */
public class ReentrantLockDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ReentrantLockDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 锁的根路径
    private static final String LOCK_PATH = "/locks/reentrant-lock";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== 可重入分布式锁演示开始 ==========");
        
        // 演示 1: 基本锁使用
        demonstrateBasicLock();
        
        // 演示 2: 可重入特性
        demonstrateReentrant();
        
        // 演示 3: 多线程锁竞争
        demonstrateMultiThreadLock();
        
        // 演示 4: 锁超时
        demonstrateLockTimeout();
        
        logger.info("========== 可重入分布式锁演示结束 ==========");
    }
    
    /**
     * 演示 1: 基本锁使用
     */
    private static void demonstrateBasicLock() throws Exception {
        logger.info("\n--- 演示 1: 基本锁使用 ---");
        
        CuratorFramework client = createClient("client-1");
        InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH);
        
        try {
            logger.info("尝试获取锁...");
            // 获取锁（阻塞直到获取成功）
            lock.acquire();
            logger.info("✓ 成功获取锁");
            
            // 模拟业务操作
            logger.info("执行业务操作...");
            Thread.sleep(2000);
            logger.info("业务操作完成");
            
        } finally {
            // 释放锁
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
                logger.info("✓ 锁已释放");
            }
            client.close();
        }
    }
    
    /**
     * 演示 2: 可重入特性
     */
    private static void demonstrateReentrant() throws Exception {
        logger.info("\n--- 演示 2: 可重入特性 ---");
        
        CuratorFramework client = createClient("client-2");
        InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH);
        
        try {
            logger.info("第一次获取锁...");
            lock.acquire();
            logger.info("✓ 第一次获取锁成功");
            
            logger.info("第二次获取锁（可重入）...");
            lock.acquire();
            logger.info("✓ 第二次获取锁成功（可重入）");
            
            logger.info("第三次获取锁（可重入）...");
            lock.acquire();
            logger.info("✓ 第三次获取锁成功（可重入）");
            
            // 模拟嵌套调用
            nestedMethod(lock, 3);
            
        } finally {
            // 需要释放相同次数的锁
            // 注意：nestedMethod 中已经释放了它获取的锁，这里只需要释放 main 方法中获取的 3 次
            logger.info("开始释放锁（main 方法中获取的）...");
            int releaseCount = 0;
            while (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                    releaseCount++;
                    logger.info("释放第 {} 次锁", releaseCount);
                } catch (IllegalMonitorStateException e) {
                    // 如果已经释放完所有锁，退出循环
                    logger.warn("锁已全部释放: {}", e.getMessage());
                    break;
                }
            }
            logger.info("✓ 所有锁已释放（共释放 {} 次）", releaseCount);
            client.close();
        }
    }
    
    /**
     * 嵌套方法，演示可重入
     */
    private static void nestedMethod(InterProcessMutex lock, int depth) throws Exception {
        if (depth <= 0) {
            return;
        }
        
        logger.info("嵌套方法深度 {} - 获取锁", depth);
        lock.acquire();
        logger.info("嵌套方法深度 {} - 锁获取成功", depth);
        
        Thread.sleep(500);
        
        nestedMethod(lock, depth - 1);
        
        lock.release();
        logger.info("嵌套方法深度 {} - 锁已释放", depth);
    }
    
    /**
     * 演示 3: 多线程锁竞争
     */
    private static void demonstrateMultiThreadLock() throws Exception {
        logger.info("\n--- 演示 3: 多线程锁竞争 ---");
        
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger counter = new AtomicInteger(0);
        
        // 创建多个线程竞争锁
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                CuratorFramework client = createClient("client-thread-" + threadId);
                InterProcessMutex lock = new InterProcessMutex(client, LOCK_PATH);
                
                try {
                    // 等待所有线程准备就绪
                    try {
                        startLatch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    
                    logger.info("线程 {} 尝试获取锁...", threadId);
                    long startTime = System.currentTimeMillis();
                    
                    // 获取锁
                    lock.acquire();
                    
                    long waitTime = System.currentTimeMillis() - startTime;
                    logger.info("线程 {} ✓ 获取锁成功，等待时间: {} ms", threadId, waitTime);
                    
                    // 模拟业务操作
                    int value = counter.incrementAndGet();
                    logger.info("线程 {} 执行业务操作，计数器值: {}", threadId, value);
                    Thread.sleep(1000);
                    
                    logger.info("线程 {} 业务操作完成", threadId);
                    
                } catch (Exception e) {
                    logger.error("线程 {} 执行失败", threadId, e);
                } finally {
                    try {
                        if (lock.isAcquiredInThisProcess()) {
                            lock.release();
                            logger.info("线程 {} ✓ 锁已释放", threadId);
                        }
                        client.close();
                    } catch (Exception e) {
                        logger.error("线程 {} 释放锁失败", threadId, e);
                    }
                    finishLatch.countDown();
                }
            });
        }
        
        // 等待所有线程准备就绪
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
    }
    
    /**
     * 演示 4: 锁超时
     */
    private static void demonstrateLockTimeout() throws Exception {
        logger.info("\n--- 演示 4: 锁超时 ---");
        
        // 第一个客户端持有锁
        CuratorFramework client1 = createClient("client-timeout-1");
        InterProcessMutex lock1 = new InterProcessMutex(client1, LOCK_PATH);
        
        // 第二个客户端尝试获取锁
        CuratorFramework client2 = createClient("client-timeout-2");
        InterProcessMutex lock2 = new InterProcessMutex(client2, LOCK_PATH);
        
        try {
            // 第一个客户端获取锁
            logger.info("客户端1 获取锁...");
            lock1.acquire();
            logger.info("客户端1 ✓ 获取锁成功");
            
            // 第二个客户端尝试获取锁（带超时）
            logger.info("客户端2 尝试获取锁（超时时间: 3秒）...");
            long startTime = System.currentTimeMillis();
            
            boolean acquired = lock2.acquire(3, TimeUnit.SECONDS);
            long waitTime = System.currentTimeMillis() - startTime;
            
            if (acquired) {
                logger.info("客户端2 ✓ 获取锁成功，等待时间: {} ms", waitTime);
                lock2.release();
            } else {
                logger.info("客户端2 ✗ 获取锁超时，等待时间: {} ms", waitTime);
            }
            
            // 第一个客户端释放锁
            Thread.sleep(1000);
            logger.info("客户端1 释放锁...");
            lock1.release();
            logger.info("客户端1 ✓ 锁已释放");
            
            // 第二个客户端再次尝试获取锁
            logger.info("客户端2 再次尝试获取锁...");
            startTime = System.currentTimeMillis();
            acquired = lock2.acquire(3, TimeUnit.SECONDS);
            waitTime = System.currentTimeMillis() - startTime;
            
            if (acquired) {
                logger.info("客户端2 ✓ 获取锁成功，等待时间: {} ms", waitTime);
                lock2.release();
                logger.info("客户端2 ✓ 锁已释放");
            } else {
                logger.info("客户端2 ✗ 获取锁超时");
            }
            
        } finally {
            if (lock1.isAcquiredInThisProcess()) {
                lock1.release();
            }
            if (lock2.isAcquiredInThisProcess()) {
                lock2.release();
            }
            client1.close();
            client2.close();
        }
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
