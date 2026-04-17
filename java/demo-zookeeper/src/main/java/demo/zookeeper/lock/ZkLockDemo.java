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
 * ZkLock 演示类
 * 演示自定义实现的可重入公平锁的使用
 */
public class ZkLockDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ZkLockDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 锁的路径
    private static final String LOCK_PATH = "/zk-locks/demo-lock";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== ZkLock 可重入公平锁演示开始 ==========");
        
        // 演示 1: 基本锁使用
        demonstrateBasicLock();
        
        // 演示 2: 可重入特性
        demonstrateReentrant();
        
        // 演示 3: 公平锁特性（多线程按顺序获取）
        demonstrateFairLock();
        
        // 演示 4: 锁超时
        demonstrateLockTimeout();
        
        logger.info("========== ZkLock 可重入公平锁演示结束 ==========");
    }
    
    /**
     * 演示 1: 基本锁使用
     */
    private static void demonstrateBasicLock() throws Exception {
        logger.info("\n--- 演示 1: 基本锁使用 ---");
        
        CuratorFramework client = createClient("client-1");
        try (ZkLock lock = new ZkLock(client, LOCK_PATH)) {
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
        try (ZkLock lock = new ZkLock(client, LOCK_PATH)) {
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
    private static void nestedMethod(ZkLock lock, int depth) throws Exception {
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
     * 演示 3: 公平锁特性（多线程按顺序获取）
     */
    private static void demonstrateFairLock() throws Exception {
        logger.info("\n--- 演示 3: 公平锁特性（多线程按顺序获取） ---");
        
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
                ZkLock lock = new ZkLock(client, LOCK_PATH);
                
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
                    
                } catch (Exception e) {
                    logger.error("线程 {} 执行失败", threadId, e);
                } finally {
                    try {
                        if (lock.isLocked()) {
                            lock.unlock();
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
     * 演示 4: 锁超时
     */
    private static void demonstrateLockTimeout() throws Exception {
        logger.info("\n--- 演示 4: 锁超时 ---");
        
        // 第一个客户端持有锁
        CuratorFramework client1 = createClient("client-timeout-1");
        // 第二个客户端尝试获取锁
        CuratorFramework client2 = createClient("client-timeout-2");
        
        try (ZkLock lock1 = new ZkLock(client1, LOCK_PATH);
             ZkLock lock2 = new ZkLock(client2, LOCK_PATH)) {
            // 第一个客户端获取锁
            logger.info("客户端1 获取锁...");
            lock1.lock();
            logger.info("客户端1 ✓ 获取锁成功");
            
            // 第二个客户端尝试获取锁（带超时）
            logger.info("客户端2 尝试获取锁（超时时间: 3秒）...");
            long startTime = System.currentTimeMillis();
            
            boolean acquired = lock2.tryLock(3, TimeUnit.SECONDS);
            long waitTime = System.currentTimeMillis() - startTime;
            
            if (acquired) {
                logger.info("客户端2 ✓ 获取锁成功，等待时间: {} ms", waitTime);
                lock2.unlock();
            } else {
                logger.info("客户端2 ✗ 获取锁超时，等待时间: {} ms", waitTime);
            }
            
            // 第一个客户端释放锁
            Thread.sleep(1000);
            logger.info("客户端1 释放锁...");
            lock1.unlock();
            logger.info("客户端1 ✓ 锁已释放");
            
            // 第二个客户端再次尝试获取锁
            logger.info("客户端2 再次尝试获取锁...");
            startTime = System.currentTimeMillis();
            acquired = lock2.tryLock(3, TimeUnit.SECONDS);
            waitTime = System.currentTimeMillis() - startTime;
            
            if (acquired) {
                logger.info("客户端2 ✓ 获取锁成功，等待时间: {} ms", waitTime);
                lock2.unlock();
                logger.info("客户端2 ✓ 锁已释放");
            } else {
                logger.info("客户端2 ✗ 获取锁超时");
            }
            
        } finally {
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
