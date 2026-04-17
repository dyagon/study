package demo.spring.spel;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

/**
 * SpEL 演示入口：先跑程序式 SpEL 示例，再展示在 Spring 中通过 @Value 使用 SpEL。
 */
@SpringBootApplication
public class SpelDemoApplication {

    /** @Value 中使用 SpEL：系统属性、运算、条件、Bean 属性等 */
    @Value("#{systemProperties['user.home']}")
    private String userHome;

    @Value("#{1 + 2 * 3}")
    private int computed;

    @Value("#{${app.demo.number:10} > 5 ? 'big' : 'small'}")
    private String conditional;

    @Value("#{T(java.lang.Runtime).getRuntime().availableProcessors()}")
    private int processorCount;

    @Bean
    public CommandLineRunner runSpelDemo() {
        return args -> {
            System.out.println("========== 程序式 SpEL 示例 ==========");
            SpelDemo.runAll();

            System.out.println("\n========== @Value 中的 SpEL 示例 ==========");
            System.out.println("  @Value(#{systemProperties['user.home']}) => " + userHome);
            System.out.println("  @Value(#{1 + 2 * 3}) => " + computed);
            System.out.println("  @Value(#{${app.demo.number:10} > 5 ? 'big' : 'small'}) => " + conditional);
            System.out.println("  @Value(#{T(java.lang.Runtime).getRuntime().availableProcessors()}) => " + processorCount);
        };
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpelDemoApplication.class)
                .run(args);
    }
}
