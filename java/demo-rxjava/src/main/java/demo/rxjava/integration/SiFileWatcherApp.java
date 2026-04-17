package demo.rxjava.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 主应用程序类。
 */
@SpringBootApplication
public class SiFileWatcherApp {

    public static void main(String[] args) {
        SpringApplication.run(SiFileWatcherApp.class, args);
        System.out.println("Spring Integration 文件监控程序已启动。");
        System.out.println("正在监控目录: data/input");
        System.out.println("处理完的文件将被移动到: data/processed");
        System.out.println("按 Ctrl+C 退出。");
    }
}
