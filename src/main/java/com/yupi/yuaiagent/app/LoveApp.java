package com.yupi.yuaiagent.app;


import com.yupi.yuaiagent.advisor.BannedWordsAdvisor;
import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;

import jakarta.annotation.Resources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;


import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private final SystemPromptTemplate systemPromptTemplate;

    /**
     * 初始化AI chatClient
     * @param ollamaChatModel
     */

    public LoveApp(ChatModel ollamaChatModel, ChatMemory chatMemory, Resource systemResource){
        // 在构造函数中初始化 SystemPromptTemplate
        if (systemResource.exists()) {
            this.systemPromptTemplate = new SystemPromptTemplate(systemResource);
        } else {
            throw new IllegalStateException("System prompt resource not found.");
        }
        String systemPrompt = systemPromptTemplate.render();
        chatClient = ChatClient.builder(ollamaChatModel)
                //.defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志advisor，按需开启
                        new MyLoggerAdvisor(),
                        //自定义推理增强advisor，按需开启
                        //new ReReadingAdvisor()
                        //自定义违禁词拦截器
                        BannedWordsAdvisor.builder()
                                .addWords(List.of("科比", "劳大"))
                                .mode(BannedWordsAdvisor.Mode.MASK)
                                .order(1) // 越小越先执行，尽量早拦截
                                .build()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆)
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        //log.info("chatId:{},message:{},response:{}",chatId,message,content);
        return content;
    }


    record LoveReport(String title, List<String>suggestions) {

    }

    /**
     * AI 恋爱报告功能（支持结构化输出)
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId){

        String systemPrompt = systemPromptTemplate.render();
        LoveReport loveReport = chatClient
                .prompt()
                .system( systemPrompt+ "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

    // ✅ 新增：图片 + 文本 多模态
    public String doChatWithImage(String message, MultipartFile image, String chatId) throws IOException {
        // 获取图片的字节数据
        byte[] bytes = image.getBytes();

        // 获取图片的 MIME 类型
        String mimeType = image.getContentType();  // 例如： "image/png" 或 "image/jpeg"

        // 将字节数组包装成 ByteArrayResource
        ByteArrayResource resource = new ByteArrayResource(bytes);
        // 创建一个 ByteArrayResource 包装 byte[] 数据


        // 创建 Media 对象，MimeType 和 ByteArrayResource
        Media media;
        if (mimeType != null) {
            media = new Media(MimeType.valueOf(mimeType), resource);
        } else {
            media = null;
        }

        // 创建 chatResponse 请求
        ChatResponse chatResponse = chatClient
                .prompt()
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .user(u -> u.text(message)
                        .media(media)  // 使用 Media 对象
                )
                .call()
                .chatResponse();

        // 返回处理结果
        return chatResponse.getResult().getOutput().getText();
    }


    //AI数据库知识问答功能
    @Autowired
    @Qualifier("loveAppVectorStore")
    private VectorStore loveAppvectorStore;


    @jakarta.annotation.Resource
    private Advisor loveAppRagCloudAdvisor;

    @Autowired
    private VectorStore pgVectorVectorStore;
    /**
     * 和Rag知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message,String chatId){
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                //开启多轮对话
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//                //开启日志
//                .advisors(new MyLoggerAdvisor())
//                //应用简单的官方RAG知识库问答（基于内存存储）
//                .advisors(new QuestionAnswerAdvisor(loveAppvectorStore))
//                //应用RAG检索增强服务（基于云知识库）
//                .advisors(loveAppRagCloudAdvisor)
                //应用本地RAG检索增强服务基于（本地PGVector向量存储数据库）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;

    }


}


