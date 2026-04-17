import java.util.ArrayList;
import java.util.List;

public class HeapOOMTest {

    // 创建一个占用内存较大的内部类
    static class OOMObject {
    }

    public static void main(String[] args) {
        List<OOMObject> list = new ArrayList<>();
            while (true) {
                list.add(new OOMObject());
        } 
    }
}
