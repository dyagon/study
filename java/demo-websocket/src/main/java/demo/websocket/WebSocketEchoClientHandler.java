package demo.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client handler to print server echoes and expose handshake completion future.
 */
public class WebSocketEchoClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEchoClientHandler.class);

    private Promise<Void> handshakeFuture;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        // Create a promise tied to this handler's context
        handshakeFuture = ctx.executor().newPromise();
    }

    public Promise<Void> handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketClientProtocolHandler.ClientHandshakeStateEvent) {
            WebSocketClientProtocolHandler.ClientHandshakeStateEvent e =
                    (WebSocketClientProtocolHandler.ClientHandshakeStateEvent) evt;
            if (e == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
                handshakeFuture.setSuccess(null);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            log.info("<- echo text: {}", ((TextWebSocketFrame) frame).text());
        } else if (frame instanceof BinaryWebSocketFrame) {
            log.info("<- echo binary: {} bytes", frame.content().readableBytes());
        } else if (frame instanceof PongWebSocketFrame) {
            log.debug("<- pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("<- close: {}", ((CloseWebSocketFrame) frame).reasonText());
            ctx.close();
        }
    }

    public void sendText(String msg) {
        ChannelHandlerContext c = this.ctx;
        if (c != null && handshakeFuture != null && handshakeFuture.isSuccess()) {
            // Ensure we send from the channel's EventLoop
            c.executor().execute(() -> {
                ChannelHandlerContext ctx = WebSocketEchoClientHandler.this.ctx;
                if (ctx != null && ctx.channel().isActive()) {
                    ctx.writeAndFlush(new TextWebSocketFrame(msg));
                }
            });
        }
    }

    private volatile ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.ctx = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Client error", cause);
        if (handshakeFuture != null && !handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}
