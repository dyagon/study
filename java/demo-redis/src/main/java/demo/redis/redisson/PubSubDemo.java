package demo.redis.redisson;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 7. 消息发布/订阅 Demo
 * 使用 RTopic 发布与订阅消息。
 */
public class PubSubDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 7. 消息发布/订阅 ==========");

        String channel = "demo:pubsub:news";
        RTopic topic = redisson.getTopic(channel);

        CountDownLatch received = new CountDownLatch(1);
        int listenerId = topic.addListener(String.class, (MessageListener<String>) (channelName, msg) -> {
            System.out.println("  [RTopic] 收到消息: " + msg);
            received.countDown();
        });

        topic.publish("Hello from Redisson Pub/Sub!");
        try {
            boolean ok = received.await(3, TimeUnit.SECONDS);
            System.out.println("  [RTopic] 订阅端是否收到: " + ok);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        topic.removeListener(listenerId);
        System.out.println("  消息发布/订阅 demo 结束\n");
    }
}
