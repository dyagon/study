package demo.nio.echo.udp;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.io.InputStreamReader;


public class UdpEchoClient {

    public static void main(String[] args) {
        try (DatagramChannel channel = DatagramChannel.open();
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader reader = new BufferedReader(inputStreamReader)) {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", 18899);
            System.out.println("UDP Echo Client started");
            System.out.println("Enter messages to send to server (type 'exit' to quit)");
            String message;
            ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
            while ((message = reader.readLine()) != null) {
                if ("exit".equalsIgnoreCase(message.trim())) {
                    break;
                }
                ByteBuffer sendBuffer = ByteBuffer.wrap(message.getBytes());
                channel.send(sendBuffer, serverAddress);
                System.out.println("Sent message: " + message);
                // receive echo message
                receiveBuffer.clear();
                channel.receive(receiveBuffer);
                receiveBuffer.flip();
                byte[] data = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(data);
                String echoMessage = new String(data);
                System.out.println("Received echo message: " + echoMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
