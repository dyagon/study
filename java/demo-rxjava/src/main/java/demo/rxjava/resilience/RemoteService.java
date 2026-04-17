package demo.rxjava.resilience;

// 一个模拟的远程服务，我们用它来触发容错机制
class RemoteService {
    
    // 模拟一个可能会失败的操作
    public String riskyOperation() {
        // 模拟50%的失败率
        if (Math.random() < 0.5) {
            throw new RuntimeException("Operation failed due to a transient error!");
        }
        return "Operation was successful!";
    }

    // 模拟一个高延迟的操作
    public String slowOperation() throws InterruptedException {
        // 模拟2秒的延迟
        Thread.sleep(2000);
        return "Slow operation completed!";
    }
}
