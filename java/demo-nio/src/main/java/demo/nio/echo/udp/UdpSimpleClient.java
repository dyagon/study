package demo.nio.echo.udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.io.InputStreamReader;



public class UdpSimpleClient {

    static void send() throws IOException {

        DatagramChannel channel = DatagramChannel.open();

        channel.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String message = scanner.nextLine();
            buffer.put((LocalDateTime.now().toString() + " >> " + message).getBytes());
            buffer.flip();
            channel.send(buffer, new InetSocketAddress("localhost", 18899));
            System.out.println("Sent message: " + message);
            buffer.clear();
        }
        System.out.println("Closing channel and scanner");
        channel.close();
        scanner.close();
    }

    public static void main(String[] args) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", 18899);
            String message = "hello world";
            ByteBuffer sendBuffer = ByteBuffer.wrap(message.getBytes());
            channel.send(sendBuffer, serverAddress);
            System.out.println("Sent message: " + message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
