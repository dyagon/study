
# GC test


## GCSimluator

```bash
# java 21
javac GCSimulator.java

# G1
java -Xms50m -Xmx50m -XX:+UseG1GC "-Xlog:gc*=info:file=gc-g1.log:time,tags,level" GCSimulator

# ZGC
java -Xms50m -Xmx50m -XX:+UseZGC "-Xlog:gc*=info:file=gc-zgc.log:time,tags" GCSimulator

# ZGC gen
java -Xms50m -Xmx50m -XX:+UseZGC -XX:+ZGenerational "-Xlog:gc*=info:file=gc-zgc.log:time,tags,level" GCSimulator

# Shenandoah
java -Xms50m -Xmx50m -XX:+UseShenandoahGC "-Xlog:gc*=info:file=gc-shenandoah.log:time,tags,level" GCSimulator

# Serial
java -Xms50m -Xmx50m -XX:+UseSerialGC "-Xlog:gc*=info:file=gc-serial.log:time,tags,level" GCSimulator

# Parallel
java -Xms50m -Xmx50m -XX:+UseParallelGC "-Xlog:gc*=info:file=gc-parallel.log:time,tags,level" GCSimulator

# Epsilon
java -Xms50m -Xmx50m -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC "-Xlog:gc*=info:file=gc-epsilon.log:time,tags,level" GCSimulator

# CMS: must java 11-
java -Xms50m -Xmx50m -XX:+UseConcMarkSweepGC "-Xlog:gc*=info:file=gc-cms.log:time,tags,level" GCSimulator


```

执行上面的（不包括CMS），可以发现一个有趣的现象： ZGC、Shenandoah、G1 分配不到 30MB 就OOM了，而 Serial、Parallel 可以分配到 47MB 左右。 说明后两者占用的内存少。
使用 Epsilon 直接 1MB 挂掉了，因为在程序中的短生命对象直接撑爆了堆。

```bash
# Epsilon
java -Xms50m -Xmx50m -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC "-Xlog:gc*=info:file=gc-epsilon.log:time,tags,level" GCSimulator
```
```
Starting GC log generation experiment...
Iteration 10: Added 1MB to long-lived list. Total list size: 1 MB
Terminating due to java.lang.OutOfMemoryError: Java heap space
```