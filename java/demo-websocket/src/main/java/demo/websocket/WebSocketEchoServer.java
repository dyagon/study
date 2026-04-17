package demo.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty WebSocket echo server. Echoes text and binary frames back to client.
 */
public class WebSocketEchoServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEchoServer.class);

    public static void main(String[] args) throws InterruptedException {
        int port = 8080;
        if (args != null && args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignore) {}
        }
        new WebSocketEchoServer().start(port);
    }

    public void start(int port) throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        // HTTP handshake support
                        p.addLast(new HttpServerCodec());
                        p.addLast(new HttpObjectAggregator(64 * 1024));
                        // Handle the WebSocket upgrade handshake and control frames on path "/ws"
                        p.addLast(new WebSocketServerProtocolHandler("/ws", null, true));
                        // Echo handler
                        p.addLast(new WebSocketEchoServerHandler());
                    }
                });

            Channel ch = b.bind(port).sync().channel();
            log.info("WebSocket echo server started at ws://0.0.0.0:{}/ws", port);
            ch.closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}

