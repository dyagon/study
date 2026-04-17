import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 自定义类加载器：从指定目录读取 .class 文件并 defineClass。
 * 对指定类名不委托给父加载器，由本加载器直接加载（便于演示）。
 */
public class MyClassLoader extends ClassLoader {

    private final String basePath;

    public MyClassLoader(ClassLoader parent, String basePath) {
        super(parent);
        this.basePath = (basePath == null || basePath.isEmpty()) ? "" : (basePath.endsWith("/") ? basePath : basePath + "/");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 对 ToLoad 不委托，由本加载器加载
        if ("ToLoad".equals(name)) {
            Class<?> c = findClass(name);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = basePath + name.replace('.', '/') + ".class";
        byte[] bytes;
        try {
            bytes = readClassBytes(path);
        } catch (IOException e) {
            throw new ClassNotFoundException("无法读取: " + path, e);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] readClassBytes(String path) throws IOException {
        // 优先从当前目录/classpath 读（便于在 IDE 或 classloader 目录下运行）
        try (InputStream in = getParent().getResourceAsStream(path)) {
            if (in != null) {
                return in.readAllBytes();
            }
        }
        // 再从文件系统读
        Path p = Path.of(path);
        if (Files.exists(p)) {
            return Files.readAllBytes(p);
        }
        throw new IOException("未找到: " + path);
    }
}
