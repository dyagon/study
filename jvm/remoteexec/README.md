# 远程执行字节码 Demo（B/S）

这个示例参考《深入理解Java虚拟机》第9章的思路，演示：

1. 浏览器提交 Java 源码（B/S 交互）。
2. 服务端使用 `JavaCompiler` 在内存编译源码，得到字节码（`byte[]`）。
3. 服务端使用自定义 `ClassLoader#defineClass` 加载字节码并执行。

## 目录

- `RemoteExecHttpServer.java`：简单 HTTP 服务（`/` 页面 + `/execute` 接口）
- `RemoteCodeExecutor.java`：编译 + 类加载 + 反射执行
- `InMemoryJavaCompiler.java`：内存编译器
- `ExecutionResult.java`：返回结果模型

## 方法约定

远程类支持两种入口（优先 `execute`）：

- `public static String execute()`
- `public static void main(String[] args)`

## 运行

在项目根目录执行：

```bash
javac remoteexec/*.java
java -cp remoteexec RemoteExecHttpServer
```

启动后打开：

- `http://localhost:8080`

可以在页面中修改并提交源码，服务器会返回：

- 编译/执行是否成功
- 编译出的 class 字节码大小
- 运行输出

## curl 调用示例

```bash
curl -X POST http://localhost:8080/execute \
  --data-urlencode "className=DemoTask" \
  --data-urlencode "sourceCode=public class DemoTask { public static String execute(){ return \"hello remote\"; } }"
```

## 安全提示

本 demo 仅用于学习 JVM 字节码执行链路，不可直接用于生产环境。生产场景需要至少补充：

- 进程隔离或容器沙箱
- CPU/内存/文件/网络权限限制
- 类白名单、API 白名单
- 审计日志与防滥用限流
