package demo.zookeeper.cluster;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 集群节点命名类
 * 使用 ZooKeeper 有序临时节点为集群中的节点分配唯一且有序的 ID
 * 
 * 工作原理：
 * 1. 每个节点在 ZooKeeper 中创建一个有序临时节点（EPHEMERAL_SEQUENTIAL）
 * 2. ZooKeeper 会自动为节点分配一个唯一的序号
 * 3. 节点可以根据序号确定自己在集群中的位置和角色
 */
public class ClusterNodeNaming implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterNodeNaming.class);
    
    // 集群节点根路径
    private static final String CLUSTER_ROOT = "/cluster/nodes";
    
    // 节点路径前缀
    private static final String NODE_PREFIX = "node-";
    
    private final CuratorFramework client;
    private final String connectString;
    private final String clusterName;
    private String registeredPath;
    private ClusterNodeInfo currentNodeInfo;
    private PathChildrenCache childrenCache;
    private final List<ClusterChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * 集群变化监听器接口
     */
    public interface ClusterChangeListener {
        /**
         * 集群节点列表发生变化时调用
         * @param allNodes 所有节点列表（按序号排序）
         * @param currentNode 当前节点信息
         */
        void onClusterChange(List<ClusterNodeInfo> allNodes, ClusterNodeInfo currentNode);
    }
    
    /**
     * 构造函数
     * @param connectString ZooKeeper 连接字符串
     * @param clusterName 集群名称
     */
    public ClusterNodeNaming(String connectString, String clusterName) {
        this.connectString = connectString;
        this.clusterName = clusterName;
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
    }
    
    /**
     * 启动客户端并加入集群
     * @param nodeData 节点数据（可选，用于存储节点元信息）
     * @return 当前节点的信息
     */
    public ClusterNodeInfo joinCluster(String nodeData) throws Exception {
        client.start();
        logger.info("集群节点命名客户端已启动，连接地址: {}, 集群: {}", connectString, clusterName);
        
        // 创建有序临时节点
        String nodePath = CLUSTER_ROOT + "/" + NODE_PREFIX;
        String data = nodeData != null ? nodeData : "";
        
        registeredPath = client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)  // 有序临时节点
                .forPath(nodePath, data.getBytes(StandardCharsets.UTF_8));
        
        // 解析当前节点信息
        currentNodeInfo = ClusterNodeInfo.fromPath(registeredPath, data);
        
        logger.info("节点已加入集群: {} -> 序号: {}", currentNodeInfo.getDisplayName(), currentNodeInfo.getSequence());
        
        // 开始监听集群节点变化
        startWatching();
        
        return currentNodeInfo;
    }
    
    /**
     * 开始监听集群节点变化
     */
    private void startWatching() throws Exception {
        childrenCache = new PathChildrenCache(client, CLUSTER_ROOT, true);
        childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                handleClusterChange(event);
            }
        });
        
        // 初始更新
        updateClusterNodes();
    }
    
    /**
     * 处理集群变化事件
     */
    private void handleClusterChange(PathChildrenCacheEvent event) throws Exception {
        PathChildrenCacheEvent.Type eventType = event.getType();
        
        switch (eventType) {
            case CHILD_ADDED:
                logger.info("节点加入集群: {}", event.getData().getPath());
                break;
            case CHILD_REMOVED:
                logger.info("节点离开集群: {}", event.getData().getPath());
                break;
            case CHILD_UPDATED:
                logger.debug("节点更新: {}", event.getData().getPath());
                break;
            default:
                break;
        }
        
        updateClusterNodes();
    }
    
    /**
     * 更新集群节点列表
     */
    private void updateClusterNodes() throws Exception {
        List<ClusterNodeInfo> allNodes = getAllNodes();
        
        logger.info("集群当前节点数: {}, 当前节点序号: {}", 
                   allNodes.size(), 
                   currentNodeInfo != null ? currentNodeInfo.getSequence() : "N/A");
        
        // 通知监听器
        for (ClusterChangeListener listener : listeners) {
            try {
                listener.onClusterChange(new ArrayList<>(allNodes), currentNodeInfo);
            } catch (Exception e) {
                logger.error("通知集群变化监听器失败", e);
            }
        }
    }
    
    /**
     * 获取所有集群节点（按序号排序）
     */
    public List<ClusterNodeInfo> getAllNodes() throws Exception {
        if (childrenCache == null) {
            return Collections.emptyList();
        }
        
        List<ClusterNodeInfo> nodes = new ArrayList<>();
        
        for (var childData : childrenCache.getCurrentData()) {
            try {
                String path = childData.getPath();
                byte[] data = childData.getData();
                String dataStr = data != null ? new String(data, StandardCharsets.UTF_8) : "";
                ClusterNodeInfo nodeInfo = ClusterNodeInfo.fromPath(path, dataStr);
                nodes.add(nodeInfo);
            } catch (Exception e) {
                logger.warn("解析节点信息失败: {}", e.getMessage());
            }
        }
        
        // 按序号排序
        nodes.sort(Comparator.comparingInt(ClusterNodeInfo::getSequence));
        
        return nodes;
    }
    
    /**
     * 获取当前节点信息
     */
    public ClusterNodeInfo getCurrentNode() {
        return currentNodeInfo;
    }
    
    /**
     * 获取当前节点在集群中的位置（从 0 开始）
     */
    public int getCurrentNodePosition() throws Exception {
        List<ClusterNodeInfo> allNodes = getAllNodes();
        for (int i = 0; i < allNodes.size(); i++) {
            if (allNodes.get(i).getPath().equals(currentNodeInfo.getPath())) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 判断当前节点是否是 Leader（序号最小的节点）
     */
    public boolean isLeader() throws Exception {
        List<ClusterNodeInfo> allNodes = getAllNodes();
        if (allNodes.isEmpty()) {
            return false;
        }
        ClusterNodeInfo leader = allNodes.get(0);
        return leader.getPath().equals(currentNodeInfo.getPath());
    }
    
    /**
     * 获取 Leader 节点信息
     */
    public ClusterNodeInfo getLeader() throws Exception {
        List<ClusterNodeInfo> allNodes = getAllNodes();
        return allNodes.isEmpty() ? null : allNodes.get(0);
    }
    
    /**
     * 获取前一个节点（序号更小的节点）
     */
    public ClusterNodeInfo getPreviousNode() throws Exception {
        List<ClusterNodeInfo> allNodes = getAllNodes();
        int currentPos = getCurrentNodePosition();
        if (currentPos > 0) {
            return allNodes.get(currentPos - 1);
        }
        return null;
    }
    
    /**
     * 获取后一个节点（序号更大的节点）
     */
    public ClusterNodeInfo getNextNode() throws Exception {
        List<ClusterNodeInfo> allNodes = getAllNodes();
        int currentPos = getCurrentNodePosition();
        if (currentPos >= 0 && currentPos < allNodes.size() - 1) {
            return allNodes.get(currentPos + 1);
        }
        return null;
    }
    
    /**
     * 添加集群变化监听器
     */
    public void addListener(ClusterChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除集群变化监听器
     */
    public void removeListener(ClusterChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 离开集群
     */
    public void leaveCluster() throws Exception {
        if (registeredPath != null) {
            try {
                client.delete().forPath(registeredPath);
                logger.info("节点已离开集群: {}", currentNodeInfo.getDisplayName());
            } catch (Exception e) {
                logger.warn("离开集群失败: {}", e.getMessage());
            } finally {
                registeredPath = null;
                currentNodeInfo = null;
            }
        }
    }
    
    /**
     * 获取 CuratorFramework 客户端
     */
    public CuratorFramework getClient() {
        return client;
    }
    
    @Override
    public void close() {
        try {
            if (childrenCache != null) {
                childrenCache.close();
            }
            leaveCluster();
        } catch (Exception e) {
            logger.error("关闭集群节点命名客户端时发生错误", e);
        }
        if (client != null) {
            client.close();
            logger.info("集群节点命名客户端已关闭");
        }
    }
}
