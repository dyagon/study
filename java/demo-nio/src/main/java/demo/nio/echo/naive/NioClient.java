package demo.nio.echo.naive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioClient {

    public static void main(String[] args) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 8080));
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put("Hello, Server!".getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
            socketChannel.close();
            System.out.println("Message sent to server");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
