package dev.edigonzales;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String openAiModel;

    @Bean("gpt-4")
    public ChatModel openAiChatModel() {
        var openAiApi = new OpenAiApi(openAiApiKey);        
        var openAiChatOptions = OpenAiChatOptions.builder()
                    .withModel("gpt-4")
                .build();
        var chatModel = new OpenAiChatModel(openAiApi, openAiChatOptions);
        return chatModel;   
    }
    
}
