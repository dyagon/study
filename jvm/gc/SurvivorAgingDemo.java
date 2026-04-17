import java.util.ArrayList;
import java.util.List;

public class SurvivorAgingDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("========== 开始观察对象晋升与年龄增长 ==========");
        
        // 1. 创建我们要重点观察的“长寿对象”集合
        // 为了避免触发大对象机制，我们分配 100 个 10KB 的小数组，总计约 1MB。
        // 这 1MB 对象一开始会被分配在 Eden 区，并且由于被列表持有引用，它们在后续 GC 中不会死。
        List<byte[]> survivors = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            survivors.add(new byte[10 * 1024]); // 10 KB
        }
        System.out.println(">> 成功创建了 1MB 的存活对象，它们目前在 Eden 区（年龄 0）。");

        // 2. 开始制造垃圾，人为触发多次 Young GC
        for (int i = 1; i <= 10; i++) {
            System.out.println("\n>> 开始第 " + i + " 轮分配短命对象，准备触发 GC...");
            
            // 循环分配用完即弃的 1MB 数组，迅速塞满 Eden 区触发 GC
            for (int j = 0; j < 30; j++) {
                byte[] deadObject = new byte[1024 * 1024]; 
            }
            
            // 稍作停顿，方便我们在控制台和日志中对齐时间
            Thread.sleep(500); 
        }
        
        System.out.println(">> 实验结束，保持存活对象引用: " + survivors.size());
    }
}
