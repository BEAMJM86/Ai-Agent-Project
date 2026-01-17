package com.yupi.yuaiagent.rag;


import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * 创建上下文查询增强器
 */
public class LoveAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance(){

        //这里的{query}和{context}不加上的话会报错：The following placeholders must be present in the prompt template: query,context
        PromptTemplate emptyContexPromptTemplate = new PromptTemplate("""
                    {query}
    
                    {context}
                你应该输出下面的内容：
                抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                有问题可以联系编程导航客服 https://codefather.cn
                """);
        return ContextualQueryAugmenter
                .builder()
                .allowEmptyContext(false)
                .promptTemplate(emptyContexPromptTemplate)
                .build();
    }

}
