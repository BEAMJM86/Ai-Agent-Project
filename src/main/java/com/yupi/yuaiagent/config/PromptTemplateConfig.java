package com.yupi.yuaiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class PromptTemplateConfig {

    // 定义一个 @Bean 来提供系统提示模板资源
    @Bean
    public Resource systemResource() {
        return new ClassPathResource("/prompts/system-message.st");  // 定义资源路径
    }
}
