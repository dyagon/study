/**
 * 通过增加方法内局部变量数量，使每个栈帧变大，从而更快耗尽栈空间。
 * 对比：无变量的 stackLeak() 可递归约 1 万次，带大量变量的 stackLeak() 可能只有几百次。
 */
public class StackOverflowByVariables {

    private int stackLength = 0;

    public void stackLeak() {
        stackLength++;
        // 每个栈帧中分配大量局部变量，增大栈帧体积
        long a0 = 0, a1 = 1, a2 = 2, a3 = 3, a4 = 4, a5 = 5, a6 = 6, a7 = 7, a8 = 8, a9 = 9;
        long b0 = 0, b1 = 1, b2 = 2, b3 = 3, b4 = 4, b5 = 5, b6 = 6, b7 = 7, b8 = 8, b9 = 9;
        long c0 = 0, c1 = 1, c2 = 2, c3 = 3, c4 = 4, c5 = 5, c6 = 6, c7 = 7, c8 = 8, c9 = 9;
        long d0 = 0, d1 = 1, d2 = 2, d3 = 3, d4 = 4, d5 = 5, d6 = 6, d7 = 7, d8 = 8, d9 = 9;
        stackLeak();
    }

    public static void main(String[] args) {
        StackOverflowByVariables test = new StackOverflowByVariables();
        try {
            test.stackLeak();
        } catch (Throwable e) {
            System.out.println("栈深度: " + test.stackLength);
            throw e;
        }
    }
}
