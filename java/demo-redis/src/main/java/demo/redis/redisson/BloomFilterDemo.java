package demo.redis.redisson;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;

/**
 * 6. 布隆过滤器 Demo
 * 用于判断元素是否“可能存在”于集合中，有极低误判率、无漏判；适合防缓存穿透等。
 */
public class BloomFilterDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 6. 布隆过滤器 ==========");

        RBloomFilter<String> filter = redisson.getBloomFilter("demo:bloom:users");
        // 预期元素数量 10000，误判率 0.01
        if (!filter.isExists()) {
            filter.tryInit(10_000L, 0.01);
        }

        filter.add("user:1001");
        filter.add("user:1002");
        System.out.println("  [RBloomFilter] contains user:1001 = " + filter.contains("user:1001"));
        System.out.println("  [RBloomFilter] contains user:9999 = " + filter.contains("user:9999"));

        System.out.println("  布隆过滤器 demo 结束\n");
    }
}
