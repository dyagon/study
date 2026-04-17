public class StackOverflowTest2 {
    
    private static int stackLength = 0;
    
    public static void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public static void main(String[] args) {
        try {
            stackLeak();
        } catch (Throwable e) {
            System.out.println("栈深度: " + stackLength);
            throw e;
        }
    }
}
