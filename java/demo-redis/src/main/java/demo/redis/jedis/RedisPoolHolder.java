package demo.redis.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * 基于 JedisPool 的 Redis 连接池持有者。
 * 单例，应用内共享一个连接池。
 */
public final class RedisPoolHolder {

    private static final Logger log = LoggerFactory.getLogger(RedisPoolHolder.class);

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 6379;
    private static final int DEFAULT_DATABASE = 0;
    private static final String DEFAULT_PASSWORD = null;

    private static volatile JedisPool pool;

    private RedisPoolHolder() {}

    /**
     * 使用默认配置（localhost:6379）获取连接池。
     */
    public static JedisPool getPool() {
        return getPool(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PASSWORD, DEFAULT_DATABASE);
    }

    /**
     * 指定 host、port 获取连接池，无密码。
     */
    public static JedisPool getPool(String host, int port) {
        return getPool(host, port, null, DEFAULT_DATABASE);
    }

    /**
     * 指定 host、port、password、database 获取连接池。
     */
    public static JedisPool getPool(String host, int port, String password, int database) {
        if (pool == null) {
            synchronized (RedisPoolHolder.class) {
                if (pool == null) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(16);
                    config.setMaxIdle(8);
                    config.setMinIdle(2);
                    config.setTestOnBorrow(true);
                    config.setTestWhileIdle(true);
                    config.setBlockWhenExhausted(true);
                    config.setMaxWait(Duration.ofSeconds(3));

                    // timeout 2000ms；无密码时 password 传 null
                    pool = new JedisPool(config, host, port, 2000, password, database);
                    log.info("JedisPool created: {}:{}, database={}", host, port, database);
                }
            }
        }
        return pool;
    }

    /**
     * 关闭连接池（应用退出时调用）。
     */
    public static void closePool() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
            pool = null;
            log.info("JedisPool closed.");
        }
    }
}
