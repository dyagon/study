package demo.zookeeper.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ZooKeeper 客户端封装类，提供节点的增删改查操作
 */
public class ZooKeeperClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperClient.class);
    
    private final CuratorFramework client;
    private final String connectString;
    
    /**
     * 构造函数
     * @param connectString ZooKeeper 连接字符串，例如 "localhost:2181"
     */
    public ZooKeeperClient(String connectString) {
        this.connectString = connectString;
        // 创建 CuratorFramework 客户端
        // 使用指数退避重试策略：初始等待时间 1000ms，最多重试 3 次
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
    }
    
    /**
     * 启动客户端连接
     */
    public void start() {
        client.start();
        logger.info("ZooKeeper 客户端已启动，连接地址: {}", connectString);
    }
    
    /**
     * 创建节点（如果父节点不存在会自动创建）
     * @param path 节点路径
     * @param data 节点数据
     * @param createMode 节点类型（持久化、临时等）
     * @return 创建的实际路径（可能包含序号）
     */
    public String createNode(String path, String data, CreateMode createMode) throws Exception {
        byte[] dataBytes = data != null ? data.getBytes(StandardCharsets.UTF_8) : null;
        String actualPath = client.create()
                .creatingParentsIfNeeded()  // 如果父节点不存在，自动创建
                .withMode(createMode)       // 设置节点类型
                .forPath(path, dataBytes);
        logger.info("创建节点成功: {} (实际路径: {}), 数据: {}", path, actualPath, data);
        return actualPath;
    }
    
    /**
     * 创建持久化节点
     * @param path 节点路径
     * @param data 节点数据
     * @return 创建的实际路径
     */
    public String createPersistentNode(String path, String data) throws Exception {
        return createNode(path, data, CreateMode.PERSISTENT);
    }
    
    /**
     * 创建临时节点（会话结束后自动删除）
     * @param path 节点路径
     * @param data 节点数据
     * @return 创建的实际路径
     */
    public String createEphemeralNode(String path, String data) throws Exception {
        return createNode(path, data, CreateMode.EPHEMERAL);
    }
    
    /**
     * 检查节点是否存在
     * @param path 节点路径
     * @return 如果节点存在返回 Stat 对象，否则返回 null
     */
    public Stat exists(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        if (stat != null) {
            logger.debug("节点存在: {}, 版本: {}, 数据长度: {}", path, stat.getVersion(), stat.getDataLength());
        } else {
            logger.debug("节点不存在: {}", path);
        }
        return stat;
    }
    
    /**
     * 读取节点数据
     * @param path 节点路径
     * @return 节点数据（字符串形式）
     */
    public String readNode(String path) throws Exception {
        byte[] data = client.getData().forPath(path);
        String dataStr = data != null ? new String(data, StandardCharsets.UTF_8) : null;
        logger.info("读取节点: {}, 数据: {}", path, dataStr);
        return dataStr;
    }
    
    /**
     * 读取节点数据和状态信息
     * @param path 节点路径
     * @return 节点数据结果，包含数据和状态信息
     */
    public NodeData readNodeWithStat(String path) throws Exception {
        Stat stat = new Stat();
        byte[] data = client.getData().storingStatIn(stat).forPath(path);
        String dataStr = data != null ? new String(data, StandardCharsets.UTF_8) : null;
        logger.info("读取节点: {}, 数据: {}, 版本: {}", path, dataStr, stat.getVersion());
        return new NodeData(dataStr, stat);
    }
    
    /**
     * 更新节点数据
     * @param path 节点路径
     * @param data 新数据
     * @return 更新后的节点状态
     */
    public Stat updateNode(String path, String data) throws Exception {
        byte[] dataBytes = data != null ? data.getBytes(StandardCharsets.UTF_8) : null;
        Stat stat = client.setData().forPath(path, dataBytes);
        logger.info("更新节点成功: {}, 新数据: {}, 新版本: {}", path, data, stat.getVersion());
        return stat;
    }
    
    /**
     * 带版本号的更新节点数据（乐观锁）
     * @param path 节点路径
     * @param data 新数据
     * @param version 期望的版本号
     * @return 更新后的节点状态
     */
    public Stat updateNodeWithVersion(String path, String data, int version) throws Exception {
        byte[] dataBytes = data != null ? data.getBytes(StandardCharsets.UTF_8) : null;
        Stat stat = client.setData()
                .withVersion(version)  // 只有版本号匹配时才更新
                .forPath(path, dataBytes);
        logger.info("更新节点成功（版本 {}）: {}, 新数据: {}, 新版本: {}", version, path, data, stat.getVersion());
        return stat;
    }
    
    /**
     * 删除节点
     * @param path 节点路径
     */
    public void deleteNode(String path) throws Exception {
        client.delete().forPath(path);
        logger.info("删除节点成功: {}", path);
    }
    
    /**
     * 删除节点及其子节点
     * @param path 节点路径
     */
    public void deleteNodeRecursive(String path) throws Exception {
        client.delete()
                .deletingChildrenIfNeeded()  // 递归删除子节点
                .forPath(path);
        logger.info("递归删除节点成功: {}", path);
    }
    
    /**
     * 带版本号的删除节点（乐观锁）
     * @param path 节点路径
     * @param version 期望的版本号
     */
    public void deleteNodeWithVersion(String path, int version) throws Exception {
        client.delete()
                .withVersion(version)  // 只有版本号匹配时才删除
                .forPath(path);
        logger.info("删除节点成功（版本 {}）: {}", version, path);
    }
    
    /**
     * 获取子节点列表
     * @param path 节点路径
     * @return 子节点名称列表
     */
    public List<String> getChildren(String path) throws Exception {
        List<String> children = client.getChildren().forPath(path);
        logger.info("获取子节点列表: {}, 子节点数量: {}", path, children.size());
        return children;
    }
    
    /**
     * 获取 CuratorFramework 客户端（用于高级操作）
     * @return CuratorFramework 实例
     */
    public CuratorFramework getClient() {
        return client;
    }
    
    @Override
    public void close() {
        if (client != null) {
            client.close();
            logger.info("ZooKeeper 客户端已关闭");
        }
    }
    
    /**
     * 节点数据结果类
     */
    public static class NodeData {
        private final String data;
        private final Stat stat;
        
        public NodeData(String data, Stat stat) {
            this.data = data;
            this.stat = stat;
        }
        
        public String getData() {
            return data;
        }
        
        public Stat getStat() {
            return stat;
        }
        
        @Override
        public String toString() {
            return "NodeData{data='" + data + "', version=" + stat.getVersion() + 
                   ", dataLength=" + stat.getDataLength() + "}";
        }
    }
}
