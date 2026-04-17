package demo.nio.echo.naive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    public static void main(String[] args) throws IOException {
        // 1. 创建 Selector
        Selector selector = Selector.open();

        // 2. 创建 ServerSocketChannel，绑定端口
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.configureBlocking(false); // 设置为非阻塞

        // 3. 将 ServerSocketChannel 注册到 Selector，监听 ACCEPT 事件
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port 8080...");

        while (true) {
            // 4. 阻塞等待，直到有事件发生
            if (selector.select() == 0) {
                continue;
            }

            // 5. 获取所有就绪的事件
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                // 6. 根据事件类型处理
                if (key.isAcceptable()) {
                    // 是一个新的连接事件
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = ssc.accept(); // 获取客户端通道
                    System.out.println("Accepted new connection: " + clientChannel.getRemoteAddress());

                    clientChannel.configureBlocking(false);
                    // 将这个新通道也注册到 Selector，监听 READ 事件
                    clientChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    // 是一个读事件
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len = clientChannel.read(buffer);

                    if (len > 0) {
                        buffer.flip();
                        String message = new String(buffer.array(), 0, len);
                        System.out.println("Received from " + clientChannel.getRemoteAddress() + ": " + message);

                        // 回写数据
                        clientChannel.write(ByteBuffer.wrap(("Echo: " + message).getBytes()));
                    } else if (len == -1) {
                        // 客户端断开连接
                        System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
                        clientChannel.close();
                    }
                }

                // 7. 移除已处理的事件，防止重复处理
                iterator.remove();
            }
        }
    }

}
