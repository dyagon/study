package demo.redis.redisson;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * 1. 分布式锁与同步器 Demo
 * - 可重入锁 RLock
 * - 读写锁 RReadWriteLock
 * - 信号量 RSemaphore
 * - 闭锁 RCountDownLatch
 */
public class DistributedLockDemo {

    public static void run(RedissonClient redisson) {
        System.out.println("========== 1. 分布式锁与同步器 ==========");

        // 可重入锁
        RLock lock = redisson.getLock("demo:lock:resource1");
        try {
            boolean acquired = lock.tryLock(2, 10, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    System.out.println("  [RLock] 获取锁成功，执行业务");
                    lock.lock(); // 同一线程可重入
                    System.out.println("  [RLock] 重入一次");
                    lock.unlock();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("  [RLock] 被中断: " + e.getMessage());
        }

        // 读写锁
        RReadWriteLock rwLock = redisson.getReadWriteLock("demo:rwlock:config");
        rwLock.readLock().lock();
        try {
            System.out.println("  [RReadWriteLock] 读锁已获取");
        } finally {
            rwLock.readLock().unlock();
        }
        rwLock.writeLock().lock();
        try {
            System.out.println("  [RReadWriteLock] 写锁已获取");
        } finally {
            rwLock.writeLock().unlock();
        }

        // 信号量（限制同时访问数量）
        var semaphore = redisson.getSemaphore("demo:semaphore:permits");
        semaphore.trySetPermits(3);
        try {
            if (semaphore.tryAcquire(1, 1, TimeUnit.SECONDS)) {
                try {
                    System.out.println("  [RSemaphore] 获取 1 个许可");
                } finally {
                    semaphore.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 闭锁（等待 N 次 countDown 后放行）
        RCountDownLatch latch = redisson.getCountDownLatch("demo:latch:start");
        latch.trySetCount(1);
        latch.countDown();
        try {
            boolean ok = latch.await(2, TimeUnit.SECONDS);
            System.out.println("  [RCountDownLatch] await 结果: " + ok);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("  分布式锁与同步器 demo 结束\n");
    }
}
