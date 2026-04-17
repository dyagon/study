public class ThreadOOM {
    public static void main(String[] args) {
        while(true) {
            new Thread(() -> {
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {}
            }).start();
        }
    }
}
