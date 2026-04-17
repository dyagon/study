package demo.redis.spring.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 Spring 缓存注解的示例：@Cacheable、@CachePut、@CacheEvict。
 * 缓存名：products，由 RedisConfig 中的 CacheManager 写入 Redis。
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private static final long DB_SLEEP_MS = 100;

    private final Map<Long, Product> db = new ConcurrentHashMap<>();

    public ProductService() {
        db.put(1L, new Product(1L, "商品A", "SKU-001"));
        db.put(2L, new Product(2L, "商品B", "SKU-002"));
    }

    /** 命中则从缓存返回，未命中则查“DB”并写入缓存 */
    @Cacheable(cacheNames = "products", key = "#id")
    public Product getById(Long id) {
        log.info("getById({}) -> 未命中缓存，访问 DB（模拟 {}ms）", id, DB_SLEEP_MS);
        sleep(DB_SLEEP_MS);
        return db.get(id);
    }

    /** 更新 DB 并更新缓存（CachePut：总是执行方法并写回缓存） */
    @CachePut(cacheNames = "products", key = "#product.id")
    public Product save(Product product) {
        log.info("save({}) -> 更新 DB 并刷新缓存", product.getId());
        db.put(product.getId(), product);
        return product;
    }

    /** 删除 DB 并删除缓存 */
    @CacheEvict(cacheNames = "products", key = "#id")
    public void delete(Long id) {
        log.info("delete({}) -> 删除 DB 并清除缓存", id);
        db.remove(id);
    }

    /** 清空 products 缓存（不删 DB） */
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void evictAll() {
        log.info("evictAll() -> 清空 products 缓存");
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
