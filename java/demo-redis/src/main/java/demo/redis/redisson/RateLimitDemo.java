package demo.redis.redisson;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 3. 分布式限流 Demo
 * 使用 RRateLimiter，按速率/令牌桶在集群内限流。
 */
public class RateLimitDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 3. 分布式限流 ==========");

        RRateLimiter limiter = redisson.getRateLimiter("demo:ratelimit:api");
        // 每 1 秒最多 2 个请求（OVERALL 表示全局限流）
        limiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

        int acquired = 0;
        for (int i = 0; i < 4; i++) {
            if (limiter.tryAcquire(1, 100, TimeUnit.MILLISECONDS)) {
                acquired++;
                System.out.println("  [RRateLimiter] 第 " + (i + 1) + " 次请求通过");
            } else {
                System.out.println("  [RRateLimiter] 第 " + (i + 1) + " 次请求被限流");
            }
        }
        System.out.println("  通过数: " + acquired);

        System.out.println("  分布式限流 demo 结束\n");
    }
}
