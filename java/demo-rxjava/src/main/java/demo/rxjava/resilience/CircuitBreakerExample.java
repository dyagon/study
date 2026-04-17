package demo.rxjava.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import java.util.function.Supplier;

public class CircuitBreakerExample {
    public static void main(String[] args) {
        RemoteService service = new RemoteService();

        // 1. 配置熔断器
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // 失败率阈值，超过50%则打开熔断器
                .failureRateThreshold(50)
                // 在打开状态下等待的时间
                .waitDurationInOpenState(Duration.ofSeconds(10))
                // 半开状态下允许的测试请求数
                .permittedNumberOfCallsInHalfOpenState(2)
                // 计算失败率的滑动窗口大小（最近10次调用）
                .slidingWindowSize(10)
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("my-circuit-breaker", config);

        // 2. 将服务调用包装在熔断器中
        Supplier<String> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, service::riskyOperation);

        // 3. 模拟多次调用
        for (int i = 0; i < 100; i++) {
            try {
                System.out.println("Attempt " + (i + 1) + ": " + decoratedSupplier.get());
            } catch (Exception e) {
                // 当熔断器打开时，会抛出 CallNotPermittedException
                System.err.println("Attempt " + (i + 1) + ": Call failed! Reason: " + e.getMessage());
            }
        }

    }
}
