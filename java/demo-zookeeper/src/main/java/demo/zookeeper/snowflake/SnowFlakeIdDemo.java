package demo.zookeeper.snowflake;

import demo.zookeeper.cluster.ClusterNodeInfo;
import demo.zookeeper.cluster.ClusterNodeNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SnowFlake ID 生成演示类
 * 演示如何使用集群节点命名服务生成 SnowFlake ID
 */
public class SnowFlakeIdDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(SnowFlakeIdDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 集群名称
    private static final String CLUSTER_NAME = "snowflake-cluster";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== SnowFlake ID 生成演示开始 ==========");
        
        // 演示 1: 单节点生成 ID
        demonstrateSingleNode();
        
        // 演示 2: 多节点并发生成 ID（验证唯一性）
        demonstrateMultiNodeConcurrency();
        
        // 演示 3: 使用集群节点命名服务
        demonstrateClusterNodeNaming();
        
        logger.info("========== SnowFlake ID 生成演示结束 ==========");
    }
    
    /**
     * 演示单节点生成 ID
     */
    private static void demonstrateSingleNode() throws Exception {
        logger.info("\n--- 演示 1: 单节点生成 ID ---");
        
        SnowFlakeIdGenerator generator = new SnowFlakeIdGenerator(1);
        
        // 生成 10 个 ID
        logger.info("生成 10 个 ID:");
        for (int i = 0; i < 10; i++) {
            long id = generator.nextId();
            SnowFlakeIdGenerator.IdParts parts = SnowFlakeIdGenerator.parseId(id);
            logger.info("ID {}: {} (时间戳: {}, 机器ID: {}, 序列号: {})", 
                       i + 1, 
                       id,
                       parts.getTimestamp(),
                       parts.getMachineId(),
                       parts.getSequence());
        }
        
        // 测试性能
        logger.info("\n性能测试：生成 100,000 个 ID");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            generator.nextId();
        }
        long endTime = System.currentTimeMillis();
        logger.info("耗时: {} ms, 平均: {} ID/ms", 
                   endTime - startTime, 
                   100000.0 / (endTime - startTime));
    }
    
    /**
     * 演示多节点并发生成 ID（验证唯一性）
     */
    private static void demonstrateMultiNodeConcurrency() throws Exception {
        logger.info("\n--- 演示 2: 多节点并发生成 ID（验证唯一性） ---");
        
        int nodeCount = 5;
        int idsPerNode = 10000;
        ExecutorService executor = Executors.newFixedThreadPool(nodeCount);
        CountDownLatch latch = new CountDownLatch(nodeCount);
        List<Set<Long>> allIds = new CopyOnWriteArrayList<>();
        
        // 每个节点生成 ID
        for (int i = 0; i < nodeCount; i++) {
            final int nodeId = i;
            executor.submit(() -> {
                try {
                    SnowFlakeIdGenerator generator = new SnowFlakeIdGenerator(nodeId);
                    // 使用 ConcurrentHashMap.newKeySet() 创建线程安全的 Set
                    Set<Long> ids = ConcurrentHashMap.newKeySet();
                    
                    for (int j = 0; j < idsPerNode; j++) {
                        ids.add(generator.nextId());
                    }
                    
                    allIds.add(ids);
                    logger.info("节点 {} 生成完成，生成了 {} 个唯一 ID", nodeId, ids.size());
                    latch.countDown();
                } catch (Exception e) {
                    logger.error("节点 {} 生成 ID 失败", nodeId, e);
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        // 验证所有 ID 的唯一性
        Set<Long> allUniqueIds = allIds.stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        
        int totalIds = allIds.stream().mapToInt(Set::size).sum();
        logger.info("总共生成 {} 个 ID，唯一 ID 数量: {}", totalIds, allUniqueIds.size());
        
        if (totalIds == allUniqueIds.size()) {
            logger.info("✓ 所有 ID 都是唯一的！");
        } else {
            logger.warn("✗ 发现重复 ID！");
        }
    }
    
    /**
     * 演示使用集群节点命名服务
     */
    private static void demonstrateClusterNodeNaming() throws Exception {
        logger.info("\n--- 演示 3: 使用集群节点命名服务 ---");
        
        int nodeCount = 3;
        ExecutorService executor = Executors.newFixedThreadPool(nodeCount);
        CountDownLatch latch = new CountDownLatch(nodeCount);
        List<ClusterSnowFlakeIdGenerator> generators = new CopyOnWriteArrayList<>();
        
        // 启动多个节点
        for (int i = 0; i < nodeCount; i++) {
            final int nodeIndex = i;
            executor.submit(() -> {
                try {
                    // 创建节点命名服务并加入集群
                    ClusterNodeNaming naming = new ClusterNodeNaming(CONNECT_STRING, CLUSTER_NAME);
                    naming.joinCluster("节点-" + nodeIndex);
                    
                    // 等待节点注册完成
                    Thread.sleep(1000);
                    
                    // 创建 SnowFlake ID 生成器
                    ClusterSnowFlakeIdGenerator generator = new ClusterSnowFlakeIdGenerator(naming);
                    
                    ClusterNodeInfo nodeInfo = generator.getCurrentNodeInfo();
                    logger.info("节点 {} 初始化完成，节点序号: {}, 机器 ID: {}", 
                               nodeIndex, 
                               nodeInfo.getSequence(), 
                               generator.getMachineId());
                    
                    // 生成一些 ID
                    logger.info("节点 {} 生成的 ID:", nodeIndex);
                    for (int j = 0; j < 5; j++) {
                        long id = generator.nextId();
                        SnowFlakeIdGenerator.IdParts parts = generator.parseId(id);
                        logger.info("  ID {}: {} (机器ID: {}, 序列号: {})", 
                                   j + 1, id, parts.getMachineId(), parts.getSequence());
                    }
                    
                    generators.add(generator);
                    latch.countDown();
                    
                    // 保持运行一段时间
                    Thread.sleep(10000);
                    
                    // 关闭
                    naming.close();
                } catch (Exception e) {
                    logger.error("节点 {} 运行失败", nodeIndex, e);
                    latch.countDown();
                }
            });
            
            // 错开启动时间
            Thread.sleep(500);
        }
        
        latch.await();
        Thread.sleep(2000);
        
        // 验证不同节点的机器 ID 不同
        Set<Long> machineIds = generators.stream()
                .map(ClusterSnowFlakeIdGenerator::getMachineId)
                .collect(Collectors.toSet());
        
        logger.info("\n所有节点的机器 ID: {}", machineIds);
        if (machineIds.size() == generators.size()) {
            logger.info("✓ 所有节点都有不同的机器 ID！");
        } else {
            logger.warn("✗ 发现重复的机器 ID！");
        }
        
        executor.shutdown();
    }
}
