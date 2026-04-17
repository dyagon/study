package demo.zookeeper.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 服务注册类
 * 使用 ZooKeeper 临时节点实现服务注册
 * 当服务下线或连接断开时，临时节点会自动删除
 */
public class ServiceRegistry implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);
    
    // 服务注册的根路径
    private static final String REGISTRY_ROOT = "/services";
    
    private final CuratorFramework client;
    private final String connectString;
    private final String servicePath;
    private String registeredPath;
    
    /**
     * 构造函数
     * @param connectString ZooKeeper 连接字符串
     */
    public ServiceRegistry(String connectString) {
        this.connectString = connectString;
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
        this.servicePath = null;
    }
    
    /**
     * 启动客户端
     */
    public void start() {
        client.start();
        logger.info("服务注册客户端已启动，连接地址: {}", connectString);
    }
    
    /**
     * 注册服务
     * @param serviceInfo 服务信息
     * @return 注册的节点路径
     */
    public String register(ServiceInfo serviceInfo) throws Exception {
        // 构建服务路径：/services/{serviceName}/{serviceId}
        String serviceNamePath = REGISTRY_ROOT + "/" + serviceInfo.getServiceName();
        String nodePath = serviceNamePath + "/" + serviceInfo.getServiceId();
        
        // 创建临时节点（EPHEMERAL），会话断开时自动删除
        // 节点数据存储服务地址信息
        String data = serviceInfo.toDataString();
        registeredPath = client.create()
                .creatingParentsIfNeeded()  // 如果父节点不存在，自动创建
                .withMode(CreateMode.EPHEMERAL)  // 临时节点
                .forPath(nodePath, data.getBytes(StandardCharsets.UTF_8));
        
        logger.info("服务注册成功: {} -> {}", serviceInfo, registeredPath);
        return registeredPath;
    }
    
    /**
     * 取消注册服务
     */
    public void unregister() throws Exception {
        if (registeredPath != null) {
            try {
                client.delete().forPath(registeredPath);
                logger.info("服务取消注册成功: {}", registeredPath);
            } catch (Exception e) {
                logger.warn("取消注册服务失败: {}", e.getMessage());
            } finally {
                registeredPath = null;
            }
        }
    }
    
    /**
     * 检查服务是否已注册
     */
    public boolean isRegistered() {
        return registeredPath != null;
    }
    
    /**
     * 获取注册的路径
     */
    public String getRegisteredPath() {
        return registeredPath;
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
            unregister();
        } catch (Exception e) {
            logger.error("关闭服务注册时发生错误", e);
        }
        if (client != null) {
            client.close();
            logger.info("服务注册客户端已关闭");
        }
    }
}
