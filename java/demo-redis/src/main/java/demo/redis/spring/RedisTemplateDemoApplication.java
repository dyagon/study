package demo.redis.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import demo.redis.spring.cache.ProductService;
import demo.redis.spring.cache.UserService;

/**
 * 使用 Spring Data Redis 的 RedisTemplate 操作 Redis 的示例。
 * 运行前请确保本地 Redis 已启动（默认 127.0.0.1:6379，可在 application.properties 修改）。
 */
@SpringBootApplication
public class RedisTemplateDemoApplication {

    private static final Logger log = LoggerFactory.getLogger(RedisTemplateDemoApplication.class);

    /** 示例 key 的过期时间（秒），便于在 Redis 中观察后自动清理 */
    private static final long EXPIRE_SECONDS = 300;

    public static void main(String[] args) {
        SpringApplication.run(RedisTemplateDemoApplication.class, args);
    }

    private static final String DEMO_SEP = "==========";

    @Bean
    public CommandLineRunner run(StringRedisTemplate redisTemplate, UserService userService,
                                 ProductService productService, ApplicationContext ctx) {
        return args -> {
            demoString(redisTemplate);
            demoHash(redisTemplate);
            demoList(redisTemplate);
            demoSet(redisTemplate);
            demoKeys(redisTemplate);
            demoCacheCrud(userService);
            demoSpringCacheAnnotations(productService);
            SpringApplication.exit(ctx, () -> 0);
        };
    }

    /** String：opsForValue().set / get / expire */
    static void demoString(StringRedisTemplate redis) {
        log.info("{} [Demo] String：opsForValue set/get/expire {}", DEMO_SEP, DEMO_SEP);
        String key = "demo:spring:str:hello";
        redis.opsForValue().set(key, "world");
        redis.expire(key, java.time.Duration.ofSeconds(EXPIRE_SECONDS));
        String value = redis.opsForValue().get(key);
        log.info("  String {} = {}", key, value);
    }

    /** Hash：opsForHash().put / entries / expire */
    static void demoHash(StringRedisTemplate redis) {
        log.info("{} [Demo] Hash：opsForHash put/entries/expire {}", DEMO_SEP, DEMO_SEP);
        String key = "demo:spring:hash:user:1";
        var hash = redis.opsForHash();
        hash.put(key, "name", "张三");
        hash.put(key, "age", "20");
        hash.put(key, "city", "北京");
        redis.expire(key, java.time.Duration.ofSeconds(EXPIRE_SECONDS));
        var map = hash.entries(key);
        log.info("  Hash {} = {}", key, map);
    }

    /** List：opsForList().leftPush / range / leftPop / expire */
    static void demoList(StringRedisTemplate redis) {
        log.info("{} [Demo] List：opsForList leftPush/range/leftPop/expire {}", DEMO_SEP, DEMO_SEP);
        String key = "demo:spring:list:queue";
        redis.delete(key);
        redis.opsForList().leftPushAll(key, "c", "b", "a");
        redis.expire(key, java.time.Duration.ofSeconds(EXPIRE_SECONDS));
        var list = redis.opsForList().range(key, 0, -1);
        log.info("  List {} = {}", key, list);
        String popped = redis.opsForList().leftPop(key);
        log.info("  Lpop {} = {}", key, popped);
    }

    /** Set：opsForSet().add / members / expire */
    static void demoSet(StringRedisTemplate redis) {
        log.info("{} [Demo] Set：opsForSet add/members/expire {}", DEMO_SEP, DEMO_SEP);
        String key = "demo:spring:set:tags";
        redis.delete(key);
        redis.opsForSet().add(key, "java", "redis", "spring-data-redis");
        redis.expire(key, java.time.Duration.ofSeconds(EXPIRE_SECONDS));
        var members = redis.opsForSet().members(key);
        log.info("  Set {} = {}", key, members);
    }

    /** 通用：hasKey、expire、getExpire */
    static void demoKeys(StringRedisTemplate redis) {
        log.info("{} [Demo] Keys：hasKey/expire/getExpire {}", DEMO_SEP, DEMO_SEP);
        String key = "demo:spring:key:ttl";
        redis.opsForValue().set(key, "expire-me");
        redis.expire(key, java.time.Duration.ofSeconds(EXPIRE_SECONDS));
        Boolean exists = redis.hasKey(key);
        Long ttl = redis.getExpire(key);
        log.info("  Key {} exists={}, ttl={}s", key, exists, ttl);
    }

    private static final int CACHE_HIT_ITERATIONS = 1000;
    private static final int CACHE_HIT_WARMUP = 200;

    /** 演示缓存效果：无缓存 vs 有缓存，命中时 1000 次取平均消除冷启动干扰 */
    static void demoCacheCrud(UserService userService) {
        log.info("{} [Demo] Cache CRUD：无缓存 vs 有缓存，命中访问 {} 次取平均 {}", DEMO_SEP, CACHE_HIT_ITERATIONS, DEMO_SEP);

        // 1）无缓存：两次都走 DB，都会慢
        long t0 = System.currentTimeMillis();
        var u0 = userService.findById(1L);
        long t1 = System.currentTimeMillis();
        log.info("  无缓存 findById(1) = {}，访问耗时 {}ms", u0, t1 - t0);

        long t2 = System.currentTimeMillis();
        var u1 = userService.findById(1L);
        long t3 = System.currentTimeMillis();
        log.info("  无缓存 findById(1) again = {}，访问耗时 {}ms", u1, t3 - t2);

        // 2）有缓存：第一次未命中走 DB，第二次单次命中
        long t4 = System.currentTimeMillis();
        var u2 = userService.findByIdCached(1L);
        long t5 = System.currentTimeMillis();
        log.info("  有缓存 findByIdCached(1) = {}，访问耗时 {}ms（未命中）", u2, t5 - t4);

        long t6 = System.currentTimeMillis();
        var u3 = userService.findByIdCached(1L);
        long t7 = System.currentTimeMillis();
        log.info("  有缓存 findByIdCached(1) again = {}，访问耗时 {}ms（命中，单次）", u3, t7 - t6);

        // 3）预热后对命中访问测 1000 次，取平均（消除冷启动/JIT 等干扰）
        for (int i = 0; i < CACHE_HIT_WARMUP; i++) {
            userService.findByIdCached(1L);
        }
        long totalNanos = 0;
        for (int i = 0; i < CACHE_HIT_ITERATIONS; i++) {
            long start = System.nanoTime();
            userService.findByIdCached(1L);
            totalNanos += System.nanoTime() - start;
        }
        double avgMicros = totalNanos / (double) CACHE_HIT_ITERATIONS / 1000.0;
        double avgMillis = totalNanos / (double) CACHE_HIT_ITERATIONS / 1_000_000.0;
        log.info("  命中访问 {} 次（预热 {} 次）平均耗时：{} μs，{} ms",
                CACHE_HIT_ITERATIONS, CACHE_HIT_WARMUP,
                String.format("%.2f", avgMicros), String.format("%.3f", avgMillis));

        // 4）更新后缓存失效，下次再查会重新走 DB
        userService.save(new demo.redis.spring.cache.CachedUser(1L, "张三-updated", "zhangsan@example.com"));

        long t8 = System.currentTimeMillis();
        var u4 = userService.findByIdCached(1L);
        long t9 = System.currentTimeMillis();
        log.info("  更新后 findByIdCached(1) = {}，访问耗时 {}ms（缓存已失效，再走 DB）", u4, t9 - t8);
    }

    /** 演示 Spring 缓存注解：@Cacheable、@CachePut、@CacheEvict（CacheManager 存 Redis） */
    static void demoSpringCacheAnnotations(ProductService productService) {
        log.info("{} [Demo] Spring 缓存注解：@Cacheable / @CachePut / @CacheEvict {}", DEMO_SEP, DEMO_SEP);

        long t0 = System.currentTimeMillis();
        var p1 = productService.getById(1L);
        long t1 = System.currentTimeMillis();
        log.info("  getById(1) = {}，访问耗时 {}ms（未命中）", p1, t1 - t0);

        long t2 = System.currentTimeMillis();
        var p2 = productService.getById(1L);
        long t3 = System.currentTimeMillis();
        log.info("  getById(1) again = {}，访问耗时 {}ms（命中）", p2, t3 - t2);

        productService.save(new demo.redis.spring.cache.Product(1L, "商品A-已更新", "SKU-001"));
        long t4 = System.currentTimeMillis();
        var p3 = productService.getById(1L);
        long t5 = System.currentTimeMillis();
        log.info("  save 后 getById(1) = {}，访问耗时 {}ms（CachePut 已刷新缓存）", p3, t5 - t4);

        productService.evictAll();
        long t6 = System.currentTimeMillis();
        var p4 = productService.getById(2L);
        long t7 = System.currentTimeMillis();
        log.info("  evictAll 后 getById(2) = {}，访问耗时 {}ms（缓存已清，再走 DB）", p4, t7 - t6);
    }
}
