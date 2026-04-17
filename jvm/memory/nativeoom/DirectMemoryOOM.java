import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DirectMemoryOOM {
    // 每次申请 1MB
    private static final int ONE_MB = 1024 * 1024;

    public static void main(String[] args) {
        List<ByteBuffer> list = new ArrayList<>();
        int count = 0;
        try {
            while (true) {
                // 申请堆外内存
                ByteBuffer buffer = ByteBuffer.allocateDirect(ONE_MB);
                list.add(buffer);
                count++;
                System.out.println("Allocated " + count + " MB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
