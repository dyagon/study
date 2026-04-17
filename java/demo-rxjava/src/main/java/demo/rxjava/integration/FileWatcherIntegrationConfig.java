package demo.rxjava.integration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;

import java.io.File;

/**
 * 定义 Spring Integration 的集成流程 (IntegrationFlow)。
 */
@Configuration
public class FileWatcherIntegrationConfig {

    private static final String INPUT_DIR = "data/input";
    private static final String PROCESSED_DIR = "data/processed";

    @Bean
    public IntegrationFlow fileProcessingFlow() {
        return IntegrationFlow
                // 1. 定义流程的起点：一个文件入站通道适配器 (Inbound Channel Adapter)
                .from(Files.inboundAdapter(new File(INPUT_DIR))
                                .autoCreateDirectory(true), // 如果目录不存在，自动创建
                        // 配置轮询器，每2秒检查一次目录
                        e -> e.poller(Pollers.fixedDelay(2000)))

                // 2. 记录一条日志，表明检测到文件
                //    消息的 payload 是一个 java.io.File 对象
                .log(LoggingHandler.Level.INFO, "FileWatcherFlow",
                        m -> "==> 检测到新文件: " + ((File) m.getPayload()).getName())

                // 3. 将文件移动到 'processed' 目录
                //    这是一个出站通道适配器 (Outbound Channel Adapter)
                .handle(Files.outboundAdapter(new File(PROCESSED_DIR))
                        .autoCreateDirectory(true)
                        .deleteSourceFiles(true)) // 移动后删除源文件
                
                .get(); // 构建并返回 IntegrationFlow
    }
}
