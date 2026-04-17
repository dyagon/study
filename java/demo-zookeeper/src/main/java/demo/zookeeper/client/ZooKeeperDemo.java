package demo.zookeeper.client;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZooKeeper 操作演示类
 * 演示如何使用 Curator 进行节点的增删改查操作
 */
public class ZooKeeperDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperDemo.class);
    
    // ZooKeeper 连接地址，默认本地
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 演示用的根节点路径
    private static final String ROOT_PATH = "/demo";
    
    public static void main(String[] args) {
        ZooKeeperClient client = new ZooKeeperClient(CONNECT_STRING);
        
        try {
            // 启动客户端
            client.start();
            
            // 等待连接建立
            Thread.sleep(1000);
            
            logger.info("========== ZooKeeper 操作演示开始 ==========");
            
            // 1. 创建节点（增）
            demoCreateNodes(client);
            
            // 2. 读取节点（查）
            demoReadNodes(client);
            
            // 3. 更新节点（改）
            demoUpdateNodes(client);
            
            // 4. 列出子节点
            demoListChildren(client);
            
            // 5. 删除节点（删）
            demoDeleteNodes(client);
            
            logger.info("========== ZooKeeper 操作演示结束 ==========");
            
        } catch (Exception e) {
            logger.error("操作过程中发生错误", e);
        } finally {
            // 关闭客户端
            client.close();
        }
    }
    
    /**
     * 演示创建节点操作
     */
    private static void demoCreateNodes(ZooKeeperClient client) throws Exception {
        logger.info("\n--- 演示：创建节点 ---");
        
        // 创建持久化节点
        String path1 = ROOT_PATH + "/persistent-node";
        client.createPersistentNode(path1, "这是持久化节点的数据");
        
        // 创建带子路径的节点（父节点会自动创建）
        String path2 = ROOT_PATH + "/parent/child";
        client.createPersistentNode(path2, "这是子节点的数据");
        
        // 创建临时节点
        String path3 = ROOT_PATH + "/ephemeral-node";
        client.createEphemeralNode(path3, "这是临时节点的数据");
        
        // 创建有序节点
        String path4 = ROOT_PATH + "/sequential-";
        String actualPath = client.createNode(path4, "有序节点数据", CreateMode.PERSISTENT_SEQUENTIAL);
        logger.info("创建的有序节点实际路径: {}", actualPath);
    }
    
    /**
     * 演示读取节点操作
     */
    private static void demoReadNodes(ZooKeeperClient client) throws Exception {
        logger.info("\n--- 演示：读取节点 ---");
        
        String path = ROOT_PATH + "/persistent-node";
        
        // 检查节点是否存在
        if (client.exists(path) != null) {
            // 读取节点数据
            String data = client.readNode(path);
            logger.info("读取到的数据: {}", data);
            
            // 读取节点数据和状态信息
            ZooKeeperClient.NodeData nodeData = client.readNodeWithStat(path);
            logger.info("节点详细信息: {}", nodeData);
            logger.info("节点版本号: {}", nodeData.getStat().getVersion());
            logger.info("节点创建时间: {}", nodeData.getStat().getCtime());
            logger.info("节点修改时间: {}", nodeData.getStat().getMtime());
        } else {
            logger.warn("节点不存在: {}", path);
        }
    }
    
    /**
     * 演示更新节点操作
     */
    private static void demoUpdateNodes(ZooKeeperClient client) throws Exception {
        logger.info("\n--- 演示：更新节点 ---");
        
        String path = ROOT_PATH + "/persistent-node";
        
        // 读取当前版本
        ZooKeeperClient.NodeData nodeData = client.readNodeWithStat(path);
        int currentVersion = nodeData.getStat().getVersion();
        logger.info("当前节点版本: {}", currentVersion);
        
        // 更新节点数据
        client.updateNode(path, "更新后的数据 - " + System.currentTimeMillis());
        
        // 再次读取验证
        String updatedData = client.readNode(path);
        logger.info("更新后的数据: {}", updatedData);
        
        // 使用版本号更新（乐观锁）
        try {
            client.updateNodeWithVersion(path, "使用版本号更新的数据", currentVersion);
            logger.warn("版本号更新应该失败，因为版本号已经改变");
        } catch (Exception e) {
            logger.info("版本号不匹配，更新失败（这是预期的）: {}", e.getMessage());
        }
        
        // 使用正确的版本号更新
        ZooKeeperClient.NodeData latestData = client.readNodeWithStat(path);
        client.updateNodeWithVersion(path, "使用正确版本号更新的数据", latestData.getStat().getVersion());
        logger.info("使用正确版本号更新成功");
    }
    
    /**
     * 演示列出子节点操作
     */
    private static void demoListChildren(ZooKeeperClient client) throws Exception {
        logger.info("\n--- 演示：列出子节点 ---");
        
        // 列出根节点下的所有子节点
        var children = client.getChildren(ROOT_PATH);
        logger.info("{} 下的子节点: {}", ROOT_PATH, children);
        
        // 列出父节点下的子节点
        String parentPath = ROOT_PATH + "/parent";
        if (client.exists(parentPath) != null) {
            var parentChildren = client.getChildren(parentPath);
            logger.info("{} 下的子节点: {}", parentPath, parentChildren);
        }
    }
    
    /**
     * 演示删除节点操作
     */
    private static void demoDeleteNodes(ZooKeeperClient client) throws Exception {
        logger.info("\n--- 演示：删除节点 ---");
        
        // 删除单个节点（如果节点有子节点，会失败）
        String path1 = ROOT_PATH + "/persistent-node";
        try {
            client.deleteNode(path1);
            logger.info("删除节点成功: {}", path1);
        } catch (Exception e) {
            logger.warn("删除节点失败（可能节点不存在或仍有子节点）: {}", e.getMessage());
        }
        
        // 递归删除节点及其所有子节点
        String path2 = ROOT_PATH + "/parent";
        if (client.exists(path2) != null) {
            client.deleteNodeRecursive(path2);
            logger.info("递归删除节点成功: {}", path2);
        }
        
        // 使用版本号删除（乐观锁）
        String path3 = ROOT_PATH + "/ephemeral-node";
        if (client.exists(path3) != null) {
            ZooKeeperClient.NodeData nodeData = client.readNodeWithStat(path3);
            try {
                client.deleteNodeWithVersion(path3, nodeData.getStat().getVersion());
                logger.info("使用版本号删除节点成功: {}", path3);
            } catch (Exception e) {
                logger.warn("使用版本号删除失败: {}", e.getMessage());
            }
        }
        
        // 清理：删除根节点（如果存在）
        if (client.exists(ROOT_PATH) != null) {
            var children = client.getChildren(ROOT_PATH);
            if (children.isEmpty()) {
                client.deleteNode(ROOT_PATH);
                logger.info("清理根节点: {}", ROOT_PATH);
            } else {
                logger.info("根节点下仍有子节点，跳过删除: {}", children);
            }
        }
    }
}
