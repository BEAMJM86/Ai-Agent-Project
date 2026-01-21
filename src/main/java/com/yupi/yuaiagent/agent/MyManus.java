package com.yupi.yuaiagent.agent;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;


/**
 * 超级智能体拥有自主规划能力
 */
@Component
public class MyManus extends ToolCallAgent{

    public MyManus(ToolCallback[] allTools, ChatModel ollamaChatModel, ToolCallbackProvider toolCallbackProvider) {
        super(allTools);
        this.setName("MyManus");
//        String SYSTEM_PROMPT = """
//            You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.
//            You have various tools at your disposal that you can call upon to efficiently complete complex requests.
//            """;
        String SYSTEM_PROMPT = """  
            你是MyManus，一个全能的人工智能助手，旨在解决用户交付的任何任务。 
            你拥有多种工具，可以高效完成复杂请求。
            """;
        this.setSystemPrompt(SYSTEM_PROMPT);
//        String NEXT_STEP_PROMPT = """
//            Based on user needs, proactively select the most appropriate tool or combination of tools.
//            For complex tasks, you can break down the problem and use different tools step by step to solve it.
//            After using each tool, clearly explain the execution results and suggest the next steps.
//            If you want to stop the interaction at any point, use the `terminate` tool/function call.
//            """;
        String NEXT_STEP_PROMPT = """  
                根据用户需求，主动选择最合适的工具或工具组合。
                对于复杂的任务，你可以拆解问题，并一步步使用不同的工具来解决。
                使用每个工具后，清晰说明执行结果并建议下一步。
                如果你想在任何时候停止交互，可以使用“doTerminate”工具/函数调用。
            """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                //添加MCP工具
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
        this.setOllamaChatClient(chatClient);
    }
}
