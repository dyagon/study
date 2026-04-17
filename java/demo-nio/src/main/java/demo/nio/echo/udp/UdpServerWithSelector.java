package demo.nio.echo.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Set;


public class UdpServerWithSelector {

    public static void main(String[] args) throws IOException {

        Selector selector = Selector.open();

        DatagramChannel channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress("localhost", 18899));
        channel.register(selector, SelectionKey.OP_READ);

        System.out.println("Server started on port 18899");
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (true) {
            if (selector.select() == 0) {
                continue;
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    DatagramChannel clientChannel = (DatagramChannel) key.channel();
                    buffer.clear();
                    SocketAddress senderAddress = clientChannel.receive(buffer);
                    if (senderAddress != null) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String message = new String(data);
                        System.out
                                .println("Received message from " + senderAddress + ": " + message);

                        String echoMessage = "Echo: " + message;
                        ByteBuffer sendBuffer = ByteBuffer.wrap(echoMessage.getBytes());
                        clientChannel.send(sendBuffer, senderAddress);
                    }
                }
                iterator.remove();
            }
        }
    }


}
