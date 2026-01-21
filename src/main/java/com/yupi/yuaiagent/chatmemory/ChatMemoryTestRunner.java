package com.yupi.yuaiagent.chatmemory;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class ChatMemoryTestRunner implements CommandLineRunner {

    @Resource
    private ChatMemory chatMemory;

    @Override
    public void run(String... args) {
        String convId = "test-001";

        // 1) 写入一些消息（模拟历史记忆）
        chatMemory.add(convId, List.of(
                new org.springframework.ai.chat.messages.UserMessage("第一句：我叫张三"),
                new org.springframework.ai.chat.messages.AssistantMessage("好的，记住了：你叫张三")
        ));

        // 2) 读取最后 N 条
        List<org.springframework.ai.chat.messages.Message> last = chatMemory.get(convId);

        System.out.println("=== 从 ChatMemory 读取 ===");
        last.forEach(m -> System.out.println(m.getMessageType() + " -> " + m.getText()));
    }
}
