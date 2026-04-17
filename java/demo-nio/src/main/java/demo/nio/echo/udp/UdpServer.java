package demo.nio.echo.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Set;

public class UdpServer {

    static void receive() throws IOException {

        DatagramChannel channel = DatagramChannel.open();

        channel.configureBlocking(false);

        channel.bind(new InetSocketAddress("localhost", 18899));

        Selector selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);

        while (true) {
            if (selector.select() == 0) {
                continue;
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    DatagramChannel clientChannel = (DatagramChannel) key.channel();
                    SocketAddress address = clientChannel.receive(buffer);
                    buffer.flip();
                    System.out.println("Received message from " + address);
                    System.out.println("Received message: " + new String(buffer.array(), 0, buffer.limit()));
                    buffer.clear();
                }
                iterator.remove();
            }
        }
    }

    public static void main(String[] args) {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress("localhost", 18899));
            System.out.println("Server started on port 18899");
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (true) {
                buffer.clear();
                SocketAddress senderAddress = channel.receive(buffer);
                if (senderAddress == null) {
                    continue;
                }
                buffer.flip();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String message = new String(data);
                System.out.println("Received message from " + senderAddress + ": " + message);
                
                // echo 
                String echoMessage = "Echo: " + message;
                ByteBuffer sendBuffer = ByteBuffer.wrap(echoMessage.getBytes());
                channel.send(sendBuffer, senderAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
