package com.ghrnwjd.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghrnwjd.chat.model.ChatRoom;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    // [변경] 방 이름을 키로 사용
    private Map<String, ChatRoom> chatRooms = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper;
    @PostConstruct
    private void init() {
        objectMapper = new ObjectMapper(); // ObjectMapper 초기화 추가
    }

    public ChatRoom enterChatRoom(String roomName) {
        return chatRooms.computeIfAbsent(roomName, name ->
                ChatRoom.builder()
                        .roomId(UUID.randomUUID().toString())
                        .name(name)
                        .build()
        );
    }
}
