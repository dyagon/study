package demo.rxjava.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import java.util.function.Supplier;

public class RetryExample {
    
    public static void main(String[] args) {
        RemoteService service = new RemoteService();

        // 1. 配置重试
        RetryConfig config = RetryConfig.custom()
                // 最大重试次数（总共执行3次）
                .maxAttempts(3)
                // 每次重试之间的等待时间
                .waitDuration(Duration.ofMillis(200))
                .build();
        
        Retry retry = Retry.of("my-retry", config);
        
        // 监听重试事件，用于日志记录
        retry.getEventPublisher().onRetry(event -> 
            System.out.println("--> Retrying... Attempt #" + event.getNumberOfRetryAttempts())
        );

        Supplier<String> decoratedSupplier = Retry.decorateSupplier(retry, service::riskyOperation);

        // 2. 执行并观察重试
        try {
            String result = decoratedSupplier.get();
            System.out.println("Final Result: " + result);
        } catch (Exception e) {
            System.err.println("Final Result: Operation failed after all retries.");
        }
    }
}
