package com.yupi.yuaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 初始化基于内存的向量数据库Bean
 */
@Configuration
public class HealthAppVectorStoreConfig {

    @Resource
    private HealthAppDocumentLoader documentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore healthAppVectorStore(EmbeddingModel ollamaEmbeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(ollamaEmbeddingModel).build();
        //从资源路径加载的文档信息
        List<Document> documents = documentLoader.loadMarkdowns();

//        //自主切分文档，不建议用这种切词器，容易破坏语义完整性
//        List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(documents);

//        //自动补充关键词信息（这里打开会导致耗时增加）
//        List<Document> enrichDocuments = myKeywordEnricher.enrichDocuments(documents);
//        //将文档信息通过embedding处理保存到向量数据库
//        simpleVectorStore.doAdd(enrichDocuments);

        //将文档信息通过embedding处理保存到向量数据库(基于内存)
        simpleVectorStore.doAdd(documents);
        return simpleVectorStore;
    }
}
