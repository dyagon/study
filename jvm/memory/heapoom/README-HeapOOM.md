# Java 堆内存溢出 (Heap OOM) 演示

## 说明

`HeapOOMDemo.java` 通过持续分配大对象（每次 1MB）直到堆耗尽，用于复现并观察 `java.lang.OutOfMemoryError: Java heap space`。

## 编译与运行

```bash
# 编译
javac HeapOOMDemo.java

# 使用较小堆快速触发 OOM（推荐）
java -Xmx64m HeapOOMDemo

# 使用 128MB 堆
java -Xmx128m HeapOOMDemo
```

## 可选：打印 GC 信息

```bash
java -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError HeapOOMTest
```

## 预期输出示例

```
开始分配对象，直到堆内存耗尽...
当前堆最大内存: 64 MB

已分配 10 MB
已分配 20 MB
...
已分配 50 MB

>>> 发生堆内存溢出 <<<
异常类型: java.lang.OutOfMemoryError
异常信息: Java heap space
java.lang.OutOfMemoryError: Java heap space
	at HeapOOMDemo.main(HeapOOMDemo.java:...)
```

## 参数说明

- `-Xmx64m`：最大堆 64MB，便于快速复现 OOM
- 不设 `-Xmx` 时使用 JVM 默认堆大小，可能需要较长时间才会 OOM
