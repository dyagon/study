/**
 * 供自定义类加载器加载的简单类（与 ClassLoaderDemo 同目录，便于读取 .class 文件）。
 */
public class ToLoad {
    public String say() {
        return "由自定义 ClassLoader 加载的 ToLoad";
    }
}
