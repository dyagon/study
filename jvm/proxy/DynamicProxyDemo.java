import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


public class DynamicProxyDemo {


// 1. 定义接口
interface UserService {
    void addUser(String name);
}

// 2. 目标对象实现类
static class UserServiceImpl implements UserService {
    @Override
    public void addUser(String name) {
        System.out.println("成功添加用户: " + name);
    }
}

// 3. 实现 InvocationHandler (代理逻辑)
static class LogHandler implements InvocationHandler {
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
