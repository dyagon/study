

# 内存测试


## JVM 内存

### RuntimeConstantPool

```bash
cd runtimeoom
javac RuntimeConstantPoolOOM.java
❯ java -Xms10m -Xmx10m RuntimeConstantPoolOOM

Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
        at RuntimeConstantPoolOOM.main(RuntimeConstantPoolOOM.java:16)
```


### MetaspaceOOM

```bash
avac MetaspaceOOM.java
❯ java -XX:MetaspaceSize=10m -XX:MaxMetaspaceSize=10m MetaspaceOOM

java.lang.OutOfMemoryError: Metaspace
        at java.base/java.lang.ClassLoader.defineClass1(Native Method)
        at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1027)
        at java.base/java.security.SecureClassLoader.defineClass(SecureClassLoader.java:150)
        at java.base/java.net.URLClassLoader.defineClass(URLClassLoader.java:524)
        at java.base/java.net.URLClassLoader$1.run(URLClassLoader.java:427)
        at java.base/java.net.URLClassLoader$1.run(URLClassLoader.java:421)
        at java.base/java.security.AccessController.executePrivileged(AccessController.java:809)
        at java.base/java.security.AccessController.doPrivileged(AccessController.java:714)
        at java.base/java.net.URLClassLoader.findClass(URLClassLoader.java:420)
        at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:593)
        at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:526)
        at MetaspaceOOM.main(MetaspaceOOM.java:17)
```



## 堆外内存

!!!!!测试 Native Memory（堆外内存）溢出非常危险，极有可能导致你的主机死机、鼠标卡顿、或者操作系统崩溃。

一定要在安全环境中测试（docker）。直接内存分配相对安全一些，可以限制最大堆外内存。

### 直接内存

```bash
cd nativeoom
javac DirectMemoryOOM.java
java -XX:MaxDirectMemorySize=1m DirectMemoryOOM
```
```
Allocated 1 MB
Exception in thread "main" java.lang.OutOfMemoryError: Cannot reserve 1048576 bytes of direct buffer memory (allocated: 1048576, limit: 1048576)
        at java.base/java.nio.Bits.reserveMemory(Bits.java:178)
        at java.base/java.nio.DirectByteBuffer.<init>(DirectByteBuffer.java:127)
        at java.base/java.nio.ByteBuffer.allocateDirect(ByteBuffer.java:360)
```


docker方法

```bash
cd nativeoom
docker run -it --rm --memory=50m -v "$PWD":/app -w /app eclipse-temurin:21 \
  sh -c "javac DirectMemoryOOM.java && java -XX:MaxDirectMemorySize=1m DirectMemoryOOM"
```



### 线程 （docker！！！）


```bash
cd nativeoom
docker run -it --rm --pids-limit 100 --memory=100m -v "$PWD":/app -w /app eclipse-temurin:21 \
  sh -c "javac ThreadOOM.java && java ThreadOOM"
```

```
[0.016s][warning][os,thread] Failed to start thread "Unknown thread" - pthread_create failed (EAGAIN) for attributes: stacksize: 2040k, guardsize: 0k, detached.
[0.016s][warning][os,thread] Failed to start the native thread for java.lang.Thread "Thread-86"
Exception in thread "main" java.lang.OutOfMemoryError: unable to create native thread: possibly out of memory or process/resource limits reached
        at java.base/java.lang.Thread.start0(Native Method)
        at java.base/java.lang.Thread.start(Thread.java:1526)
        at ThreadOOM.main(ThreadOOM.java:8)
```

