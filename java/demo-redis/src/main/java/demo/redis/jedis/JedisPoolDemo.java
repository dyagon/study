package demo.redis.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 使用 JedisPool 操作 Redis 的示例。
 * 运行前请确保本地 Redis 已启动（默认 127.0.0.1:6379）。
 */
public class JedisPoolDemo {

    private static final Logger log = LoggerFactory.getLogger(JedisPoolDemo.class);

    /** 示例 key 的过期时间（秒），便于在 Redis 中观察后自动清理 */
    private static final int EXPIRE_SECONDS = 300;

    public static void main(String[] args) {
        JedisPool pool = RedisPoolHolder.getPool();

        try {
            demoString(pool);
            demoHash(pool);
            demoList(pool);
            demoSet(pool);
            demoKeys(pool);
        } catch (Exception e) {
            log.error("Demo failed", e);
        } finally {
            RedisPoolHolder.closePool();
        }
    }

    /** String：set / get / expire（不删 key，便于观察） */
    static void demoString(JedisPool pool) {
        String key = "demo:str:hello";
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, "world");
            jedis.expire(key, EXPIRE_SECONDS);
            String value = jedis.get(key);
            log.info("String {} = {}", key, value);
        }
    }

    /** Hash：hset / hgetAll / expire（不删 key，便于观察） */
    static void demoHash(JedisPool pool) {
        String key = "demo:hash:user:1";
        try (Jedis jedis = pool.getResource()) {
            jedis.hset(key, Map.of("name", "张三", "age", "20", "city", "北京"));
            jedis.expire(key, EXPIRE_SECONDS);
            Map<String, String> map = jedis.hgetAll(key);
            log.info("Hash {} = {}", key, map);
        }
    }

    /** List：lpush / lrange / lpop / expire（不删 key，便于观察） */
    static void demoList(JedisPool pool) {
        String key = "demo:list:queue";
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key); // 每次演示前清空，便于观察当次写入
            jedis.lpush(key, "c", "b", "a");
            jedis.expire(key, EXPIRE_SECONDS);
            List<String> list = jedis.lrange(key, 0, -1);
            log.info("List {} = {}", key, list);
            String popped = jedis.lpop(key);
            log.info("Lpop {} = {}", key, popped);
        }
    }

    /** Set：sadd / smembers / expire（不删 key，便于观察） */
    static void demoSet(JedisPool pool) {
        String key = "demo:set:tags";
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key); // 每次演示前清空，便于观察当次写入
            jedis.sadd(key, "java", "redis", "jedis");
            jedis.expire(key, EXPIRE_SECONDS);
            Set<String> members = jedis.smembers(key);
            log.info("Set {} = {}", key, members);
        }
    }

    /** 通用：exists、expire、ttl（不删 key，便于观察 TTL 变化） */
    static void demoKeys(JedisPool pool) {
        String key = "demo:key:ttl";
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, "expire-me");
            jedis.expire(key, EXPIRE_SECONDS);
            long ttl = jedis.ttl(key);
            boolean exists = jedis.exists(key);
            log.info("Key {} exists={}, ttl={}s", key, exists, ttl);
        }
    }
}
