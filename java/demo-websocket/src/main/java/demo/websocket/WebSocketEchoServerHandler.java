package demo.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Echo WebSocket frames back to the client. Supports text & binary frames.
 */
public class WebSocketEchoServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(WebSocketEchoServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Client connected: {}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Client disconnected: {}", ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof TextWebSocketFrame) {
            String text = ((TextWebSocketFrame) frame).text();
            log.info("<- text: {}", text);
            ctx.writeAndFlush(new TextWebSocketFrame(text)); // echo
        } else if (frame instanceof BinaryWebSocketFrame) {
            ByteBuf data = frame.content().retain();
            log.info("<- binary: {} bytes", data.readableBytes());
            // echo binary payload
            ctx.writeAndFlush(new BinaryWebSocketFrame(data));
        } else if (frame instanceof PingWebSocketFrame) {
            // Protocol handler normally handles ping/pong, but just in case
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
        } else {
            // Continuation and other frames: convert to text for visibility
            ByteBuf content = frame.content();
            ctx.writeAndFlush(new TextWebSocketFrame(content.toString(CharsetUtil.UTF_8)));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Server error", cause);
        ctx.writeAndFlush(new CloseWebSocketFrame(1011, cause.getMessage() == null ? "error" : cause.getMessage()))
            .addListener(f -> ctx.close());
    }
}

