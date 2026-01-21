# Ai-Agent-Project
本项目是基于 Spring Boot 3 + Spring AI + RAG + Tool Calling + MCP 的企业级 AI 健康大师智能体，为用户提供健康指导服务。
支持多轮对话、记忆持久化、RAG 知识库检索等能力，并且基于 ReAct 模式，能够自主思考并调用工具来完成复杂任务，
比如利用网页搜索、资源下载和 PDF 生成工具制定完整的出行计划并生成文档。



MySQL 存 Kryo 二进制

```
CREATE TABLE ai_chat_memory (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  conversation_id VARCHAR(128) NOT NULL COMMENT '会话ID',
  messages_blob LONGBLOB NOT NULL COMMENT 'Kryo序列化后的 List<Message>',
  msg_count INT NOT NULL DEFAULT 0 COMMENT '消息条数',
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
    ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记忆';
```



PGvector建表

可以手工创建：https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html

```
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE IF NOT EXISTS vector_store (
id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
content text,
metadata json,
-- ollama的nomic-embed-text嵌入模型的维度为768
embedding vector(768)
);
CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);
```

