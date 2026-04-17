package demo.zookeeper.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 服务注册和发现演示类
 * 演示如何使用 ZooKeeper 实现服务注册和服务发现
 */
public class ServiceRegistryDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryDemo.class);
    
    // ZooKeeper 连接地址
    private static final String CONNECT_STRING = "localhost:2181";
    
    // 服务名称
    private static final String SERVICE_NAME = "user-service";
    
    public static void main(String[] args) throws Exception {
        logger.info("========== 服务注册和发现演示开始 ==========");
        
        // 创建服务发现实例
        ServiceDiscovery discovery = new ServiceDiscovery(CONNECT_STRING);
        discovery.start();
        
        // 添加服务变化监听器
        discovery.addListener((serviceName, instances) -> {
            logger.info("【服务变化通知】服务 {} 当前可用实例数: {}", serviceName, instances.size());
            for (ServiceInfo instance : instances) {
                logger.info("  - 实例: {}", instance);
            }
        });
        
        // 开始监听服务
        discovery.watchService(SERVICE_NAME);
        
        // 等待一下，让监听器初始化
        Thread.sleep(1000);
        
        // 创建多个服务实例并注册
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
        CountDownLatch latch = new CountDownLatch(3);
        
        // 启动 3 个服务实例
        for (int i = 1; i <= 3; i++) {
            final int instanceId = i;
            executor.submit(() -> {
                try {
                    ServiceRegistry registry = new ServiceRegistry(CONNECT_STRING);
                    registry.start();
                    
                    // 注册服务（每个实例使用不同的端口）
                    ServiceInfo serviceInfo = new ServiceInfo(
                            SERVICE_NAME,
                            "localhost",
                            8080 + instanceId
                    );
                    registry.register(serviceInfo);
                    
                    logger.info("服务实例 {} 已注册: {}", instanceId, serviceInfo);
                    latch.countDown();
                    
                    // 模拟服务运行一段时间
                    Thread.sleep(10000 + new Random().nextInt(10000));
                    
                    // 取消注册
                    registry.unregister();
                    logger.info("服务实例 {} 已下线", instanceId);
                    
                    registry.close();
                } catch (Exception e) {
                    logger.error("服务实例 {} 运行出错", instanceId, e);
                    latch.countDown();
                }
            });
        }
        
        // 等待所有服务注册完成
        latch.await();
        Thread.sleep(2000);
        
        // 演示服务发现功能
        logger.info("\n--- 演示：服务发现功能 ---");
        demonstrateServiceDiscovery(discovery);
        
        // 模拟服务动态上下线
        logger.info("\n--- 演示：服务动态上下线 ---");
        demonstrateDynamicService(discovery, executor);
        
        // 等待一段时间观察服务变化
        Thread.sleep(30000);
        
        // 清理
        executor.shutdown();
        discovery.close();
        
        logger.info("========== 服务注册和发现演示结束 ==========");
    }
    
    /**
     * 演示服务发现功能
     */
    private static void demonstrateServiceDiscovery(ServiceDiscovery discovery) throws Exception {
        // 获取所有服务实例
        List<ServiceInfo> instances = discovery.getServiceInstances(SERVICE_NAME);
        logger.info("当前可用服务实例数: {}", instances.size());
        
        // 随机选择一个实例（负载均衡）
        for (int i = 0; i < 5; i++) {
            ServiceInfo instance = discovery.getRandomInstance(SERVICE_NAME);
            if (instance != null) {
                logger.info("随机选择的服务实例 {}: {}", i + 1, instance);
            } else {
                logger.warn("没有可用的服务实例");
            }
            Thread.sleep(500);
        }
    }
    
    /**
     * 演示服务动态上下线
     */
    private static void demonstrateDynamicService(ServiceDiscovery discovery, 
                                                   ScheduledExecutorService executor) {
        // 创建一个临时服务实例
        executor.submit(() -> {
            try {
                ServiceRegistry registry = new ServiceRegistry(CONNECT_STRING);
                registry.start();
                
                ServiceInfo tempService = new ServiceInfo(SERVICE_NAME, "localhost", 9999);
                registry.register(tempService);
                logger.info("临时服务实例已上线: {}", tempService);
                
                // 运行 5 秒后下线
                Thread.sleep(5000);
                
                registry.unregister();
                logger.info("临时服务实例已下线: {}", tempService);
                
                registry.close();
            } catch (Exception e) {
                logger.error("临时服务实例运行出错", e);
            }
        });
    }
}
