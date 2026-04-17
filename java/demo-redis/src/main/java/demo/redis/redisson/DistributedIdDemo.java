package demo.redis.redisson;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;

/**
 * 5. 分布式 ID 生成器 Demo
 * 使用 RAtomicLong 实现全局自增 ID；可扩展为带前缀、步长等业务 ID。
 */
public class DistributedIdDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 5. 分布式 ID 生成器 ==========");

        RAtomicLong counter = redisson.getAtomicLong("demo:id:order");
        long id1 = counter.incrementAndGet();
        long id2 = counter.incrementAndGet();
        long id3 = counter.addAndGet(10);
        System.out.println("  [RAtomicLong] id1=" + id1 + ", id2=" + id2 + ", addAndGet(10)=" + id3);

        // 业务 ID 示例：前缀 + 自增
        String orderId = "ORD" + String.format("%012d", counter.incrementAndGet());
        System.out.println("  [分布式ID] 订单号示例: " + orderId);

        System.out.println("  分布式 ID 生成器 demo 结束\n");
    }
}
