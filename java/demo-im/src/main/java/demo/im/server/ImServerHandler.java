package demo.im.server;

import demo.im.protocol.ImMessage.ChatRequest;
import demo.im.protocol.ImMessage.ChatResponse;
import demo.im.protocol.ImMessage.ChatMessage;
import demo.im.protocol.ImMessage.CommandType;
import demo.im.protocol.ImMessage.LoginRequest;
import demo.im.protocol.ImMessage.LogoutRequest;
import demo.im.protocol.ImMessage.LoginResponse;
import demo.im.protocol.ImMessage.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ImServerHandler extends SimpleChannelInboundHandler<Packet> {
    private final SessionManager sessionManager;
    public static final AttributeKey<String> USER_ID_KEY = AttributeKey.valueOf("userId");

    public ImServerHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
        if (msg.getCommand() == CommandType.LOGIN_REQUEST) {
            handleLoginRequest(ctx, msg.getLoginRequest());
        } else if (msg.getCommand() == CommandType.CHAT_REQUEST) {
            handleChatRequest(ctx, msg.getChatRequest());
        } else if (msg.getCommand() == CommandType.LOGOUT_REQUEST) {
            handleLogoutRequest(ctx, msg.getLogoutRequest());
        }
    }

    private void handleLogoutRequest(ChannelHandlerContext ctx, LogoutRequest req) {
        String userId = ctx.channel().attr(USER_ID_KEY).get();
        log.info("Handling logout for user: {}, request: {}", userId, req);
        if (userId != null) {
            sessionManager.unbind(userId);
            ctx.channel().attr(USER_ID_KEY).set(null);
            log.info("User logged out: {}", userId);
            ctx.close();
        }
    }

    private void handleChatRequest(ChannelHandlerContext ctx, ChatRequest req) {
        String fromUserId = ctx.channel().attr(USER_ID_KEY).get();
        if (fromUserId == null) {
            sendChatResponse(ctx, false, "Not logged in");
            return;
        }

        String toUserId = req.getToUserId();
        io.netty.channel.Channel toChannel = sessionManager.getSession(toUserId);

        if (toChannel != null && toChannel.isActive()) {
            ChatMessage chatMsg = ChatMessage.newBuilder()
                    .setFromUserId(fromUserId)
                    .setContent(req.getContent())
                    .build();

            Packet packet = Packet.newBuilder()
                    .setCommand(CommandType.CHAT_MESSAGE)
                    .setChatMessage(chatMsg)
                    .build();

            toChannel.writeAndFlush(packet);
            sendChatResponse(ctx, true, "Sent");
        } else {
            sendChatResponse(ctx, false, "User " + toUserId + " is offline");
        }
    }

    private void sendChatResponse(ChannelHandlerContext ctx, boolean success, String message) {
        ChatResponse response = ChatResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();

        Packet packet = Packet.newBuilder()
                .setCommand(CommandType.CHAT_RESPONSE)
                .setChatResponse(response)
                .build();

        ctx.writeAndFlush(packet);
    }

    private void handleLoginRequest(ChannelHandlerContext ctx, LoginRequest req) {
        String userId = req.getUserId();
        if (userId.isEmpty()) {
            sendLoginResponse(ctx, false, "Username cannot be empty");
            return;
        }

        if (sessionManager.getSession(userId) != null) {
            sendLoginResponse(ctx, false, "User already logged in");
            return;
        }

        sessionManager.bind(userId, ctx.channel());
        ctx.channel().attr(USER_ID_KEY).set(userId);

        sendLoginResponse(ctx, true, "Login successful");
        log.info("User logged in: {}", userId);
    }

    private void sendLoginResponse(ChannelHandlerContext ctx, boolean success, String message) {
        LoginResponse response = LoginResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();

        Packet packet = Packet.newBuilder()
                .setCommand(CommandType.LOGIN_RESPONSE)
                .setLoginResponse(response)
                .build();

        ctx.writeAndFlush(packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String userId = ctx.channel().attr(USER_ID_KEY).get();
        if (userId != null) {
            sessionManager.unbind(userId);
            log.info("User disconnected: {}", userId);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
