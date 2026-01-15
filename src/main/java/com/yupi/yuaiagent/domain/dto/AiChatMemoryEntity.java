package com.yupi.yuaiagent.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatMemoryEntity {
    private Long id;
    private String conversationId;
    private byte[] messagesBlob;
    private Integer msgCount;
    private LocalDateTime updatedAt;
}
