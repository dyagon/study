package demo.redis.redisson;

import org.redisson.api.RedissonClient;

/**
 * Redisson 功能演示入口。
 * 依次运行：分布式锁与同步器、分布式集合、分布式限流、延迟队列、
 * 分布式 ID 生成器、布隆过滤器、消息发布/订阅。
 * 需确保 Redis 已启动（默认 127.0.0.1:6379）。
 */
public class RedissonDemoApplication {

    public static void main(String[] args) {
        System.out.println("Redisson Demo 启动（Redis 默认 127.0.0.1:6379）\n");
        RedissonClient redisson = RedissonConfig.createClient();
        try {
            DistributedLockDemo.run(redisson);
            DistributedCollectionDemo.run(redisson);
            RateLimitDemo.run(redisson);
            DelayedQueueDemo.run(redisson);
            DistributedIdDemo.run(redisson);
            BloomFilterDemo.run(redisson);
            PubSubDemo.run(redisson);
            System.out.println("全部 Redisson Demo 执行完毕。");
        } finally {
            redisson.shutdown();
        }
    }
}
