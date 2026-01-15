package com.yupi.yuaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;

/**
 * 初始化基于内存的向量数据库Bean
 */
@Configuration
public class LoveAppVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader documentLoader;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel ollamaEmbeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(ollamaEmbeddingModel).build();
        //从资源路径加载的文档信息
        List<Document> documents = documentLoader.loadMarkdowns();
        //将文档信息通过embedding处理保存到向量数据库
        simpleVectorStore.doAdd(documents);
        return simpleVectorStore;
    }
}
