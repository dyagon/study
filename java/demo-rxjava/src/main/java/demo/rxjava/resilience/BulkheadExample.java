package demo.rxjava.resilience;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class BulkheadExample {
    
    public static void main(String[] args) {
        RemoteService service = new RemoteService();

        // 1. 配置舱壁
        BulkheadConfig config = BulkheadConfig.custom()
                // 最大并发调用数
                .maxConcurrentCalls(2)
                // 当舱壁已满时，请求愿意等待的最长时间
                .maxWaitDuration(Duration.ofMillis(500))
                .build();
        
        Bulkhead bulkhead = Bulkhead.of("my-bulkhead", config);

        Supplier<String> decoratedSupplier = Bulkhead.decorateSupplier(bulkhead, () -> {
            try {
                return service.slowOperation();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 2. 模拟5个并发请求
        System.out.println("Simulating 5 concurrent requests...");
        for (int i = 0; i < 5; i++) {
            int callNumber = i + 1;
            CompletableFuture.supplyAsync(decoratedSupplier)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        // 当舱壁已满且等待超时后，会抛出 BulkheadFullException
                        System.err.println("Call " + callNumber + " rejected: " + throwable.getMessage());
                    } else {
                        System.out.println("Call " + callNumber + " succeeded: " + result);
                    }
                });
        }
        
        // 等待所有异步任务完成
        try {
            Thread.sleep(5000); 
        } catch (InterruptedException ignored) {}
    }
}
