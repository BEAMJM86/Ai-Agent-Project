package com.yupi.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;

import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档元信息增强器：利用生成式 AI 模型从文档内容中提取关键词并添加为元数据。
 */
@Component
class MyKeywordEnricher {

    @Resource
    private ChatModel ollamaChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher =new KeywordMetadataEnricher(ollamaChatModel,5);
        return enricher.apply(documents);
    }
}