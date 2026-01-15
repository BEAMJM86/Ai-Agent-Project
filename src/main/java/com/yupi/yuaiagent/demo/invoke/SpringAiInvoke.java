package com.yupi.yuaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
public class SpringAiInvoke implements CommandLineRunner {

    @Resource
    @Qualifier("dashscopeChatModel")  // 明确指定使用 dashscopeChatModel
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage assistantMessage = dashscopeChatModel
                .call(new Prompt("你好，你是谁？"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
