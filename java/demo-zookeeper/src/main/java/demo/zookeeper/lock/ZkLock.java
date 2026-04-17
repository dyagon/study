package demo.zookeeper.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 基于 ZooKeeper 的可重入公平锁实现
 * 
 * 特性：
 * 1. 可重入：同一线程可以多次获取锁
 * 2. 公平锁：按照获取锁的顺序分配（使用有序临时节点）
 * 3. 自动释放：连接断开时自动释放锁（使用临时节点）
 * 
 * 工作原理：
 * 1. 每个获取锁的请求创建一个有序临时节点
 * 2. 序号最小的节点获得锁
 * 3. 其他节点监听前一个节点的删除事件
 * 4. 前一个节点删除后，下一个节点获得锁
 */
public class ZkLock implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ZkLock.class);
    
    // 锁节点路径前缀
    private static final String LOCK_PREFIX = "lock-";
    
    private final CuratorFramework client;
    private CuratorCache cache;
    private final String lockPath;
    private String currentNodePath;
    private volatile boolean locked = false;
    private final ThreadLocal<String> threadLockPath = new ThreadLocal<>();
    private final AtomicInteger reentrantCount = new AtomicInteger(0);
    
    /**
     * 构造函数
     * @param client CuratorFramework 客户端
     * @param lockPath 锁的路径
     */
    public ZkLock(CuratorFramework client, String lockPath) {
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
        // 检查是否已经持有锁（可重入）
        String currentThreadPath = threadLockPath.get();
        if (currentThreadPath != null && locked) {
            // 同一线程重入
            reentrantCount.incrementAndGet();
            logger.debug("锁重入，当前重入次数: {}", reentrantCount.get());
            return true;
        }
        
        // 创建有序临时节点
        String nodePath = lockPath + '/' + LOCK_PREFIX;
        String createdPath = client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(nodePath);
        
        currentNodePath = createdPath;
        threadLockPath.set(createdPath);
        
        logger.debug("创建锁节点: {}", createdPath);
        
        // 获取序号
        String sequence = extractSequence(createdPath);
        long sequenceNumber = Long.parseLong(sequence);
        
        // 检查是否可以获得锁
        if (tryAcquireLock(timeout, unit)) {
            locked = true;
            reentrantCount.set(1);
            logger.debug("成功获取锁，节点: {}", createdPath);
            return true;
        } else {
            // 超时或失败，删除节点
            try {
                client.delete().forPath(createdPath);
            } catch (Exception e) {
                logger.warn("删除锁节点失败: {}", createdPath, e);
            }
            threadLockPath.remove();
            return false;
        }
    }
    
    /**
     * 尝试获取锁
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return 是否获取成功
     * @throws Exception 获取锁失败
     */
    private boolean tryAcquireLock(long timeout, TimeUnit unit) throws Exception {
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
            if (currentNodePath.endsWith(firstChild)) {
                logger.debug("当前节点是最小的，获得锁");
                return true;
            }
            
            // 检查超时
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMs) {
                logger.debug("获取锁超时");
                return false;
            }
            
            // 找到前一个节点
            String previousNode = findPreviousNode(children, currentNodePath);
            if (previousNode == null) {
                // 没有前一个节点，说明当前节点应该获得锁
                return true;
            }
            
            // 监听前一个节点的删除
            String previousNodePath = lockPath + '/' + previousNode;
            CountDownLatch latch = new CountDownLatch(1);
            
            // 创建缓存监听前一个节点
            CuratorCache previousCache = CuratorCache.build(client, previousNodePath);
            CuratorCacheListener listener = CuratorCacheListener.builder()
                    .forDeletes(oldData -> {
                        logger.debug("前一个节点已删除: {}", previousNodePath);
                        latch.countDown();
                    })
                    .build();
            
            previousCache.listenable().addListener(listener);
            previousCache.start();
            
            try {
                // 再次检查前一个节点是否还存在
                if (client.checkExists().forPath(previousNodePath) == null) {
                    // 前一个节点已经不存在，重新检查
                    previousCache.close();
                    continue;
                }
                
                // 等待前一个节点删除或超时
                long remainingTime = timeoutMs - elapsed;
                if (remainingTime > 0) {
                    boolean notified = latch.await(remainingTime, TimeUnit.MILLISECONDS);
                    if (!notified) {
                        // 超时
                        logger.debug("等待前一个节点删除超时");
                        return false;
                    }
                } else {
                    return false;
                }
            } finally {
                previousCache.close();
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
        String nodeName = path.substring(path.lastIndexOf('/') + 1);
        return nodeName.substring(LOCK_PREFIX.length());
    }
    
    /**
     * 找到前一个节点
     */
    private String findPreviousNode(List<String> sortedChildren, String currentNodePath) {
        String currentNodeName = currentNodePath.substring(currentNodePath.lastIndexOf('/') + 1);
        long currentSequence = Long.parseLong(extractSequence(currentNodeName));
        
        for (String child : sortedChildren) {
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
        if (!locked) {
            throw new IllegalMonitorStateException("当前线程未持有锁");
        }
        
        // 检查重入次数
        int count = reentrantCount.decrementAndGet();
        if (count > 0) {
            logger.debug("锁重入释放，剩余重入次数: {}", count);
            return;
        }
        
        // 完全释放锁
        if (currentNodePath != null) {
            try {
                client.delete().forPath(currentNodePath);
                logger.debug("锁节点已删除: {}", currentNodePath);
            } catch (KeeperException.NoNodeException e) {
                // 节点已经不存在，忽略
                logger.debug("锁节点已不存在: {}", currentNodePath);
            }
            currentNodePath = null;
        }
        
        locked = false;
        threadLockPath.remove();
        logger.debug("锁已完全释放");
    }
    
    /**
     * 检查是否持有锁
     * @return 是否持有锁
     */
    public boolean isLocked() {
        return locked && reentrantCount.get() > 0;
    }
    
    /**
     * 获取重入次数
     * @return 重入次数
     */
    public int getReentrantCount() {
        return reentrantCount.get();
    }
    
    @Override
    public void close() throws Exception {
        // 如果还持有锁，释放它
        if (locked) {
            try {
                unlock();
            } catch (Exception e) {
                logger.warn("关闭时释放锁失败", e);
            }
        }
        
        if (cache != null) {
            cache.close();
        }
    }
}
