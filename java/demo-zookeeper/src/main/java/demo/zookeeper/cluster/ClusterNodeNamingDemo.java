package demo.zookeeper.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 集群节点命名演示类
 * 演示如何使用 ZooKeeper 有序节点为集群中的节点分配唯一且有序的 ID
 */
public class ClusterNodeNamingDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeNamingDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 集群名称
    private static final String CLUSTER_NAME = "demo-cluster";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== 集群节点命名演示开始 ==========");
        
        // 创建多个节点模拟集群
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
        CountDownLatch latch = new CountDownLatch(5);
        
        // 启动 5 个节点
        for (int i = 1; i <= 5; i++) {
            final int nodeIndex = i;
            executor.submit(() -> {
                try {
                    ClusterNodeNaming naming = new ClusterNodeNaming(CONNECT_STRING, CLUSTER_NAME);
                    
                    // 添加集群变化监听器
                    naming.addListener((allNodes, currentNode) -> {
                        logger.info("【节点 {}】集群变化通知 - 总节点数: {}, 当前节点序号: {}, 是否 Leader: {}", 
                                   nodeIndex,
                                   allNodes.size(),
                                   currentNode.getSequence(),
                                   isLeader(naming, currentNode, allNodes));
                        
                        // 打印所有节点信息
                        logger.info("【节点 {}】集群节点列表:", nodeIndex);
                        for (int j = 0; j < allNodes.size(); j++) {
                            ClusterNodeInfo node = allNodes.get(j);
                            String role = (j == 0) ? " [Leader]" : "";
                            logger.info("  {}: {} (序号: {}){}", 
                                       j + 1, 
                                       node.getDisplayName(), 
                                       node.getSequence(),
                                       role);
                        }
                    });
                    
                    // 加入集群
                    String nodeData = "节点-" + nodeIndex + "-数据";
                    ClusterNodeInfo nodeInfo = naming.joinCluster(nodeData);
                    
                    logger.info("【节点 {}】已加入集群: {}", nodeIndex, nodeInfo);
                    
                    // 等待一下，让所有节点都注册完成
                    Thread.sleep(2000);
                    
                    // 演示获取集群信息
                    demonstrateClusterInfo(naming, nodeIndex);
                    
                    latch.countDown();
                    
                    // 模拟节点运行一段时间
                    int runTime = 15000 + new Random().nextInt(10000);
                    Thread.sleep(runTime);
                    
                    // 离开集群
                    naming.leaveCluster();
                    logger.info("【节点 {}】已离开集群", nodeIndex);
                    
                    naming.close();
                } catch (Exception e) {
                    logger.error("节点 {} 运行出错", nodeIndex, e);
                    latch.countDown();
                }
            });
            
            // 错开启动时间，模拟节点不同时启动
            Thread.sleep(500);
        }
        
        // 等待所有节点启动
        latch.await();
        Thread.sleep(3000);
        
        // 演示动态节点加入和离开
        logger.info("\n--- 演示：动态节点变化 ---");
        demonstrateDynamicNodes(executor);
        
        // 等待一段时间观察集群变化
        Thread.sleep(20000);
        
        // 清理
        executor.shutdown();
        
        logger.info("========== 集群节点命名演示结束 ==========");
    }
    
    /**
     * 演示获取集群信息
     */
    private static void demonstrateClusterInfo(ClusterNodeNaming naming, int nodeIndex) throws Exception {
        logger.info("\n【节点 {}】--- 集群信息 ---", nodeIndex);
        
        // 获取所有节点
        List<ClusterNodeInfo> allNodes = naming.getAllNodes();
        logger.info("【节点 {}】集群总节点数: {}", nodeIndex, allNodes.size());
        
        // 获取当前节点信息
        ClusterNodeInfo currentNode = naming.getCurrentNode();
        logger.info("【节点 {}】当前节点: {} (序号: {})", 
                   nodeIndex, 
                   currentNode.getDisplayName(), 
                   currentNode.getSequence());
        
        // 获取当前节点位置
        int position = naming.getCurrentNodePosition();
        logger.info("【节点 {}】当前节点位置: {}", nodeIndex, position);
        
        // 判断是否是 Leader
        boolean isLeader = naming.isLeader();
        logger.info("【节点 {}】是否 Leader: {}", nodeIndex, isLeader);
        
        // 获取 Leader 节点
        ClusterNodeInfo leader = naming.getLeader();
        if (leader != null) {
            logger.info("【节点 {}】Leader 节点: {} (序号: {})", 
                       nodeIndex, 
                       leader.getDisplayName(), 
                       leader.getSequence());
        }
        
        // 获取前一个节点
        ClusterNodeInfo previous = naming.getPreviousNode();
        if (previous != null) {
            logger.info("【节点 {}】前一个节点: {} (序号: {})", 
                       nodeIndex, 
                       previous.getDisplayName(), 
                       previous.getSequence());
        }
        
        // 获取后一个节点
        ClusterNodeInfo next = naming.getNextNode();
        if (next != null) {
            logger.info("【节点 {}】后一个节点: {} (序号: {})", 
                       nodeIndex, 
                       next.getDisplayName(), 
                       next.getSequence());
        }
    }
    
    /**
     * 演示动态节点变化
     */
    private static void demonstrateDynamicNodes(ScheduledExecutorService executor) {
        // 创建一个临时节点
        executor.submit(() -> {
            try {
                ClusterNodeNaming naming = new ClusterNodeNaming(CONNECT_STRING, CLUSTER_NAME);
                ClusterNodeInfo nodeInfo = naming.joinCluster("临时节点数据");
                logger.info("临时节点加入: {}", nodeInfo);
                
                // 运行 5 秒后离开
                Thread.sleep(5000);
                
                naming.leaveCluster();
                logger.info("临时节点离开: {}", nodeInfo);
                
                naming.close();
            } catch (Exception e) {
                logger.error("临时节点运行出错", e);
            }
        });
    }
    
    /**
     * 判断节点是否是 Leader
     */
    private static boolean isLeader(ClusterNodeNaming naming, 
                                    ClusterNodeInfo currentNode, 
                                    List<ClusterNodeInfo> allNodes) {
        try {
            return naming.isLeader();
        } catch (Exception e) {
            // 如果无法判断，通过列表判断
            return !allNodes.isEmpty() && 
                   allNodes.get(0).getPath().equals(currentNode.getPath());
        }
    }
}
