public class VersionTest {
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.version"));
        System.out.println(System.getProperty("java.home"));
        System.out.println(System.getProperty("java.vm.name")); // 看看是不是你编译时的名字
    }
}
