在 Java 中，动态代理的本质是在**运行期动态生成类的字节码**并加载到 JVM 中。最常见的实现方式有两种：**JDK 动态代理**（基于接口）和 **CGLIB 动态代理**（基于子类）。

为了清晰说明字节码是如何生成的，我们以 JDK 标准的动态代理为例。我们不仅会写出代理的实现逻辑，还会通过开启 JVM 参数，将内存中生成的字节码文件（`.class`）保存到磁盘上，并反编译看看它到底生成了什么。

### 一、 编写动态代理基础代码

首先，我们需要一个接口、一个目标实现类，以及一个 `InvocationHandler`。

```java
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 1. 定义接口
interface UserService {
    void addUser(String name);
}

// 2. 目标对象实现类
class UserServiceImpl implements UserService {
    @Override
    public void addUser(String name) {
        System.out.println("成功添加用户: " + name);
    }
}

// 3. 实现 InvocationHandler (代理逻辑)
class LogHandler implements InvocationHandler {
    private Object target; // 被代理的目标对象

    public LogHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("[日志] 方法执行前: " + method.getName());
        Object result = method.invoke(target, args); // 反射调用目标方法
        System.out.println("[日志] 方法执行后: " + method.getName());
        return result;
    }
}
```

### 二、 触发字节码生成并保存

在 Java 运行时，`Proxy.newProxyInstance` 会在内存中直接生成代理类的字节码。为了看到这个字节码，我们需要配置一个系统属性，让 JVM 将生成的 `.class` 文件保存到本地。

```java
public class DynamicProxyDemo {
    public static void main(String[] args) {
        // 【关键配置】将 JDK 动态生成的字节码保存到磁盘
        // 注意：JDK 8 及以下使用 "sun.misc.ProxyGenerator.saveGeneratedFiles"
        // JDK 9 及以上使用 "jdk.proxy.ProxyGenerator.saveGeneratedFiles"
        System.setProperty("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");

        UserService target = new UserServiceImpl();
        LogHandler handler = new LogHandler(target);

        // 生成代理类实例
        UserService proxy = (UserService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                handler
        );

        // 调用代理方法
        proxy.addUser("张三");
        
        System.out.println("代理类的名字: " + proxy.getClass().getName());
    }
}
```

运行上述代码，控制台会输出：
```text
[日志] 方法执行前: addUser
成功添加用户: 张三
[日志] 方法执行后: addUser
代理类的名字: com.sun.proxy.$Proxy0
```

同时，在项目根目录下会生成一个名为 `com/sun/proxy/$Proxy0.class` 的文件。**这就是在内存中动态生成的字节码文件。**

### 三、 揭秘生成的字节码 ($Proxy0.class)

JVM 底层是通过 `ProxyGenerator.generateClassFile()` 方法，按照 Class 文件的规范，拼接出魔数、常量池、字段表、方法表等，最终生成了字节码数组（`byte[]`），然后通过类加载器加载到 JVM 内存中。

如果我们使用反编译工具（如 JD-GUI 或 CFR）打开生成的 `$Proxy0.class`，你会看到类似如下的 Java 代码（为了方便理解，做了适度精简和注释）：

```java
package com.sun.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;

// 1. 代理类继承了 Proxy，并且实现了我们传入的 UserService 接口
// （这解释了为什么 JDK 动态代理只能代理接口，因为 Java 是单继承的，它已经继承了 Proxy）
public final class $Proxy0 extends Proxy implements UserService {

    // 2. 提前提取出所有要代理的 Method 对象作为静态变量
    private static Method m1; // Object.equals
    private static Method m2; // Object.toString
    private static Method m3; // UserService.addUser (我们的业务方法)
    private static Method m0; // Object.hashCode

    // 3. 构造函数，接收我们自己写的 InvocationHandler
    public $Proxy0(InvocationHandler var1) throws  {
        super(var1); // 调用父类 Proxy 的构造器，将 handler 存入父类的 this.h 中
    }

    // 4. 重写业务方法
    public final void addUser(String var1) throws  {
        try {
            // 【核心逻辑】直接调用我们编写的 LogHandler.invoke() 方法
            // 传入：当前的代理实例(this)，目标方法(m3)，参数(new Object[]{var1})
            super.h.invoke(this, m3, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    // 5. 静态代码块，在类加载时利用反射获取所有相关 Method 对象的引用
    static {
        try {
            m1 = Class.forName("java.lang.Object").getMethod("equals", Class.forName("java.lang.Object"));
            m2 = Class.forName("java.lang.Object").getMethod("toString");
            m3 = Class.forName("UserService").getMethod("addUser", Class.forName("java.lang.String"));
            m0 = Class.forName("java.lang.Object").getMethod("hashCode");
        } catch (NoSuchMethodException var2) {
            throw new NoSuchMethodError(var2.getMessage());
        } catch (ClassNotFoundException var3) {
            throw new NoClassDefFoundError(var3.getMessage());
        }
    }
    
    // (省略 equals, toString, hashCode 方法的具体实现，结构与 addUser 相同，都是委托给 super.h.invoke)
}
```

### 四、 字节码生成的总结

从反编译的结果中，我们可以清晰地看到动态代理的整个闭环：

1. **生成签名**：底层直接拼装字节码，声明一个名为 `$Proxy0` 的类，继承 `java.lang.reflect.Proxy`，并实现你传入的接口（`UserService`）。
2. **生成方法**：遍历你传入的接口的所有方法（加上 Object 的 `toString/equals/hashCode`），为每一个方法在字节码中生成对应的静态 `Method` 对象，并在静态代码块 `static {}` 中完成初始化。
3. **路由请求**：在生成的接口方法实现中（如 `addUser`），不包含任何业务逻辑，而是统一调用父类（`Proxy`）保存的 `InvocationHandler`（也就是我们写的 `LogHandler`）的 `invoke()` 方法。
4. **加载运行**：最后，通过 `ClassLoader.defineClass()` 将拼装好的 `byte[]` 字节数组转化为 JVM 内存中的 Class 对象。

