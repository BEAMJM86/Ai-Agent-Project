package com.yupi.yuaiagent.app;


import com.yupi.yuaiagent.advisor.BannedWordsAdvisor;
import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.rag.QueryRewriter;
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
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class HealthApp {

    private final ChatClient chatClient;

    private final SystemPromptTemplate systemPromptTemplate;

    /**
     * 初始化AI chatClient
     * @param ollamaChatModel
     */

    public HealthApp(ChatModel ollamaChatModel, ChatMemory chatMemory, @Value("classpath:/prompts/system-message.st")Resource systemResource){
        // 在构造函数中初始化 SystemPromptTemplate
        if (systemResource.exists()) {
            this.systemPromptTemplate = new SystemPromptTemplate(systemResource);
        } else {
            throw new IllegalStateException("System prompt resource not found.");
        }
        String systemPrompt = systemPromptTemplate.render();


        chatClient = ChatClient.builder(ollamaChatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        //自定义日志advisor，按需开启
                        new MyLoggerAdvisor(),
                        //自定义推理增强advisor，按需开启
                        //new ReReadingAdvisor()
                        //自定义违禁词拦截器
                        BannedWordsAdvisor.builder()
                                .addWords(List.of("违禁词1", "违禁词2"))
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

    /**
     * AI 基础对话（支持多轮对话记忆，支持流式调用)
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                //应用RAG知识库问答（基于内存存储）
                .advisors(new QuestionAnswerAdvisor(healthAppvectorStore))
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
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
    @jakarta.annotation.Resource
    @Qualifier("healthAppVectorStore")
    private VectorStore healthAppvectorStore;


//    @jakarta.annotation.Resource
//    private Advisor loveAppRagCloudAdvisor;

//    @Autowired
//    private VectorStore pgVectorVectorStore;

//    @jakarta.annotation.Resource
//    private QueryRewriter queryRewriter;
    /**
     * 和Rag知识库进行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message,String chatId){
        //查询重写
        //String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                //使用改写后的查询
                .user(message)
                //开启多轮对话
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //应用RAG知识库问答（基于内存存储）
                .advisors(new QuestionAnswerAdvisor(healthAppvectorStore))
//                //应用RAG检索增强服务（基于云知识库）
//                .advisors(loveAppRagCloudAdvisor)
                //应用本地RAG检索增强服务基于（本地PGVector向量存储数据库）
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                //应用自定义的RAG检索增强服务（文档查询器+上下文增强）
//                .advisors(
//                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
//                                loveAppvectorStore,"已婚"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;

    }

    //Ai恋爱知识库调用工具能力
    @jakarta.annotation.Resource
    private ToolCallback[] allTools;
    /**
     * AI 恋爱报告功能（支持调用工具)
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId){

        String systemPrompt = systemPromptTemplate.render();
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    /**
     * Ai调用MCP服务
     */
    @jakarta.annotation.Resource
    private ToolCallbackProvider toolCallbackProvider;
    public String doChatWithMcp(String message, String chatId){

        String systemPrompt = systemPromptTemplate.render();
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

}


