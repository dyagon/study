package demo.nio.echo.reactor2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Boss Reactor：主 Reactor 线程
 * 专门负责处理连接接受（accept）事件
 */
public class BossReactor implements Runnable {
    
    private final ServerSocketChannel serverChannel;
    private final MultiThreadReactorEchoServer server;
    private Selector selector;
    private volatile boolean running = true;
    
    public BossReactor(ServerSocketChannel serverChannel, MultiThreadReactorEchoServer server) 
            throws IOException {
        this.serverChannel = serverChannel;
        this.server = server;
        this.selector = Selector.open();
        
        // 将 ServerSocketChannel 注册到 Selector，监听 ACCEPT 事件
        SelectionKey selectionKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new MultiThreadAcceptor(serverChannel, this.server));
    }
    
    @Override
    public void run() {
        System.out.println("Boss Reactor started");
        
        while (running) {
            try {
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
                    Runnable handler = (Runnable) key.attachment();
                    if (handler != null) {
                        handler.run();
                    }
                    
                    // 移除已处理的事件
                    iterator.remove();
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
        
        try {
            selector.close();
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("Boss Reactor stopped");
    }
    
    public void shutdown() {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }
}

