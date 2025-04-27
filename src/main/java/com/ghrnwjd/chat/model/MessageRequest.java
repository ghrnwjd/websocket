package com.ghrnwjd.chat.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("content")
    private String content;

    private String sender; // 추가 필드 (선택적)

}
