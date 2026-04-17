package demo.redis.spring.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户 CRUD 示例：模拟慢 DB + Redis 缓存。
 * - findById：每次走“数据库”（模拟 500ms），无缓存。
 * - findByIdCached：先查 Redis，未命中再查“数据库”并写入缓存，用于演示缓存效果。
 * - save / delete：写“数据库”并删除对应缓存（Cache-Aside）。
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final String CACHE_KEY_PREFIX = "demo:spring:cache:user:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);
    private static final long DB_SLEEP_MS = 500;

    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<Long, CachedUser> db = new ConcurrentHashMap<>();

    public UserService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 预置一条数据用于演示
        db.put(1L, new CachedUser(1L, "张三", "zhangsan@example.com"));
    }

    /** 模拟从 DB 读取（固定延迟），无缓存 */
    public CachedUser findById(Long id) {
        log.info("findById({}) -> 访问 DB（模拟 {}ms）", id, DB_SLEEP_MS);
        sleep(DB_SLEEP_MS);
        return db.get(id);
    }

    /** 先查缓存，未命中再查 DB 并写入缓存，用于演示缓存命中/未命中 */
    @SuppressWarnings("unchecked")
    public CachedUser findByIdCached(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof CachedUser) {
            // log.info("findByIdCached({}) -> 缓存命中", id);
            return (CachedUser) cached;
        }
        log.info("findByIdCached({}) -> 缓存未命中，访问 DB（模拟 {}ms）", id, DB_SLEEP_MS);
        sleep(DB_SLEEP_MS);
        CachedUser user = db.get(id);
        if (user != null) {
            redisTemplate.opsForValue().set(key, user, CACHE_TTL);
            log.info("findByIdCached({}) -> 已写入缓存，TTL {}s", id, CACHE_TTL.getSeconds());
        }
        return user;
    }

    /** 保存并删除缓存（Cache-Aside） */
    public void save(CachedUser user) {
        db.put(user.getId(), user);
        redisTemplate.delete(CACHE_KEY_PREFIX + user.getId());
        log.info("save({}) -> 已更新 DB 并删除缓存", user.getId());
    }

    /** 删除并删除缓存 */
    public void delete(Long id) {
        db.remove(id);
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
        log.info("delete({}) -> 已从 DB 删除并删除缓存", id);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
