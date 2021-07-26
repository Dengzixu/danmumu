package net.dengzixu.danmumu.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.dengzixu.danmumu.filter.FaceCacheFilter;
import net.dengzixu.java.DanmuResolver;
import net.dengzixu.java.listener.Listener;
import net.dengzixu.java.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;


@ServerEndpoint("/")
@Component
public class WebsocketServer implements Listener {
    private final Logger logger = LoggerFactory.getLogger(WebsocketServer.class);

    private Session session;

    private static final CopyOnWriteArraySet<WebsocketServer> webSocketServers = new CopyOnWriteArraySet<>();

    private final DanmuResolver danmuResolver = DanmuResolver.getInstance(14047);

    @OnOpen
    public void onOpen(Session session) {
        webSocketServers.add(this);

        // 测试一下
        danmuResolver.addListener(this).addFilter(new FaceCacheFilter());

        this.session = session;

        logger.info("{} 连接成功", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        webSocketServers.remove(this);
        danmuResolver.removeListener(this);

        logger.info("{} 断开链接", session.getId());
    }

    private void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        String stringMessage = null;

        try {
            stringMessage = new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        this.sendMessage(stringMessage);
    }
}
