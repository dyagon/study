package demo.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Netty WebSocket echo client. Reads from stdin by default and prints echoed frames.
 */
public class WebSocketEchoClient {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEchoClient.class);

    public static void main(String[] args) throws Exception {
        String uriStr = args.length > 0 ? args[0] : "ws://localhost:8080/ws";
        String oneShotMessage = args.length > 1 ? args[1] : null;
        new WebSocketEchoClient().run(uriStr, oneShotMessage);
    }

    public void run(String uriStr, String oneShotMessage) throws Exception {
        URI uri = new URI(uriStr);
        final String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        final String host = uri.getHost() == null ? "localhost" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            port = "wss".equalsIgnoreCase(scheme) ? 443 : 80;
        } else {
            port = uri.getPort();
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
        if (ssl) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE) // demo only
                    .build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            final WebSocketEchoClientHandler wsHandler = new WebSocketEchoClientHandler();
            WebSocketClientProtocolConfig config = WebSocketClientProtocolConfig.newBuilder()
                    .webSocketUri(uri)
                    .customHeaders(new DefaultHttpHeaders())
                    .handleCloseFrames(true)
                    .build();

            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<Channel>() {
                 @Override
                 protected void initChannel(Channel ch) {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                     }
                     p.addLast(new HttpClientCodec());
                     p.addLast(new HttpObjectAggregator(64 * 1024));
                     p.addLast(new WebSocketClientProtocolHandler(config));
                     p.addLast(wsHandler);
                 }
             });

            Channel ch = b.connect(host, port).sync().channel();

            // Wait for handshake complete before sending data
            wsHandler.handshakeFuture().sync();
            log.info("Handshake complete, connected to {}:{}{}", host, port, uri.getPath());

            if (oneShotMessage != null) {
                wsHandler.sendText(oneShotMessage);
                // Wait a moment for echo then close
                ch.eventLoop().schedule(() -> ch.close(), 500, java.util.concurrent.TimeUnit.MILLISECONDS);
            } else {
                // Read from stdin and send to server until EOF or 'exit'
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String line;
                System.out.println("Type messages to send. 'exit' to quit.");
                while ((line = in.readLine()) != null) {
                    if (line.trim().equalsIgnoreCase("exit")) {
                        ch.close();
                        break;
                    }
                    wsHandler.sendText(line);
                }
            }

            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

