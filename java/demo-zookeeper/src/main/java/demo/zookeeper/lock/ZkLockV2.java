package demo.zookeeper.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于 ZooKeeper 的可重入公平锁实现 V2
 * 
 * 修复了 V1 版本的线程安全问题：
 * 1. 使用 ThreadLocal 和 ConcurrentHashMap 管理每个线程的锁状态
 * 2. 使用轻量级 Watcher 替代 CuratorCache
 * 3. 确保多线程安全
 * 
 * 特性：
 * 1. 可重入：同一线程可以多次获取锁
 * 2. 公平锁：按照获取锁的顺序分配（使用有序临时节点）
 * 3. 自动释放：连接断开时自动释放锁（使用临时节点）
 * 4. 线程安全：支持多线程并发使用同一个锁实例
 * 
 * 工作原理：
 * 1. 每个获取锁的请求创建一个有序临时节点
 * 2. 序号最小的节点获得锁
 * 3. 其他节点使用 Watcher 监听前一个节点的删除事件
 * 4. 前一个节点删除后，下一个节点获得锁
 */
public class ZkLockV2 implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ZkLockV2.class);
    
    // 锁节点路径前缀
    private static final String LOCK_PREFIX = "lock-";
    
    // 每个线程的锁状态
    private static class LockData {
        final String nodePath;
        final AtomicInteger reentrantCount;
        volatile boolean locked;
        
        LockData(String nodePath) {
            this.nodePath = nodePath;
            this.reentrantCount = new AtomicInteger(0);
            this.locked = false;
        }
    }
    
    private final CuratorFramework client;
    private final String lockPath;
    // 使用 ConcurrentHashMap 管理每个线程的锁状态，确保线程安全
    private final ConcurrentHashMap<Thread, LockData> threadLockData = new ConcurrentHashMap<>();
    
    /**
     * 构造函数
     * @param client CuratorFramework 客户端
     * @param lockPath 锁的路径
     */
    public ZkLockV2(CuratorFramework client, String lockPath) {
        this.client = client;
        this.lockPath = lockPath;
    }
    
    /**
     * 获取锁（阻塞直到获取成功）
     * @throws Exception 获取锁失败
     */
    public void lock() throws Exception {
        lock(-1, null);
    }
    
    /**
     * 尝试获取锁（带超时）
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取成功
     * @throws Exception 获取锁失败
     */
    public boolean tryLock(long timeout, TimeUnit unit) throws Exception {
        return lock(timeout, unit);
    }
    
    /**
     * 尝试获取锁（立即返回）
     * @return 是否获取成功
     * @throws Exception 获取锁失败
     */
    public boolean tryLock() throws Exception {
        return lock(0, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 内部锁获取方法
     * @param timeout 超时时间（-1 表示无限等待）
     * @param unit 时间单位
     * @return 是否获取成功
     * @throws Exception 获取锁失败
     */
    private boolean lock(long timeout, TimeUnit unit) throws Exception {
        Thread currentThread = Thread.currentThread();
        
        // 检查是否已经持有锁（可重入）
        LockData lockData = threadLockData.get(currentThread);
        if (lockData != null && lockData.locked) {
            // 同一线程重入
            lockData.reentrantCount.incrementAndGet();
            logger.debug("线程 {} 锁重入，当前重入次数: {}", currentThread.getName(), lockData.reentrantCount.get());
            return true;
        }
        
        // 创建有序临时节点
        String nodePath = lockPath + '/' + LOCK_PREFIX;
        String createdPath = client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(nodePath);
        
        logger.debug("线程 {} 创建锁节点: {}", currentThread.getName(), createdPath);
        
        // 创建或更新线程的锁数据
        lockData = new LockData(createdPath);
        threadLockData.put(currentThread, lockData);
        
        // 检查是否可以获得锁
        if (tryAcquireLock(lockData, timeout, unit)) {
            lockData.locked = true;
            lockData.reentrantCount.set(1);
            logger.debug("线程 {} 成功获取锁，节点: {}", currentThread.getName(), createdPath);
            return true;
        } else {
            // 超时或失败，删除节点
            try {
                client.delete().forPath(createdPath);
            } catch (Exception e) {
                logger.warn("线程 {} 删除锁节点失败: {}", currentThread.getName(), createdPath, e);
            }
            threadLockData.remove(currentThread);
            return false;
        }
    }
    
    /**
     * 尝试获取锁
     * @param lockData 当前线程的锁数据
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取成功
     * @throws Exception 获取锁失败
     */
    private boolean tryAcquireLock(LockData lockData, long timeout, TimeUnit unit) throws Exception {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout < 0 ? Long.MAX_VALUE : unit.toMillis(timeout);
        
        while (true) {
            // 获取所有锁节点
            List<String> children = getSortedChildren();
            
            if (children.isEmpty()) {
                return false;
            }
            
            // 检查当前节点是否是最小的（可以获得锁）
            String firstChild = children.get(0);
            String currentNodeName = lockData.nodePath.substring(lockData.nodePath.lastIndexOf('/') + 1);
            if (currentNodeName.equals(firstChild)) {
                logger.debug("线程 {} 当前节点是最小的，获得锁", Thread.currentThread().getName());
                return true;
            }
            
            // 检查超时
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMs) {
                logger.debug("线程 {} 获取锁超时", Thread.currentThread().getName());
                return false;
            }
            
            // 找到前一个节点
            String previousNode = findPreviousNode(children, currentNodeName);
            if (previousNode == null) {
                // 没有前一个节点，说明当前节点应该获得锁
                return true;
            }
            
            // 使用轻量级 Watcher 监听前一个节点的删除
            String previousNodePath = lockPath + '/' + previousNode;
            CountDownLatch latch = new CountDownLatch(1);
            
            // 使用 Watcher 而不是 CuratorCache，性能更好
            Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDeleted) {
                        logger.debug("线程 {} 前一个节点已删除: {}", Thread.currentThread().getName(), previousNodePath);
                        latch.countDown();
                    }
                }
            };
            
            // 检查前一个节点是否存在，并注册 Watcher
            try {
                if (client.checkExists().usingWatcher(watcher).forPath(previousNodePath) == null) {
                    // 前一个节点已经不存在，重新检查
                    continue;
                }
            } catch (KeeperException.NoNodeException e) {
                // 节点不存在，重新检查
                continue;
            }
            
            // 等待前一个节点删除或超时
            long remainingTime = timeoutMs - elapsed;
            if (remainingTime > 0) {
                boolean notified = latch.await(remainingTime, TimeUnit.MILLISECONDS);
                if (!notified) {
                    // 超时
                    logger.debug("线程 {} 等待前一个节点删除超时", Thread.currentThread().getName());
                    return false;
                }
            } else {
                return false;
            }
        }
    }
    
    /**
     * 获取排序后的子节点列表
     */
    private List<String> getSortedChildren() throws Exception {
        List<String> children = client.getChildren().forPath(lockPath);
        return children.stream()
                .sorted((a, b) -> {
                    long seqA = Long.parseLong(extractSequence(a));
                    long seqB = Long.parseLong(extractSequence(b));
                    return Long.compare(seqA, seqB);
                })
                .toList();
    }
    
    /**
     * 从路径中提取序号
     */
    private String extractSequence(String path) {
        return path.substring(LOCK_PREFIX.length());
    }
    
    /**
     * 找到前一个节点（序号最大的小于当前节点的节点）
     */
    private String findPreviousNode(List<String> sortedChildren, String currentNodeName) {
        long currentSequence = Long.parseLong(extractSequence(currentNodeName));
        
        // 从后往前找，找到第一个序号小于当前节点的
        for (int i = sortedChildren.size() - 1; i >= 0; i--) {
            String child = sortedChildren.get(i);
            long childSequence = Long.parseLong(extractSequence(child));
            if (childSequence < currentSequence) {
                return child;
            }
        }
        return null;
    }
    
    /**
     * 释放锁
     * @throws Exception 释放锁失败
     */
    public void unlock() throws Exception {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadLockData.get(currentThread);
        
        if (lockData == null || !lockData.locked) {
            throw new IllegalMonitorStateException("当前线程未持有锁");
        }
        
        // 检查重入次数
        int count = lockData.reentrantCount.decrementAndGet();
        if (count > 0) {
            logger.debug("线程 {} 锁重入释放，剩余重入次数: {}", currentThread.getName(), count);
            return;
        }
        
        // 完全释放锁
        try {
            client.delete().forPath(lockData.nodePath);
            logger.debug("线程 {} 锁节点已删除: {}", currentThread.getName(), lockData.nodePath);
        } catch (KeeperException.NoNodeException e) {
            // 节点已经不存在，忽略
            logger.debug("线程 {} 锁节点已不存在: {}", currentThread.getName(), lockData.nodePath);
        }
        
        lockData.locked = false;
        threadLockData.remove(currentThread);
        logger.debug("线程 {} 锁已完全释放", currentThread.getName());
    }
    
    /**
     * 检查当前线程是否持有锁
     * @return 是否持有锁
     */
    public boolean isLocked() {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadLockData.get(currentThread);
        return lockData != null && lockData.locked && lockData.reentrantCount.get() > 0;
    }
    
    /**
     * 获取当前线程的重入次数
     * @return 重入次数
     */
    public int getReentrantCount() {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadLockData.get(currentThread);
        return lockData != null ? lockData.reentrantCount.get() : 0;
    }
    
    /**
     * 检查是否有任何线程持有锁
     * @return 是否有线程持有锁
     */
    public boolean hasAnyLock() {
        return threadLockData.values().stream()
                .anyMatch(lockData -> lockData.locked && lockData.reentrantCount.get() > 0);
    }
    
    @Override
    public void close() throws Exception {
        // 释放当前线程的锁（如果持有）
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadLockData.get(currentThread);
        if (lockData != null && lockData.locked) {
            try {
                unlock();
            } catch (Exception e) {
                logger.warn("关闭时释放锁失败", e);
            }
        }
    }
}
