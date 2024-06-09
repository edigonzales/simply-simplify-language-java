package dev.edigonzales.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SimplifyService {
    private static final Logger logger = LoggerFactory.getLogger(SimplifyService.class);

    private ChatClient openAiChatClient;
        
    public SimplifyService(@Qualifier("gpt-4") ChatClient openAiChatClient) {
        this.openAiChatClient = openAiChatClient;
    }

    public void simplifyText() {
        System.err.println("simplifying...");
        
        System.err.println(openAiChatClient);
        
        
        String response = openAiChatClient.prompt()
                .user("Tell me a dad joke.")
                .call()
                .content();
        
        System.out.println(response);
        
//        try {
//            Thread.sleep(20_000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        
        System.err.println("simplifying end");
    }
}
