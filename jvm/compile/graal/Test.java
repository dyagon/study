public class Test {
    // 这是一个我们想要观察其编译过程的目标方法
    public static int compute(int a, int b) {
        int result = a + b;
        return result * 2;
    }

    public static void main(String[] args) {
        int sum = 0;
        // 循环执行足够多的次数，触发 HotSpot 阈值，让 Graal JIT 介入编译
        for (int i = 0; i < 20000; i++) {
            sum += compute(i, i % 10);
        }
        System.out.println("Result: " + sum);
    }
}
