package demo.redis.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * 仅用于 CacheTimingTest 的轻量入口，不执行主应用的 CommandLineRunner，避免上下文被 exit 关闭。
 */
@SpringBootApplication
@ComponentScan(
    basePackages = "demo.redis.spring",
    excludeFilters = @ComponentScan.Filter(classes = RedisTemplateDemoApplication.class, type = FilterType.ASSIGNABLE_TYPE)
)
public class RedisTimingTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisTimingTestApplication.class, args);
    }
}
