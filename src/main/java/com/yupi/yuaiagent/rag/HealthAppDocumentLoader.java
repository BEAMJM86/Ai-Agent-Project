package com.yupi.yuaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
class HealthAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;


    HealthAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载文档，不用经过特殊处理
     * @return
     */
    public List<Document> loadMarkdowns(){
        List<Document> allDocument=new ArrayList<>();

        try {
            // 这里可以修改为你要加载的多个 Markdown 文件的路径模式
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/health/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        //将文件元信息，如文件名，保存，以便可以精确检索
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status",status)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                allDocument.addAll(markdownDocumentReader.get());

            }
        } catch (IOException e) {
            log.error("Markdown文件加载失败！",e);
        }
        return allDocument;
    }
}
