import java.util.ArrayList;
import java.util.List;

public class GCTriggerDemo {
    private static final int MB = 1024 * 1024;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("请指定实验模式: minor, mixed, 或 full");
            return;
        }

        String mode = args[0];
        System.out.println("========== 开始实验: " + mode + " ==========");

        switch (mode) {
            case "minor":
                triggerMinorGC();
                break;
            case "mixed":
                triggerMixedGC();
                break;
            case "full":
                triggerFullGC();
                break;
            default:
                System.out.println("未知的模式");
        }
    }

    /**
     * 实验 1：触发 Minor GC
     * 策略：疯狂创建短期对象（用完即弃），迅速塞满年轻代。
     */
    private static void triggerMinorGC() throws Exception {
        for (int i = 1; i <= 500; i++) {
            // 创建 2MB 的数组，但不将其保存在任何列表中，它会立刻变成垃圾
            byte[] deadObject = new byte[2 * MB]; 
            Thread.sleep(10); // 稍微停顿，方便观察日志时间轴
        }
        System.out.println("Minor GC 实验结束，程序正常退出。");
    }

    /**
     * 实验 2：触发 Mixed GC (现代意义上的 Major GC)
     * 策略：除了创建短期对象，还要持续地把一些对象存入 List 中（模拟缓存）。
     * 这样老年代的使用率会慢慢上升，最终达到阈值触发并发标记和 Mixed GC。
     */
    private static void triggerMixedGC() throws Exception {
        List<byte[]> oldGenCache = new ArrayList<>();
        for (int i = 1; i <= 300; i++) {
            byte[] deadObject = new byte[1 * MB]; // 短期对象触发 Minor GC
            
            // 每循环 3 次，就永久保留一个 1MB 的对象在老年代
            if (i % 3 == 0) {
                oldGenCache.add(new byte[1 * MB]); 
            }
            Thread.sleep(20);
        }
        System.out.println("Mixed GC 实验结束，缓存大小: " + oldGenCache.size() + "MB");
    }

    /**
     * 实验 3：触发 Full GC
     * 策略：极其野蛮地分配超大对象，或者引发严重的内存泄漏，直接击穿 G1 的并发防线。
     */
    private static void triggerFullGC() {
        List<byte[]> memoryLeak = new ArrayList<>();
        try {
            while (true) {
                // 每次直接塞入 10MB 的超大对象，迅速把 100M 的堆撑爆
                memoryLeak.add(new byte[10 * MB]); 
            }
        } catch (OutOfMemoryError e) {
            System.out.println("成功触发 OOM，在此之前必然发生了 Full GC 绝望的挣扎。");
        }
        
        // 另一种最直接触发 Full GC 的方式是显式调用代码（需要关闭 DisableExplicitGC）：
        // System.gc(); 
    }
}
