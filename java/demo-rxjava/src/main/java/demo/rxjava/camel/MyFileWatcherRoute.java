package demo.rxjava.camel;

import org.apache.camel.builder.RouteBuilder;

/**
 * 定义一个 Camel 路由，用于监控文件目录。
 */
public class MyFileWatcherRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // 定义路由的起始点
        from("file:data/input?noop=true&delay=2000")
            // 给路由一个唯一的ID，方便管理和调试
            .routeId("file-watcher-route")
            // 记录一条日志，说明检测到了文件。
            // ${file:name} 是 Camel 的 Simple Language 表达式，用于获取文件名。
            .log("==> 检测到新文件或变更: ${file:name}")
            // 将文件内容转换为字符串，方便打印
            .convertBodyTo(String.class)
            // 再次记录日志，这次打印文件内容。
            // ${body} 是 Simple Language 表达式，用于获取消息体（即文件内容）。
            .log("    文件内容如下:\n${body}");
    }
}
