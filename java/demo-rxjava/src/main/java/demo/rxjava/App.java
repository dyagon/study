package demo.rxjava;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.schedulers.TestScheduler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import io.reactivex.rxjava3.core.BackpressureStrategy;

public class App {

    static class Dish {
        private final byte[] oneKb = new byte[1024];
        private final int id;

        Dish(int id) {
            this.id = id;
            System.out.println("Dish " + id + " created");
        }

        public String toString() {
            return "Dish " + id;
        }
    }

    static void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    static void testNoBackpressure() {
        Observable.range(1, 1000000)
            .map(i -> new Dish(i))
            .observeOn(Schedulers.io())  // OOM error here
            .subscribe(d -> {
                System.out.println("Dish " + d.id + " consumed");
                sleepMillis(50);
            });

        sleepMillis(10000);
    }


    static void testNoBackpressure2() {
        Observable.range(1, 1000000)
            .toFlowable(BackpressureStrategy.ERROR)
            .map(i -> new Dish(i))
            .observeOn(Schedulers.io())  // OOM error here
            .subscribe(d -> {
                System.out.println("Dish " + d.id + " consumed");
                sleepMillis(50);
            });

        sleepMillis(10000);
    }


    static void testBackpressure() {
        Flowable.range(1, 1000000)
            .map(i -> new Dish(i))
            .doOnRequest(count -> System.out.println("Requested " + count + " items"))
            .observeOn(Schedulers.io())
            .doOnRequest(count -> System.out.println("Requested " + count + " items"))
            .subscribe(d -> {
                System.out.println("Dish " + d.id + " consumed");
                sleepMillis(50);
            });
        sleepMillis(10000);
    }

    static void testRetry() {
        AtomicInteger attemptCounter = new AtomicInteger(0);

        Observable<String> unstableSource = Observable.create(emitter -> {
            int attempt = attemptCounter.incrementAndGet();
            System.out.println("开始第 " + attempt + " 次尝试...");

            if (attempt <= 2) { // 模拟前两次尝试都失败
                System.out.println("第 " + attempt + " 次尝试失败！");
                emitter.onError(new RuntimeException("临时性错误"));
            } else { // 第三次尝试成功
                System.out.println("第 " + attempt + " 次尝试成功！");
                emitter.onNext("成功获取数据");
                emitter.onComplete();
            }
        });

        unstableSource
            .retry(2) // 第一次失败后，额外重试 2 次
            .subscribe(
                data -> System.out.println("onNext: " + data),
                error -> System.out.println("onError: " + error.getMessage()), // 如果最终还是失败，会调用这里
                () -> System.out.println("onComplete")
            );

        sleepMillis(10000);
    }

    static void testScheduler() {
        TestScheduler scheduler = new TestScheduler();

        Observable<String> fast = Observable
            .interval(10, TimeUnit.MILLISECONDS, scheduler)
            .map(x -> "F" + x)
            .take(3);
        
        Observable<String> slow = Observable
            .interval(50, TimeUnit.MILLISECONDS, scheduler)
            .map(x -> "S" + x);

        Observable.concat(fast, slow)
            .subscribe(System.out::println);

        System.out.println("Subscribed");

        sleepMillis(1000);
        System.out.println("After one second");
        scheduler.advanceTimeBy(25, TimeUnit.MILLISECONDS);

        sleepMillis(1000);
        System.out.println("After two seconds");
        scheduler.advanceTimeBy(75, TimeUnit.MILLISECONDS);
        System.out.println("Advanced to 2 seconds");

        sleepMillis(1000);
        System.out.println("After three seconds");
        scheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS);
    }


    public static void main(String[] args) {
        
        // testNoBackpressure();
        // testNoBackpressure2();
        testBackpressure();

        // testRetry();
        // testScheduler();
    }
}
