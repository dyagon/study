package demo.redis.redisson;

import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

/**
 * 2. 分布式集合 Demo
 * - RMap: 分布式 Map
 * - RSet: 分布式 Set
 * - RQueue: 分布式队列
 */
public class DistributedCollectionDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 2. 分布式集合 ==========");

        String keyPrefix = "demo:collection:";

        // RMap
        RMap<String, String> map = redisson.getMap(keyPrefix + "map");
        map.clear();
        map.put("name", "Redisson");
        map.put("version", "3.35");
        System.out.println("  [RMap] " + map.get("name") + " " + map.get("version"));
        map.remove("version");

        // RSet
        RSet<String> set = redisson.getSet(keyPrefix + "set");
        set.clear();
        set.add("a");
        set.add("b");
        set.add("a");
        System.out.println("  [RSet] size=" + set.size() + ", contains(a)=" + set.contains("a"));

        // RQueue
        RQueue<String> queue = redisson.getQueue(keyPrefix + "queue");
        queue.clear();
        queue.offer("first");
        queue.offer("second");
        String head = queue.poll();
        System.out.println("  [RQueue] poll=" + head + ", size=" + queue.size());

        System.out.println("  分布式集合 demo 结束\n");
    }
}
