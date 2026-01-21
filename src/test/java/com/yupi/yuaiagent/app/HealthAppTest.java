package com.yupi.yuaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HealthAppTest {

    @Resource
    private HealthApp healthApp;


    @Test
    void testChat() {
        String chatId= UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员beam";
        String answer = healthApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "最近眼睛很累";
        answer = healthApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的身体状况是什么来着？刚跟你说过，帮我回忆一下";
        answer = healthApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

    }

    @Test
    void doChat() {
    }



    @Test
    void testMysqlMemory() {
        String chatId = "test-001";
        // 第一轮
        String message = "我的名字是什么";
        String answer = healthApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }



    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "我最近身体很不舒服，特别是颈椎，怎么办？";
        String answer= healthApp.doChatWithRag(message, chatId);
        assertNotNull(answer);
    }


    @Test
    void doChatWithImage() {
    }


    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = healthApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点";
        String answer =  healthApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);

        // 测试图片搜索 MCP
//        String message = "帮我用搜索一些哄另一半开心的图片";
//        String answer =  healthApp.doChatWithMcp(message, chatId);
//        Assertions.assertNotNull(answer);

    }

}