import java.util.ArrayList;
import java.util.List;

public class RuntimeConstantPoolOOM {
    public static void main(String[] args) {
        // 使用 List 保持着对字符串的引用，防止被 GC 回收
        List<String> list = new ArrayList<>();
        int i = 0;
        try {
            while (true) {
                // String.intern() 会强制将字符串放入常量池
                // String.valueOf(i++).intern() 生成不同的字符串
                list.add(String.valueOf(i++).intern());
            }
        } catch (Throwable e) {
            System.out.println("Count: " + i);
            throw e;
        }
    }
}
