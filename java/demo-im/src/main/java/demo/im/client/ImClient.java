package demo.im.client;

import demo.im.protocol.ImMessage.ChatRequest;
import demo.im.protocol.ImMessage.CommandType;
import demo.im.protocol.ImMessage.LoginRequest;
import demo.im.protocol.ImMessage.LogoutRequest;
import demo.im.protocol.ImMessage.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ImClient {
    private final String host;
    private final int port;

    public ImClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @SuppressWarnings({"java:S106", "java:S3776"})
    public void run() throws Exception {
        EventLoopGroup group;
        Class channelClass;

        if (Epoll.isAvailable()) {
            group = new EpollEventLoopGroup();
            channelClass = EpollSocketChannel.class;
        } else if (KQueue.isAvailable()) {
            group = new KQueueEventLoopGroup();
            channelClass = KQueueSocketChannel.class;
        } else {
            group = new NioEventLoopGroup();
            channelClass = NioSocketChannel.class;
        }

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(channelClass)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                     ch.pipeline().addLast(new ProtobufDecoder(Packet.getDefaultInstance()));
                     ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                     ch.pipeline().addLast(new ProtobufEncoder());
                     ch.pipeline().addLast(new ImClientHandler());
                 }
             });

            ChannelFuture f = b.connect(host, port).sync();
            System.out.println("Connected to IM Server at " + host + ":" + port);

            Channel channel = f.channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                if (line.startsWith("login ")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 2) {
                        String userId = parts[1];
                        LoginRequest req = LoginRequest.newBuilder()
                                .setUserId(userId)
                                .setPassword("pwd")
                                .build();
                        Packet packet = Packet.newBuilder()
                                .setCommand(CommandType.LOGIN_REQUEST)
                                .setLoginRequest(req)
                                .build();
                        channel.writeAndFlush(packet);
                    } else {
                        System.out.println("Usage: login <userId>");
                    }
                } else if (line.startsWith("send ")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length == 3) {
                        String toUser = parts[1];
                        String content = parts[2];
                        ChatRequest req = ChatRequest.newBuilder()
                                .setToUserId(toUser)
                                .setContent(content)
                                .build();
                        Packet packet = Packet.newBuilder()
                                .setCommand(CommandType.CHAT_REQUEST)
                                .setChatRequest(req)
                                .build();
                        channel.writeAndFlush(packet);
                    } else {
                        System.out.println("Usage: send <toUser> <message>");
                    }
                } else if ("logout".equals(line)) {
                    Packet packet = Packet.newBuilder()
                            .setCommand(CommandType.LOGOUT_REQUEST)
                            .setLogoutRequest(LogoutRequest.newBuilder().setUserId("").build())
                            .build();
                    channel.writeAndFlush(packet);
                } else if ("quit".equals(line)) {
                    channel.close();
                    break;
                } else {
                    System.out.println("Unknown command. Try 'login <userId>', 'send <toUser> <msg>', 'logout' or 'quit'");
                }
            }

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080;
        new ImClient(host, port).run();
    }
}
