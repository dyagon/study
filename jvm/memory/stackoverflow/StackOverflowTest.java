public class StackOverflowTest {


    private int stackLength = 0;
    
    public void stackLeak() {
        stackLength++;
        stackLeak();
    }

    public static void main(String[] args) {
        StackOverflowTest test = new StackOverflowTest();
        try {
            test.stackLeak();
        } catch (Throwable e) {
            System.out.println("栈深度: " + test.stackLength);
            throw e;
        }
    }
}
