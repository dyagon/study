# 类加载器 (ClassLoader) 演示

## 说明

- **ClassLoaderDemo.java**：打印类加载器层次、委托链，并用**自定义 ClassLoader** 加载 `ToLoad` 并调用其方法。
- **MyClassLoader.java**：自定义类加载器，从指定目录（或 classpath 资源）读取 `.class` 并 `defineClass`；对类名 `ToLoad` 不委托给父加载器，由本加载器加载。
- **ToLoad.java**：供自定义加载器加载的简单类，仅有一个 `say()` 方法。

## 编译与运行

```bash
# 在项目根目录编译
javac classloader/ClassLoaderDemo.java classloader/MyClassLoader.java classloader/ToLoad.java

# 运行（-cp classloader 让类在 classloader 目录下）
java -cp classloader ClassLoaderDemo
```

或在 `classloader` 目录下：

```bash
cd classloader
javac ClassLoaderDemo.java MyClassLoader.java ToLoad.java
java -cp . ClassLoaderDemo
```

## 编译与运行

```bash
javac ClassLoaderDemo.java
java ClassLoaderDemo
```

## 预期输出示例

```
ClassLoaderDemo 的类加载器: jdk.internal.loader.ClassLoaders$AppClassLoader@xxx

--- 委托链 (parent) ---
  jdk.internal.loader.ClassLoaders$AppClassLoader@xxx
  jdk.internal.loader.ClassLoaders$PlatformClassLoader@xxx
  null (Bootstrap ClassLoader)

--- 常见类的加载器 ---
  String (Bootstrap): null (Bootstrap)
  ClassLoaderDemo (App): jdk.internal.loader.ClassLoaders$AppClassLoader@xxx
  当前线程上下文: jdk.internal.loader.ClassLoaders$AppClassLoader@xxx
```

- **Bootstrap**：加载核心类（如 java.lang.*），用 `null` 表示。
- **PlatformClassLoader**：JDK 9+ 替代原 ExtClassLoader，加载平台/扩展类。
- **AppClassLoader**：加载 classpath 下的应用类。
