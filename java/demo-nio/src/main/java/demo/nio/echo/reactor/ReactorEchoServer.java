package demo.nio.echo.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 单线程 Reactor 模式的 Echo 服务器
 * 
 * Reactor 模式的核心组件：
 * 1. Reactor：负责监听和分发事件
 * 2. Acceptor：处理连接接受事件
 * 3. Handler：处理具体的业务逻辑（读写事件）
 */
public class ReactorEchoServer {
    
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final int port;
    
    public ReactorEchoServer(int port) throws IOException {
        this.port = port;
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.bind(new InetSocketAddress(port));
        this.serverChannel.configureBlocking(false);
        
        // 将 ServerSocketChannel 注册到 Selector，监听 ACCEPT 事件
        // 使用 Acceptor 作为附件，当有连接事件时，会调用 Acceptor 处理
        SelectionKey selectionKey = this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor(selector, serverChannel));
    }
    
    /**
     * 启动 Reactor 事件循环
     */
    public void start() throws IOException {
        System.out.println("Reactor Echo Server started on port " + port);
        
        while (true) {
            // 阻塞等待事件发生
            if (selector.select() == 0) {
                continue;
            }
            
            // 获取所有就绪的事件
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                
                // 分发事件：从 SelectionKey 中获取附件（Runnable），并执行
                // Acceptor 和 Handler 都实现了 Runnable 接口
                Runnable handler = (Runnable) key.attachment();
                if (handler != null) {
                    handler.run();
                }
                
                // 移除已处理的事件
                iterator.remove();
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        ReactorEchoServer server = new ReactorEchoServer(port);
        server.start();
    }
}

