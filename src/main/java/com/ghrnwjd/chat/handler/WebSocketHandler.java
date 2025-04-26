package com.ghrnwjd.chat.handler;

import com.ghrnwjd.chat.model.ChatMessage;
import com.ghrnwjd.chat.repository.ChatRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private final ChatRepository chatRepository;

    // ConcurrentHashMap 이란?

    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS =
            new ConcurrentHashMap<String, WebSocketSession>();

    public WebSocketHandler(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CLIENTS.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(session.getId())
                .messageType(ChatMessage.MessageType.QUIT)
                .build();

        chatRepository.save(chatMessage);

        CLIENTS.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();

        ChatMessage chatMessage = ChatMessage.builder()
                .message(message.toString())
                .messageType(ChatMessage.MessageType.WRITE)
                .sender(sessionId)
                .build();

        chatRepository.save(chatMessage);

        CLIENTS.entrySet().forEach(client -> {
            if(!client.getKey().equals(sessionId)) {
                try {
                    client.getValue().sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
