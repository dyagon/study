package demo.nio.trans;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

public class NioSendClient {

    static void sendFile(String sourcePath) throws IOException {

        SocketChannel socketChannel = SocketChannel.open();

        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        socketChannel.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        File file = new File(sourcePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + sourcePath);
        }

        String fileName = new File(sourcePath).getName();
        // send file name and length
        int fileNameLength = fileName.length();
        buffer.putInt(fileNameLength);
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        buffer.put(fileName.getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        long fileLength = file.length();
        buffer.putLong(fileLength);
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        // send file content
        try (FileInputStream fileInputStream = new FileInputStream(file);
                FileChannel sourceChannel = fileInputStream.getChannel()) {
            System.out.println("Sending file content...");
            int len = 0;
            long progress = 0;
            while ((len = sourceChannel.read(buffer)) > 0) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
                progress += len;
                System.out.println("Progress: " + progress + " / " + fileLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File sent successfully");
        socketChannel.close();
    }

    public static void main(String[] args) {
        try {
            sendFile("/Users/dyagon/Workspace/study/react-java/demo/src/main/java/demo/App.java");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
