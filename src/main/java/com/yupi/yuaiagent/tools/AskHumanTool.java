package com.yupi.yuaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.util.Scanner;

/**
 * AskHuman 工具示例
 * 模拟在多智能体框架中向人类请求输入的功能
 */
@Component
public class AskHumanTool {

    /**
     * 向人类提问并获取回答
     *
     * @param question 要询问的问题
     * @return 用户输入的回答
     */
    @Tool(description = """
            Use this tool to ask human for help..""")
    public String askHuman(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("问题不能为空");
        }

        System.out.println("🤖 AI: 我需要你的帮助，请回答以下问题：");
        System.out.println("❓ " + question);

        Scanner scanner = new Scanner(System.in);
        String answer = null;

        // 循环直到用户输入有效内容
        while (answer == null || answer.trim().isEmpty()) {
            System.out.print("👤 你的回答: ");
            if (scanner.hasNextLine()) {
                answer = scanner.nextLine().trim();
                if (answer.isEmpty()) {
                    System.out.println("⚠️ 回答不能为空，请重新输入。");
                }
            }
        }

        return answer;
    }
}
