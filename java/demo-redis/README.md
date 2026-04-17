


```bash

# jedis demo
./gradlew :demo-redis:runJedisPoolDemo      


./gradlew :demo-redis:runRedisTemplateDemo 

# 测试 redis 访问时间
./gradlew :demo-redis:test --tests "demo.redis.spring.cache.CacheTimingTest.breakdownCacheHitTiming"
