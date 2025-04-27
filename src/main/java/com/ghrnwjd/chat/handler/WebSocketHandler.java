package com.ghrnwjd.chat.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghrnwjd.chat.model.ChatMessage;
import com.ghrnwjd.chat.model.MessageRequest;
import com.ghrnwjd.chat.repository.ChatRepository;
import com.ghrnwjd.chat.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatRepository chatRepository;

    // Key: sessionId, Value: roomId 집합
    private static final ConcurrentHashMap<String, Set<String>> SESSION_ROOMS =
            new ConcurrentHashMap<>();

    // Key: roomId, Value: 해당 방의 세션들
    private static final ConcurrentHashMap<String, Set<WebSocketSession>> ROOM_SESSIONS =
            new ConcurrentHashMap<>();

    public WebSocketHandler(ChatService chatService, ChatRepository chatRepository) {
        this.chatService = chatService;
        this.chatRepository = chatRepository;
    }

    private String getRoomNameFromURI(URI uri) {
        String query = uri.getQuery();
        return query.split("=")[1]; // 예: ws://url?roomName=방이름
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = getRoomNameFromURI(session.getUri()); // URI에서 방 이름 추출
        String sessionId = session.getId();
        // 세션 ↔ 방 매핑
        SESSION_ROOMS.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                .add(roomId);

        // 방 ↔ 세션 매핑
        ROOM_SESSIONS.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        chatRepository.save(ChatMessage.builder()
                        .roomId(roomId)
                        .sender(sessionId)
                        .message("["+sessionId+"] 님이 입장하였습니다.")
                        .messageType(ChatMessage.MessageType.ENTER)
                        .build());

        chatService.enterChatRoom(roomId); // 방 이름 기준 생성
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        Set<String> joinedRooms = SESSION_ROOMS.remove(sessionId);

        if (joinedRooms != null) {
            joinedRooms.forEach(roomId -> {
                // 1. 방에서 세션 제거
                ROOM_SESSIONS.getOrDefault(roomId, Collections.emptySet())
                        .remove(session);

                // 2. DB에 퇴장 기록 저장 (방별로 저장)
                chatRepository.save(ChatMessage.builder()
                        .messageType(ChatMessage.MessageType.QUIT)
                        .message("["+sessionId+"] 님이 퇴장하였습니다.")
                        .sender(sessionId)
                        .roomId(roomId) // ✅ 방마다 별도 기록
                        .build());
            });
        }
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 1. 메시지에서 roomId 파싱
            String payload = message.getPayload();
            String targetRoomId = getRoomNameFromURI(session.getUri());

            // 2. DB 저장
            chatRepository.save(ChatMessage.builder()
                    .messageType(ChatMessage.MessageType.WRITE)
                    .sender(session.getId())
                    .readCount(ROOM_SESSIONS.get(targetRoomId).size() - 1) // 모든 사용자 중에서 자기 자신을 뺀
                    .roomId(targetRoomId) // ✅ 메시지에 명시된 roomId 사용
                    .message(payload) // 실제 메시지 내용 포함
                    .timestamp(LocalDateTime.now()) // 추가 권장
                    .build());

            // 3. 메시지 브로드캐스트
            ROOM_SESSIONS.getOrDefault(targetRoomId, Collections.emptySet())
                    .forEach(s -> {
                        if (s.isOpen() && !s.equals(session)) {
                            try {
                                s.sendMessage(message);
                            } catch (IOException e) {
                                log.error("메시지 전송 실패: {}", e.getMessage());
                            }
                        }
                    });
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", e.getMessage());
        }
    }

    private MessageRequest parseMessage(String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("payload = " + payload);
            MessageRequest request = objectMapper.readValue(payload, MessageRequest.class);
            return request;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("메시지 파싱 실패");
        }
    }
}
