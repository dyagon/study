package demo.nio.echo.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Reactor Echo 服务器的测试客户端
 * 支持交互式输入，发送消息并接收服务器的回显
 */
public class ReactorEchoClient {
    
    private final String host;
    private final int port;
    
    public ReactorEchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            
            // 连接到服务器
            socketChannel.connect(new InetSocketAddress(host, port));
            System.out.println("Connected to server: " + socketChannel.getRemoteAddress());
            System.out.println("Type messages to send (type 'exit' to quit):");
            
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            
            while (true) {
                // 读取用户输入
                System.out.print("> ");
                String input;
                try {
                    input = scanner.nextLine();
                } catch (java.util.NoSuchElementException e) {
                    // 输入流已关闭或不可用（非交互式环境）
                    System.out.println("\nInput stream not available. Exiting...");
                    break;
                }
                
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                
                if (input.isEmpty()) {
                    continue;
                }
                
                // 发送消息到服务器
                buffer.clear();
                buffer.put(input.getBytes());
                buffer.flip();
                
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
                
                // 接收服务器的回显
                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] data = new byte[bytesRead];
                    buffer.get(data);
                    String response = new String(data);
                    System.out.println("Echo: " + response);
                } else if (bytesRead == -1) {
                    System.out.println("Server closed the connection");
                    break;
                }
            }
            
            System.out.println("Client disconnected");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        
        ReactorEchoClient client = new ReactorEchoClient(host, port);
        client.start();
    }
}

