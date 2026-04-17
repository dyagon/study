package demo.redis.redisson;

import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 4. 延迟队列 Demo
 * 使用 RDelayedQueue：元素在指定延迟后被转移到目标队列，可被消费者 poll 消费。
 */
public class DelayedQueueDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 4. 延迟队列 ==========");

        RQueue<String> targetQueue = redisson.getQueue("demo:delayed:target");
        RDelayedQueue<String> delayedQueue = redisson.getDelayedQueue(targetQueue);
        targetQueue.clear();

        delayedQueue.offer("task-1", 2, TimeUnit.SECONDS);
        delayedQueue.offer("task-2", 4, TimeUnit.SECONDS);
        System.out.println("  [RDelayedQueue] 已投递 2 个延迟任务: 2s、4s 后入队");

        // 立即取不到（延迟未到）
        String immediately = targetQueue.poll();
        System.out.println("  [RDelayedQueue] 立即 poll: " + immediately);

        try {
            Thread.sleep(2_500);
            String first = targetQueue.poll();
            System.out.println("  [RDelayedQueue] 2.5s 后 poll: " + first);
            Thread.sleep(2_000);
            String second = targetQueue.poll();
            System.out.println("  [RDelayedQueue] 再 2s 后 poll: " + second);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("  延迟队列 demo 结束\n");
    }
}
