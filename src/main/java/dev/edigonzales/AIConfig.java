package dev.edigonzales;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
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

    @Value("${app.ollama.host}")
    private String ollamaHost;

    @Value("${app.ollama.port}")
    private String ollamaPort;
    
    @Bean("gpt-4")
    public ChatClient openAiGpt4ChatClient() {
        var openAiApi = new OpenAiApi(openAiApiKey);        
        var openAiChatOptions = OpenAiChatOptions.builder()
                    .withModel(OpenAiApi.ChatModel.GPT_4)
                .build();
        var chatModel = new OpenAiChatModel(openAiApi, openAiChatOptions);
        var client = ChatClient.builder(chatModel).build();
                
        return client;   
    }

    @Bean("gpt-4o")
    public ChatClient openAiGpt4oChatClient() {
        var openAiApi = new OpenAiApi(openAiApiKey);        
        var openAiChatOptions = OpenAiChatOptions.builder()
                    .withModel(OpenAiApi.ChatModel.GPT_4_O)
                .build();
        var chatModel = new OpenAiChatModel(openAiApi, openAiChatOptions);
        var client = ChatClient.builder(chatModel).build();
                
        return client;   
    }
    
    @Bean("llama3")
    public ChatClient ollamaLlama3ChatClient() {
        System.err.println("ollamaPort: " + ollamaPort);
//        var ollamaApi = new OllamaApi(); // hier die nicht default url
        var ollamaApi = new OllamaApi(ollamaHost+":"+ollamaPort);

        var chatModel = new OllamaChatModel(ollamaApi,
                OllamaOptions.create()
                    .withModel(OllamaModel.LLAMA3.getModelName()));
        var client = ChatClient.builder(chatModel).build();
                
        return client;   
    }
}
