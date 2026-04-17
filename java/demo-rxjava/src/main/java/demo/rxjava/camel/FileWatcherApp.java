package demo.rxjava.camel;
    
import org.apache.camel.main.Main;

/**
 * 主应用程序类，用于启动 Camel。
 */
public class FileWatcherApp {

    public static void main(String[] args) throws Exception {
        // 创建 Camel Main 实例
        Main main = new Main();

        // 添加我们定义的路由构建器
        // Camel Main 会自动发现并启动这个路由
        main.configure().addRoutesBuilder(new MyFileWatcherRoute());

        // 运行 Camel 应用
        // 这会启动 CamelContext，并且会阻塞主线程，使应用持续运行
        System.out.println("Apache Camel 文件监控程序已启动。");
        System.out.println("正在监控目录: data/input");
        System.out.println("按 Ctrl+C 退出。");
        main.run(args);
    }
}
