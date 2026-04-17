package demo.redis.spring.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 精确测量缓存命中路径上的各项耗时：Redis 访问、序列化/反序列化、其他。
 * 需要本地 Redis（127.0.0.1:6379）运行。
 * <p>
 * 运行：{@code ./gradlew :demo-redis:test --tests "demo.redis.spring.cache.CacheTimingTest.breakdownCacheHitTiming"}
 * <p>
 * 若主应用里缓存命中约 13ms，而本测试显示「完整缓存命中」仅约 0.08ms，差异可能来自：
 * 冷启动/JIT、日志 I/O、首次从连接池取连接、或 Redis 在 Docker/远程时的网络延迟。
 */
@SpringBootTest(classes = demo.redis.spring.RedisTimingTestApplication.class)
class CacheTimingTest {

    private static final String STRING_KEY = "demo:timing:string";
    private static final String USER_KEY = "demo:timing:user:1";
    private static final int WARMUP = 500;
    private static final int ITERATIONS = 10_000;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<String, Object> objectRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CachedUser sampleUser;
    private String sampleUserJson;

    @BeforeEach
    void setUp() throws Exception {
        sampleUser = new CachedUser(1L, "张三", "zhangsan@example.com");
        sampleUserJson = objectMapper.writeValueAsString(sampleUser);
        stringRedisTemplate.opsForValue().set(STRING_KEY, "hello");
        objectRedisTemplate.opsForValue().set(USER_KEY, sampleUser);
    }

    /** 平均耗时（纳秒转微秒），多次迭代取中位数更稳，这里用平均 */
    private static double avgNanosToMicros(long[] nanos) {
        long sum = 0;
        for (long n : nanos) sum += n;
        return sum / (double) nanos.length / 1000.0;
    }

    @Test
    void breakdownCacheHitTiming() throws Exception {
        // 0) 纯 Redis 往返：PING（无序列化）
        try (RedisConnection conn = objectRedisTemplate.getConnectionFactory().getConnection()) {
            runWarmup(() -> conn.ping(), WARMUP);
            long[] redisPing = new long[ITERATIONS];
            for (int i = 0; i < ITERATIONS; i++) {
                long t0 = System.nanoTime();
                conn.ping();
                redisPing[i] = System.nanoTime() - t0;
            }
            double redisPingUs = avgNanosToMicros(redisPing);
            System.out.println("========== 缓存命中耗时分解（微秒，迭代 " + ITERATIONS + " 次平均） ==========");
            System.out.printf("  Redis 纯往返 (PING):           %.2f μs%n", redisPingUs);
        }

        // 1) Redis + String 反序列化（反序列化几乎可忽略）→ 近似 “Redis GET 小 value”
        runWarmup(() -> stringRedisTemplate.opsForValue().get(STRING_KEY), WARMUP);
        long[] redisStringGet = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long t0 = System.nanoTime();
            stringRedisTemplate.opsForValue().get(STRING_KEY);
            redisStringGet[i] = System.nanoTime() - t0;
        }
        double redisStringGetUs = avgNanosToMicros(redisStringGet);

        // 2) 纯 JSON 反序列化（内存，无 Redis）
        runWarmup(() -> {
            try {
                objectMapper.readValue(sampleUserJson, CachedUser.class);
            } catch (Exception e) { throw new RuntimeException(e); }
        }, WARMUP);
        long[] jsonDeserialize = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long t0 = System.nanoTime();
            objectMapper.readValue(sampleUserJson, CachedUser.class);
            jsonDeserialize[i] = System.nanoTime() - t0;
        }
        double jsonDeserializeUs = avgNanosToMicros(jsonDeserialize);

        // 3) 纯 JSON 序列化（内存，无 Redis）
        runWarmup(() -> {
            try {
                objectMapper.writeValueAsString(sampleUser);
            } catch (Exception e) { throw new RuntimeException(e); }
        }, WARMUP);
        long[] jsonSerialize = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long t0 = System.nanoTime();
            objectMapper.writeValueAsString(sampleUser);
            jsonSerialize[i] = System.nanoTime() - t0;
        }
        double jsonSerializeUs = avgNanosToMicros(jsonSerialize);

        // 4) 完整缓存命中：Redis GET + 反序列化（GenericJackson2JsonRedisSerializer）
        runWarmup(() -> objectRedisTemplate.opsForValue().get(USER_KEY), WARMUP);
        long[] cacheHit = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long t0 = System.nanoTime();
            objectRedisTemplate.opsForValue().get(USER_KEY);
            cacheHit[i] = System.nanoTime() - t0;
        }
        double cacheHitUs = avgNanosToMicros(cacheHit);

        // 5) Redis 层理论耗时 ≈ Redis(String GET)；
        //    反序列化已在 2 中测得；缓存命中 ≈ Redis + 反序列化，余量为“其他”（连接池、序列化器封装等）
        double redisLikeUs = redisStringGetUs;
        double otherUs = cacheHitUs - redisLikeUs - jsonDeserializeUs;

        System.out.printf("  Redis GET 小字符串:             %.2f μs%n", redisStringGetUs);
        System.out.printf("  JSON 反序列化（内存）:        %.2f μs%n", jsonDeserializeUs);
        System.out.printf("  JSON 序列化（内存）:         %.2f μs%n", jsonSerializeUs);
        System.out.printf("  完整缓存命中 (GET+反序列化):  %.2f μs (%.2f ms)%n", cacheHitUs, cacheHitUs / 1000);
        System.out.printf("  其他（连接池/序列化器等）:   %.2f μs%n", Math.max(0, otherUs));
        System.out.println("================================================================");
    }

    private static void runWarmup(Runnable r, int times) {
        for (int i = 0; i < times; i++) r.run();
    }
}
