package com.yupi.yuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;


    @Test
    void testChat() {
        String chatId= UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（编程导航）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChat() {
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮，我想让另一半（编程导航）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void testMysqlMemory() {
        String chatId = "test-001";
        // 第一轮
        String message = "我的名字是什么";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void testBannedWordChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员鱼皮，我想让另一半（劳大）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }


    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？请用中文回复";
        String answer= loveApp.doChatWithRag(message, chatId);
        assertNotNull(answer);
    }

    @Test
    void doChatWithLoverRag() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "请给我推荐对象，我今年 32 岁，男，性取向女，目前在杭州从事互联网产品相关工作，生活节奏相对规律。性格偏理性但不冷漠，做事有计划感，也愿意倾听和沟通。平时喜欢健身、徒步和看电影，周末偶尔会自己做饭，享受简单但有品质的生活。\n" +
                "\n" +
                "在感情中，我更看重真诚和稳定，希望双方能够坦诚交流、互相理解，而不是冷处理问题。期待遇到一位情绪相对稳定、愿意沟通、对未来有基本规划的伴侣，一起把生活过好，而不是只停留在热恋阶段。";
        String answer= loveApp.doChatWithRag(message, chatId);
        assertNotNull(answer);
    }
}