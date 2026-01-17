package com.yupi.yuaiagent.chatmemory;

import com.yupi.yuaiagent.mapper.AiChatMemoryMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory(AiChatMemoryMapper mapper, @Value("${chat.memory.type:memory}") String memoryType) {
        // 根据配置选择存储方式
        if ("mysql".equalsIgnoreCase(memoryType)) {
            return new MysqlKryoChatMemory(mapper);
        } else if ("file".equalsIgnoreCase(memoryType)) {
            String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
            return new FileBasedChatMemory(fileDir);
        } else {
            return new InMemoryChatMemory();  // 默认使用内存存储
        }
    }
}
