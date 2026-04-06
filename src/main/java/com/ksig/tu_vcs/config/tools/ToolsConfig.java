package com.ksig.tu_vcs.config.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ToolsConfig {

    @Bean
    public ObjectMapper autoCloseFalseMapper() {
        return JsonMapper.builder()
                .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                .build();
    }
}
