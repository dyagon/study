package demo.nio.echo.reactor2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * MultiThreadEchoHandler：多线程版本的 Echo 处理器
 * 处理客户端的读写事件，读取客户端发送的数据并回显，就是非常简单的回显处理
 */
public class MultiThreadEchoHandler implements Runnable {
    
    private final Selector selector;
    private final SocketChannel clientChannel;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private static final int BUFFER_SIZE = 1024;
    
    public MultiThreadEchoHandler(Selector selector, SocketChannel clientChannel) {
        this.selector = selector;
        this.clientChannel = clientChannel;
        this.readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        this.writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    }
    
    @Override
    public void run() {
        try {
            SelectionKey key = clientChannel.keyFor(selector);
            if (key == null) {
                return;
            }
            
            if (key.isReadable()) {
                handleRead(key);
            } else if (key.isWritable()) {
                handleWrite(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }
    
    /**
     * 处理读事件：读取客户端发送的数据
     */
    private void handleRead(SelectionKey key) throws IOException {
        readBuffer.clear();
        int bytesRead = clientChannel.read(readBuffer);
        
        if (bytesRead > 0) {
            // 读取到数据，准备回显
            readBuffer.flip();
            
            // 将读取的数据复制到写缓冲区
            writeBuffer.clear();
            writeBuffer.put(readBuffer);
            writeBuffer.flip();
            
            // 打印接收到的消息
            byte[] data = new byte[bytesRead];
            readBuffer.rewind();
            readBuffer.get(data);
            String message = new String(data);
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    "Received from " + clientChannel.getRemoteAddress() + ": " + message);
            
            // 切换到写模式，监听 WRITE 事件
            key.interestOps(SelectionKey.OP_WRITE);
        } else if (bytesRead == -1) {
            // 客户端断开连接
            System.out.println("[" + Thread.currentThread().getName() + "] " +
                    "Client disconnected: " + clientChannel.getRemoteAddress());
            closeConnection();
        }
    }
    
    /**
     * 处理写事件：将数据回显给客户端
     */
    private void handleWrite(SelectionKey key) throws IOException {
        if (writeBuffer.hasRemaining()) {
            int bytesWritten = clientChannel.write(writeBuffer);
            
            if (bytesWritten == 0 && writeBuffer.hasRemaining()) {
                // 数据未完全写入，继续监听写事件
                key.interestOps(SelectionKey.OP_WRITE);
            } else {
                // 数据已完全写入，切换回读模式
                writeBuffer.clear();
                key.interestOps(SelectionKey.OP_READ);
            }
        } else {
            // 没有数据要写，切换回读模式
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    /**
     * 关闭连接
     */
    private void closeConnection() {
        try {
            SelectionKey key = clientChannel.keyFor(selector);
            if (key != null) {
                key.cancel();
            }
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

