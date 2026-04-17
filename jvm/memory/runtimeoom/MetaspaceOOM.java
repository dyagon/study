import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class MetaspaceOOM {
    public static void main(String[] args) {
        // 保持引用防止类加载器被回收
        List<ClassLoader> classLoaders = new ArrayList<>();
        try {
            while (true) {
                // 创建一个新的类加载器
                URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(".").toURI().toURL()}, null);
                // 加载同一个类（注意：不同的 ClassLoader 加载同一个类，JVM 认为是不同的类）
                // 这里的 "MetaspaceOOM" 可以替换成任何存在的类名
                Class<?> clazz = classLoader.loadClass("MetaspaceOOM");
                classLoaders.add(classLoader);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
