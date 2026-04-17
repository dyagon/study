

```bash

# 使用 curator 的 client
./gradlew :demo-zookeeper:run


# 服务注册和服务发现
./gradlew :demo-zookeeper:runServiceRegistryDemo

# 集群节点命名
./gradlew :demo-zookeeper:runClusterNodeNamingDemo


# 可重入公平锁（使用 Curator）
./gradlew :demo-zookeeper:runReentrantLockDemo

# 版本1
./gradlew :demo-zookeeper:runZkLockDemo 
./gradlew :demo-zookeeper:runZkLockV2Demo 

# 锁资源处理
./gradlew :demo-zookeeper:runConcurrentResourceAccessDemo  
```

