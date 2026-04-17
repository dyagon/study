# Java 栈溢出 (Stack Overflow) 演示

## 示例一：纯递归

`StackOverflowTest.java` 通过无限递归调用，使栈帧不断入栈直至超出栈容量。

## 示例二：大栈帧（局部变量占满栈帧）

`StackOverflowByVariables.java` 在每次递归中声明大量局部变量（40 个 long），使每个栈帧变大，相同栈容量下可容纳的递归层数显著减少。

## 编译与运行

```bash
# 示例一：纯递归
javac StackOverflowTest.java
java -Xss1m StackOverflowTest

# 示例二：大栈帧（对比可见深度明显减少）
javac StackOverflowByVariables.java
java -Xss1m StackOverflowByVariables
```

## 预期输出示例

```
Exception in thread "main" java.lang.StackOverflowError
	at StackOverflowTest.recurse(StackOverflowTest.java:8)
	at StackOverflowTest.recurse(StackOverflowTest.java:8)
	at StackOverflowTest.recurse(StackOverflowTest.java:8)
	...
```

## 实验


### 1. 普通递归方法的栈深度检查

```
❯ java -Xss1m StackOverflowTest 2> error.log
栈深度: 7992
❯ java -Xss512k StackOverflowTest 2> error.log
栈深度: 3310
❯ java -Xss256k StackOverflowTest 2> error.log
栈深度: 970
```

### 2. 增加局部变量之后的

```
java -Xss256k StackOverflowByVariables 2> error.log
栈深度: 144
```

### 3. 