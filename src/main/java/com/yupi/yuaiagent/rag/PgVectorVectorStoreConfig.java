package com.yupi.yuaiagent.rag;

import cn.hutool.core.collection.CollUtil;
import com.yupi.yuaiagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

//@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Autowired
    @Qualifier("postgresqlDataSource")
    private DataSource postgresqlDataSource;

    // 使用@Qualifier来明确注入指定的EmbeddingModel Bean
    @Bean
    public VectorStore pgVectorVectorStore(EmbeddingModel ollamaEmbeddingModel) {
        JdbcTemplate postgresqlJdbcTemplate = new JdbcTemplate(postgresqlDataSource);

        PgVectorStore vectorStore = PgVectorStore.builder(postgresqlJdbcTemplate, ollamaEmbeddingModel)
                .dimensions(768)                    // Optional: defaults to model dimensions or 1536
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)  // Optional: defaults to COSINE_DISTANCE
                .indexType(PgVectorStore.PgIndexType.HNSW)  // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();

        //加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        // 不添加内容重复的document
        if (CollUtil.isNotEmpty(documents)) {
            String joinedContent = documents.stream()
                    .map(Document::getText)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("','", "'", "'"));
            List<Map<String, Object>> result = postgresqlJdbcTemplate
                    .query("select * from vector_store where content in (" + joinedContent + ")", new ColumnMapRowMapper());
            List similarityTexts = result.stream()
                    .filter(map -> Objects.nonNull(map.get("content")))
                    .map(map -> map.get("content").toString())
                    .toList();
            documents.removeIf(document -> similarityTexts.contains(document.getText()));
        }
        if (CollUtil.isNotEmpty(documents)) {
            vectorStore.add(documents);
        }
        return vectorStore;
    }
}
