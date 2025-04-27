package com.ghrnwjd.chat.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int messageId;

    @Getter
    public enum MessageType {
        ENTER, WRITE, QUIT
    }

    @Column
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column
    private String roomId;

    @Column
    private String sender;

    @Column
    private String message;

    @Column()
    @CreationTimestamp
    private LocalDateTime timestamp;

    private int readCount; // 내 글을 읽은 사람 확인
}
