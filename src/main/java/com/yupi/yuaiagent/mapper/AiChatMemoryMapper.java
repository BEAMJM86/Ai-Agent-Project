package com.yupi.yuaiagent.mapper;

import com.yupi.yuaiagent.domain.dto.AiChatMemoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiChatMemoryMapper {

    AiChatMemoryEntity selectByConversationId(@Param("conversationId") String conversationId);

    int upsert(@Param("conversationId") String conversationId,
               @Param("messagesBlob") byte[] messagesBlob,
               @Param("msgCount") int msgCount);

    int deleteByConversationId(@Param("conversationId") String conversationId);
}
