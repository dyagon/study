package demo.im.client;

import demo.im.protocol.ImMessage.ChatResponse;
import demo.im.protocol.ImMessage.ChatMessage;
import demo.im.protocol.ImMessage.CommandType;
import demo.im.protocol.ImMessage.LoginResponse;
import demo.im.protocol.ImMessage.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ImClientHandler extends SimpleChannelInboundHandler<Packet> {

    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
        if (msg.getCommand() == CommandType.LOGIN_RESPONSE) {
            handleLoginResponse(msg.getLoginResponse());
        } else if (msg.getCommand() == CommandType.CHAT_RESPONSE) {
            handleChatResponse(msg.getChatResponse());
        } else if (msg.getCommand() == CommandType.CHAT_MESSAGE) {
            handleChatMessage(msg.getChatMessage());
        }
    }

    private void handleChatResponse(ChatResponse res) {
        if (!res.getSuccess()) {
            log.info("Chat failed: {}", res.getMessage());
        }
    }

    private void handleChatMessage(ChatMessage msg) {
        log.info("[{}]: {}", msg.getFromUserId(), msg.getContent());
        System.out.print("> "); 
    }

    private void handleLoginResponse(LoginResponse res) {
        if (res.getSuccess()) {
            log.info("Login successful: {}", res.getMessage());
        } else {
            log.info("Login failed: {}", res.getMessage());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
