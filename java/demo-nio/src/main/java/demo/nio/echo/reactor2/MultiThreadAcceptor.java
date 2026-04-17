package demo.nio.echo.reactor2;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * MultiThreadAcceptor：多线程版本的连接接受处理器
 * 当有新的客户端连接时，将连接分配给一个 Worker Reactor
 */
public class MultiThreadAcceptor implements Runnable {
    
    private final ServerSocketChannel serverChannel;
    private final MultiThreadReactorEchoServer server;
    
    public MultiThreadAcceptor(ServerSocketChannel serverChannel, 
                               MultiThreadReactorEchoServer server) {
        this.serverChannel = serverChannel;
        this.server = server;
    }
    
    @Override
    public void run() {
        try {
            // 接受新的客户端连接
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                System.out.println("Accepted new connection: " + clientChannel.getRemoteAddress());
                
                // 获取下一个 Worker Reactor（轮询方式）
                WorkerReactor workerReactor = server.getNextWorkerReactor();
                
                // 将客户端通道注册到选中的 Worker Reactor
                workerReactor.registerChannel(clientChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

