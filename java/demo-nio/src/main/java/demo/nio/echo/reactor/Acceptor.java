package demo.nio.echo.reactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Acceptor：处理连接接受事件
 * 当有新的客户端连接时，创建对应的 Handler 来处理该连接
 */
public class Acceptor implements Runnable {
    
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    
    public Acceptor(Selector selector, ServerSocketChannel serverChannel) {
        this.selector = selector;
        this.serverChannel = serverChannel;
    }
    
    @Override
    public void run() {
        try {
            // 接受新的客户端连接
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                System.out.println("Accepted new connection: " + clientChannel.getRemoteAddress());
                
                // 创建 Handler 来处理这个客户端连接
                EchoHandler handler = new EchoHandler(selector, clientChannel);
                
                // 将客户端通道设置为非阻塞模式
                clientChannel.configureBlocking(false);
                
                // 将客户端通道注册到 Selector，监听 READ 事件
                // 将 Handler 作为附件，当有读事件时，会调用 Handler 处理
                SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ);
                selectionKey.attach(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

