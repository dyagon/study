/**
 * 演示 JVM 类加载器层次：查看常见类由哪个 ClassLoader 加载，以及父子委托链。
 */
public class ClassLoaderDemo {

    public static void main(String[] args) {
        // 1. 当前类的加载器（通常是 AppClassLoader）
        ClassLoader loader = ClassLoaderDemo.class.getClassLoader();
        System.out.println("ClassLoaderDemo 的类加载器: " + loader);

        // 2. 打印委托链（从当前加载器到顶层）
        System.out.println("\n--- 委托链 (parent) ---");
        ClassLoader current = loader;
        while (current != null) {
            System.out.println("  " + current);
            current = current.getParent();
        }
        // Bootstrap 用 null 表示
        System.out.println("  null (Bootstrap ClassLoader)");

        // 3. 常见类由谁加载
        System.out.println("\n--- 常见类的加载器 ---");
        printLoader("String (Bootstrap)", String.class.getClassLoader());
        printLoader("ClassLoaderDemo (App)", ClassLoaderDemo.class.getClassLoader());
        printLoader("当前线程上下文", Thread.currentThread().getContextClassLoader());

        // 4. 系统类加载器（应用默认）
        System.out.println("\n--- 系统类加载器 ---");
        System.out.println("ClassLoader.getSystemClassLoader(): " + ClassLoader.getSystemClassLoader());

        // 5. 自定义类加载器加载并使用 ToLoad
        System.out.println("\n--- 自定义 ClassLoader 加载并使用 ToLoad ---");
        // basePath "" 表示从当前 classpath 根读取（-cp classloader 时即为 classloader 目录）
        MyClassLoader myLoader = new MyClassLoader(loader, "");
        try {
            Class<?> toLoadClass = myLoader.loadClass("ToLoad");
            System.out.println("ToLoad 的类加载器: " + toLoadClass.getClassLoader());
            Object instance = toLoadClass.getDeclaredConstructor().newInstance();
            String msg = (String) toLoadClass.getMethod("say").invoke(instance);
            System.out.println("调用 say(): " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printLoader(String name, ClassLoader cl) {
        System.out.println("  " + name + ": " + (cl == null ? "null (Bootstrap)" : cl));
    }
}
