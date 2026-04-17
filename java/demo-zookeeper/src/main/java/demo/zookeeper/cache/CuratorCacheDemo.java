package demo.zookeeper.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CuratorCache 演示类
 * 
 * CuratorCache 是 PathChildrenCache 的现代替代品，提供了更好的 API 和性能。
 * 
 * 主要特性：
 * 1. 监听单个节点或子节点
 * 2. 自动缓存节点数据
 * 3. 支持监听节点数据变化和子节点变化
 * 4. 提供简洁的事件处理 API
 */
public class CuratorCacheDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(CuratorCacheDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 演示用的根节点路径
    private static final String ROOT_PATH = "/cache-demo";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== CuratorCache 演示开始 ==========");
        
        // 创建 ZooKeeper 客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
        
        client.start();
        
        try {
            // 等待连接建立
            Thread.sleep(1000);
            
            // 清理旧数据
            cleanup(client);
            
            // 演示 1: 监听单个节点
            demonstrateSingleNodeCache(client);
            
            // 演示 2: 监听子节点
            demonstrateChildrenCache(client);
            
            // 演示 3: 监听节点和子节点（完整监听）
            demonstrateFullCache(client);
            
            logger.info("========== CuratorCache 演示结束 ==========");
        } finally {
            client.close();
        }
    }
    
    /**
     * 演示 1: 监听单个节点
     */
    private static void demonstrateSingleNodeCache(CuratorFramework client) throws Exception {
        logger.info("\n--- 演示 1: 监听单个节点 ---");
        
        String nodePath = ROOT_PATH + "/single-node";
        
        // 创建节点
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(nodePath, "初始数据".getBytes(StandardCharsets.UTF_8));
        
        // 创建 CuratorCache，监听单个节点
        CuratorCache cache = CuratorCache.build(client, nodePath);
        
        // 添加监听器
        // 等待：1次初始化 + 2次更新 = 3次
        CountDownLatch latch = new CountDownLatch(3);
        
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forNodeCache(() -> {
                    logger.info("节点缓存已初始化或刷新");
                    latch.countDown();
                })
                .forInitialized(() -> {
                    logger.info("缓存初始化完成");
                })
                .forCreates(childData -> {
                    logger.info("节点创建: {}", childData.getPath());
                    if (childData.getData() != null) {
                        logger.info("  数据: {}", new String(childData.getData(), StandardCharsets.UTF_8));
                    }
                })
                .forChanges((oldNode, node) -> {
                    logger.info("节点数据变化: {}", node.getPath());
                    if (oldNode != null && oldNode.getData() != null) {
                        logger.info("  旧数据: {}", new String(oldNode.getData(), StandardCharsets.UTF_8));
                    }
                    if (node.getData() != null) {
                        logger.info("  新数据: {}", new String(node.getData(), StandardCharsets.UTF_8));
                    }
                    latch.countDown();
                })
                .forDeletes(oldNode -> {
                    logger.info("节点删除: {}", oldNode.getPath());
                    if (oldNode.getData() != null) {
                        logger.info("  删除的数据: {}", new String(oldNode.getData(), StandardCharsets.UTF_8));
                    }
                    latch.countDown();
                })
                .build();
        
        cache.listenable().addListener(listener);
        cache.start();
        
        // 等待缓存初始化
        Thread.sleep(1000);
        
        // 获取缓存的节点数据
        ChildData cachedData = cache.get(nodePath).orElse(null);
        if (cachedData != null) {
            logger.info("从缓存获取节点数据: {}", new String(cachedData.getData(), StandardCharsets.UTF_8));
        }
        
        // 修改节点数据
        Thread.sleep(500);
        logger.info("修改节点数据...");
        client.setData().forPath(nodePath, "第一次更新".getBytes(StandardCharsets.UTF_8));
        
        Thread.sleep(500);
        logger.info("再次修改节点数据...");
        client.setData().forPath(nodePath, "第二次更新".getBytes(StandardCharsets.UTF_8));
        
        // 等待所有事件处理完成（包括第二次更新）
        // 给事件处理一些时间
        Thread.sleep(500);
        latch.await(5, TimeUnit.SECONDS);
        
        // 获取最新的缓存数据
        cachedData = cache.get(nodePath).orElse(null);
        if (cachedData != null) {
            logger.info("最终缓存数据: {}", new String(cachedData.getData(), StandardCharsets.UTF_8));
        }
        
        // 关闭缓存
        cache.close();
        
        // 清理
        client.delete().forPath(nodePath);
    }
    
    /**
     * 演示 2: 监听子节点
     */
    private static void demonstrateChildrenCache(CuratorFramework client) throws Exception {
        logger.info("\n--- 演示 2: 监听子节点 ---");
        
        String parentPath = ROOT_PATH + "/children";
        
        // 创建父节点
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(parentPath);
        
        // 创建 CuratorCache，监听子节点
        CuratorCache cache = CuratorCache.build(client, parentPath, CuratorCache.Options.SINGLE_NODE_CACHE);
        // 注意：要监听子节点，需要使用 BUILD_INITIAL_CACHE 或 COMPRESSED_DATA 选项
        // 这里我们使用完整监听模式
        cache = CuratorCache.build(client, parentPath);
        
        CountDownLatch latch = new CountDownLatch(6);
        
        // 添加监听器
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forInitialized(() -> {
                    logger.info("子节点缓存初始化完成");
                })
                .forCreates(childData -> {
                    logger.info("子节点创建: {}", childData.getPath());
                    if (childData.getData() != null) {
                        logger.info("  数据: {}", new String(childData.getData(), StandardCharsets.UTF_8));
                    }
                    latch.countDown();
                })
                .forChanges((oldNode, node) -> {
                    logger.info("子节点数据变化: {}", node.getPath());
                    if (oldNode != null && oldNode.getData() != null) {
                        logger.info("  旧数据: {}", new String(oldNode.getData(), StandardCharsets.UTF_8));
                    }
                    if (node.getData() != null) {
                        logger.info("  新数据: {}", new String(node.getData(), StandardCharsets.UTF_8));
                    }
                    latch.countDown();
                })
                .forDeletes(oldNode -> {
                    logger.info("子节点删除: {}", oldNode.getPath());
                    latch.countDown();
                })
                .build();
        
        cache.listenable().addListener(listener);
        cache.start();
        
        // 等待缓存初始化
        Thread.sleep(1000);
        
        // 创建子节点
        logger.info("创建子节点...");
        String child1 = parentPath + "/child1";
        String child2 = parentPath + "/child2";
        String child3 = parentPath + "/child3";
        
        client.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath(child1, "子节点1数据".getBytes(StandardCharsets.UTF_8));
        
        client.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath(child2, "子节点2数据".getBytes(StandardCharsets.UTF_8));
        
        client.create()
                .withMode(CreateMode.PERSISTENT)
                .forPath(child3, "子节点3数据".getBytes(StandardCharsets.UTF_8));
        
        Thread.sleep(1000);
        
        // 获取所有缓存的子节点
        logger.info("当前缓存的子节点:");
        List<ChildData> children = cache.stream()
                .filter(childData -> !childData.getPath().equals(parentPath))
                .toList();
        
        for (ChildData child : children) {
            String data = child.getData() != null ? 
                    new String(child.getData(), StandardCharsets.UTF_8) : "无数据";
            logger.info("  {} -> {}", child.getPath(), data);
        }
        
        // 更新子节点数据
        Thread.sleep(500);
        logger.info("更新子节点数据...");
        client.setData().forPath(child1, "子节点1更新后的数据".getBytes(StandardCharsets.UTF_8));
        
        // 删除子节点
        Thread.sleep(500);
        logger.info("删除子节点...");
        client.delete().forPath(child2);
        
        // 等待所有事件处理完成
        latch.await(5, TimeUnit.SECONDS);
        
        // 获取最新的子节点列表
        Thread.sleep(500);
        logger.info("最终缓存的子节点:");
        children = cache.stream()
                .filter(childData -> !childData.getPath().equals(parentPath))
                .toList();
        
        for (ChildData child : children) {
            String data = child.getData() != null ? 
                    new String(child.getData(), StandardCharsets.UTF_8) : "无数据";
            logger.info("  {} -> {}", child.getPath(), data);
        }
        
        // 关闭缓存
        cache.close();
        
        // 清理
        client.delete().deletingChildrenIfNeeded().forPath(parentPath);
    }
    
    /**
     * 演示 3: 完整监听（节点和子节点）
     */
    private static void demonstrateFullCache(CuratorFramework client) throws Exception {
        logger.info("\n--- 演示 3: 完整监听（节点和子节点） ---");
        
        String rootPath = ROOT_PATH + "/full";
        
        // 创建根节点
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath(rootPath, "根节点数据".getBytes(StandardCharsets.UTF_8));
        
        // 创建 CuratorCache，监听节点和所有子节点
        CuratorCache cache = CuratorCache.build(client, rootPath);
        
        CountDownLatch latch = new CountDownLatch(5);
        
        // 添加监听器
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forInitialized(() -> {
                    logger.info("完整缓存初始化完成");
                })
                .forAll((type, oldData, data) -> {
                    // 统一处理所有事件类型
                    String eventType = switch (type) {
                        case NODE_CREATED -> "节点创建";
                        case NODE_CHANGED -> "节点变化";
                        case NODE_DELETED -> "节点删除";
                    };
                    
                    logger.info("事件: {} - 路径: {}", eventType, data != null ? data.getPath() : 
                               (oldData != null ? oldData.getPath() : "未知"));
                    
                    if (data != null && data.getData() != null) {
                        logger.info("  当前数据: {}", new String(data.getData(), StandardCharsets.UTF_8));
                    }
                    if (oldData != null && oldData.getData() != null) {
                        logger.info("  旧数据: {}", new String(oldData.getData(), StandardCharsets.UTF_8));
                    }
                    
                    latch.countDown();
                })
                .build();
        
        cache.listenable().addListener(listener);
        cache.start();
        
        // 等待缓存初始化
        Thread.sleep(1000);
        
        // 创建子节点
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        
        executor.schedule(() -> {
            try {
                logger.info("创建子节点...");
                String child1 = rootPath + "/sub1";
                client.create()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(child1, "子节点1".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.error("创建子节点失败", e);
            }
        }, 500, TimeUnit.MILLISECONDS);
        
        executor.schedule(() -> {
            try {
                logger.info("更新根节点数据...");
                client.setData().forPath(rootPath, "根节点更新后的数据".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.error("更新根节点失败", e);
            }
        }, 1500, TimeUnit.MILLISECONDS);
        
        executor.schedule(() -> {
            try {
                logger.info("更新子节点数据...");
                String child1 = rootPath + "/sub1";
                client.setData().forPath(child1, "子节点1更新".getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                logger.error("更新子节点失败", e);
            }
        }, 2500, TimeUnit.MILLISECONDS);
        
        // 等待所有事件处理完成
        latch.await(5, TimeUnit.SECONDS);
        
        // 获取所有缓存的数据
        Thread.sleep(500);
        logger.info("所有缓存的数据:");
        cache.stream().forEach(childData -> {
            String data = childData.getData() != null ? 
                    new String(childData.getData(), StandardCharsets.UTF_8) : "无数据";
            logger.info("  {} -> {}", childData.getPath(), data);
        });
        
        executor.shutdown();
        
        // 关闭缓存
        cache.close();
        
        // 清理
        client.delete().deletingChildrenIfNeeded().forPath(rootPath);
    }
    
    /**
     * 清理旧数据
     */
    private static void cleanup(CuratorFramework client) {
        try {
            if (client.checkExists().forPath(ROOT_PATH) != null) {
                client.delete().deletingChildrenIfNeeded().forPath(ROOT_PATH);
                logger.info("已清理旧数据: {}", ROOT_PATH);
            }
        } catch (Exception e) {
            logger.warn("清理旧数据失败: {}", e.getMessage());
        }
    }
}
