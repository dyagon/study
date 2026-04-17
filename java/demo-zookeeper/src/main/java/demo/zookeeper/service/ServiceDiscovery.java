package demo.zookeeper.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务发现类
 * 监听 ZooKeeper 节点变化，自动更新可用服务列表
 */
public class ServiceDiscovery implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceDiscovery.class);
    
    // 服务注册的根路径
    private static final String REGISTRY_ROOT = "/services";
    
    private final CuratorFramework client;
    private final String connectString;
    private final Map<String, PathChildrenCache> serviceCaches = new ConcurrentHashMap<>();
    private final Map<String, List<ServiceInfo>> serviceInstances = new ConcurrentHashMap<>();
    private final List<ServiceChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * 服务变化监听器接口
     */
    public interface ServiceChangeListener {
        /**
         * 服务列表发生变化时调用
         * @param serviceName 服务名称
         * @param instances 当前可用的服务实例列表
         */
        void onServiceChange(String serviceName, List<ServiceInfo> instances);
    }
    
    /**
     * 构造函数
     * @param connectString ZooKeeper 连接字符串
     */
    public ServiceDiscovery(String connectString) {
        this.connectString = connectString;
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(30000)
                .build();
    }
    
    /**
     * 启动客户端
     */
    public void start() {
        client.start();
        logger.info("服务发现客户端已启动，连接地址: {}", connectString);
    }
    
    /**
     * 监听指定服务的实例变化
     * @param serviceName 服务名称
     */
    public void watchService(String serviceName) throws Exception {
        String servicePath = REGISTRY_ROOT + "/" + serviceName;
        
        // 如果已经监听，先关闭旧的监听器
        if (serviceCaches.containsKey(serviceName)) {
            unwatchService(serviceName);
        }
        
        // 创建 PathChildrenCache 监听子节点变化
        PathChildrenCache cache = new PathChildrenCache(client, servicePath, true);
        cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        
        // 添加监听器
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                handleServiceChange(serviceName, event);
            }
        });
        
        serviceCaches.put(serviceName, cache);
        
        // 初始化服务列表
        updateServiceInstances(serviceName);
        
        logger.info("开始监听服务: {}", serviceName);
    }
    
    /**
     * 取消监听指定服务
     */
    public void unwatchService(String serviceName) throws Exception {
        PathChildrenCache cache = serviceCaches.remove(serviceName);
        if (cache != null) {
            cache.close();
            logger.info("停止监听服务: {}", serviceName);
        }
        serviceInstances.remove(serviceName);
    }
    
    /**
     * 处理服务变化事件
     */
    private void handleServiceChange(String serviceName, PathChildrenCacheEvent event) throws Exception {
        PathChildrenCacheEvent.Type eventType = event.getType();
        logger.debug("服务 {} 发生变化，事件类型: {}", serviceName, eventType);
        
        switch (eventType) {
            case CHILD_ADDED:
                logger.info("服务实例上线: {} -> {}", serviceName, event.getData().getPath());
                break;
            case CHILD_REMOVED:
                logger.info("服务实例下线: {} -> {}", serviceName, event.getData().getPath());
                break;
            case CHILD_UPDATED:
                logger.info("服务实例更新: {} -> {}", serviceName, event.getData().getPath());
                break;
            default:
                break;
        }
        
        // 更新服务实例列表
        updateServiceInstances(serviceName);
    }
    
    /**
     * 更新服务实例列表
     */
    private void updateServiceInstances(String serviceName) throws Exception {
        String servicePath = REGISTRY_ROOT + "/" + serviceName;
        PathChildrenCache cache = serviceCaches.get(serviceName);
        
        if (cache == null) {
            return;
        }
        
        List<ServiceInfo> instances = new ArrayList<>();
        
        // 遍历所有子节点
        for (var childData : cache.getCurrentData()) {
            try {
                String path = childData.getPath();
                String nodeName = path.substring(path.lastIndexOf('/') + 1);
                byte[] data = childData.getData();
                
                if (data != null) {
                    String dataStr = new String(data, StandardCharsets.UTF_8);
                    ServiceInfo serviceInfo = ServiceInfo.fromString(serviceName, dataStr);
                    instances.add(serviceInfo);
                    logger.debug("发现服务实例: {}", serviceInfo);
                }
            } catch (Exception e) {
                logger.warn("解析服务实例数据失败: {}", e.getMessage());
            }
        }
        
        // 更新服务实例列表
        serviceInstances.put(serviceName, instances);
        
        logger.info("服务 {} 当前可用实例数: {}", serviceName, instances.size());
        
        // 通知监听器
        for (ServiceChangeListener listener : listeners) {
            try {
                listener.onServiceChange(serviceName, new ArrayList<>(instances));
            } catch (Exception e) {
                logger.error("通知服务变化监听器失败", e);
            }
        }
    }
    
    /**
     * 获取指定服务的所有实例
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    public List<ServiceInfo> getServiceInstances(String serviceName) {
        return new ArrayList<>(serviceInstances.getOrDefault(serviceName, Collections.emptyList()));
    }
    
    /**
     * 随机获取一个服务实例（负载均衡）
     * @param serviceName 服务名称
     * @return 服务实例，如果没有可用实例返回 null
     */
    public ServiceInfo getRandomInstance(String serviceName) {
        List<ServiceInfo> instances = getServiceInstances(serviceName);
        if (instances.isEmpty()) {
            return null;
        }
        return instances.get(new Random().nextInt(instances.size()));
    }
    
    /**
     * 添加服务变化监听器
     */
    public void addListener(ServiceChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 移除服务变化监听器
     */
    public void removeListener(ServiceChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 获取所有正在监听的服务名称
     */
    public Set<String> getWatchedServices() {
        return new HashSet<>(serviceCaches.keySet());
    }
    
    /**
     * 获取 CuratorFramework 客户端
     */
    public CuratorFramework getClient() {
        return client;
    }
    
    @Override
    public void close() {
        // 关闭所有监听器
        for (String serviceName : new HashSet<>(serviceCaches.keySet())) {
            try {
                unwatchService(serviceName);
            } catch (Exception e) {
                logger.error("关闭服务监听失败: {}", serviceName, e);
            }
        }
        
        if (client != null) {
            client.close();
            logger.info("服务发现客户端已关闭");
        }
    }
}
