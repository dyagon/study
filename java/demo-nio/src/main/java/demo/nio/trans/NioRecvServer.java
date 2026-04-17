package demo.nio.trans;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NioRecvServer {

    static class Client {
        String fileName;
        long fileLength;
        long startTime;
        InetSocketAddress remoteAddress;
        FileChannel fileChannel;
        long receivedLength = 0;

        public boolean isFinished() {
            return receivedLength >= fileLength;
        }

    }

    Map<SocketChannel, Client> clients = new HashMap<>();
    ByteBuffer buffer = ByteBuffer.allocate(1024);

    public void start() throws IOException {

        try (Selector selector = Selector.open();
                ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.bind(new InetSocketAddress("localhost", 8080));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server started on port 8080");

            while (selector.select() > 0) {
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = ssc.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        Client client = new Client();
                        client.remoteAddress = (InetSocketAddress) clientChannel.getRemoteAddress();
                        clients.put(clientChannel, client);
                    }
                    if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        processData(clientChannel);
                    }
                }
                iterator.remove();
            }

        }
    }

    void processData(SocketChannel clientChannel) throws IOException {
        Client client = clients.get(clientChannel);
        int num = 0;
        try {
            buffer.clear();
            while ((num = clientChannel.read(buffer)) > 0) {
                buffer.flip();
                if (null == client.fileName) {
                    int fileNameLength = buffer.getInt();
                    byte[] fileNameBytes = new byte[fileNameLength];
                    buffer.get(fileNameBytes);
                    String fileName = new String(fileNameBytes);
                    File directory = new File("received");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    System.out.println("Received file name: " + fileName);
                    client.fileName = fileName;
                    String fullPath = directory.getAbsolutePath() + File.separator + fileName;
                    System.out.println("Received file path: " + fullPath);
                    FileChannel fileChannel = new FileOutputStream(fullPath).getChannel();
                    client.fileChannel = fileChannel;
                    long fileLength = buffer.getLong();
                    client.fileLength = fileLength;
                    client.startTime = System.currentTimeMillis();
                    client.receivedLength += buffer.remaining();
                    client.fileChannel.write(buffer);
                    buffer.clear();
                    if (client.isFinished()) {
                        client.fileChannel.close();
                        System.out.println("File received successfully: " + client.fileName);
                    }
                } else {
                    client.receivedLength += num;
                    client.fileChannel.write(buffer);
                    buffer.clear();
                    if (client.isFinished()) {
                        client.fileChannel.close();
                        System.out.println("File received successfully: " + client.fileName);
                    }
                }
                if (num == -1) {
                    clientChannel.close();
                    clients.remove(clientChannel);
                    System.out.println("Client disconnected: " + client.remoteAddress);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        NioRecvServer server = new NioRecvServer();
        server.start();
    }
}
