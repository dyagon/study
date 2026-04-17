package demo.redis.redisson;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * Redisson 客户端配置。
 * 从系统属性或环境变量读取连接信息，默认 127.0.0.1:6379 无密码。
 */
public final class RedissonConfig {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 6379;

    private RedissonConfig() {}

    /**
     * 创建单机模式 RedissonClient。
     * 可通过 -Dredis.host=... -Dredis.port=... -Dredis.password=... 覆盖。
     */
    public static RedissonClient createClient() {
        String host = System.getProperty("redis.host", System.getenv().getOrDefault("REDIS_HOST", DEFAULT_HOST));
        int port = Integer.parseInt(
                System.getProperty("redis.port", System.getenv().getOrDefault("REDIS_PORT", String.valueOf(DEFAULT_PORT))));
        String password = System.getProperty("redis.password", System.getenv().getOrDefault("REDIS_PASSWORD", ""));

        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        if (password == null || password.isEmpty()) {
            config.useSingleServer().setAddress(address);
        } else {
            config.useSingleServer().setAddress(address).setPassword(password);
        }
        return Redisson.create(config);
    }
}
