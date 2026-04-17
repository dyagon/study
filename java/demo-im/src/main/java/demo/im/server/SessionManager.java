package demo.im.server;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, Channel> userSessions = new ConcurrentHashMap<>();

    public void bind(String userId, Channel channel) {
        userSessions.put(userId, channel);
    }

    public void unbind(String userId) {
        userSessions.remove(userId);
    }

    public Channel getSession(String userId) {
        return userSessions.get(userId);
    }
}
