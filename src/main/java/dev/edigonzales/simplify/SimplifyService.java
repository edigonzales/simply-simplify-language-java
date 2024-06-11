package dev.edigonzales.simplify;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SimplifyService {
    private static final Logger logger = LoggerFactory.getLogger(SimplifyService.class);

    @Value("classpath:/prompts/system_message_es.txt")
    private Resource systemMessageES;

    @Value("classpath:/prompts/system_message_ls.txt")
    private Resource systemMessageLS;

    @Value("classpath:/prompts/rules_es.txt")
    private Resource rulesES;

    @Value("classpath:/prompts/rules_ls.txt")
    private Resource rulesLS;

    @Value("classpath:/prompts/rewrite_complete.txt")
    private Resource rewriteComplete;

    @Value("classpath:/prompts/rewrite_condensed.txt")
    private Resource rewriteCondensed;

    @Value("classpath:/prompts/openai_es.st")
    private Resource openAiPromptES;

    @Value("classpath:/prompts/openai_ls.st")
    private Resource openAiPromptLS;

    private ChatClient openAiGpt4ChatClient;

    private ChatClient openAiGpt4oChatClient;

    public SimplifyService(
            @Qualifier("gpt-4") ChatClient openAiGpt4ChatClient,
            @Qualifier("gpt-4o") ChatClient openAiGpt4oChatClient
            ) {
        this.openAiGpt4ChatClient = openAiGpt4ChatClient;
        this.openAiGpt4oChatClient = openAiGpt4oChatClient;
    }

    public String call(String text, boolean leichteSprache, boolean condenseText, String modelName) throws IOException {
        String systemMessage;
        String prompt;
        String rules;
        if (leichteSprache) {
            prompt = openAiPromptLS.getContentAsString(Charset.defaultCharset());
            systemMessage = systemMessageLS.getContentAsString(Charset.defaultCharset());
            rules = rulesLS.getContentAsString(Charset.defaultCharset());
        } else {
            prompt = openAiPromptES.getContentAsString(Charset.defaultCharset());
            systemMessage = systemMessageES.getContentAsString(Charset.defaultCharset());
            rules = rulesES.getContentAsString(Charset.defaultCharset());
        }

        String completeness;
        if (condenseText) {
            completeness = rewriteCondensed.getContentAsString(Charset.defaultCharset());
        } else {
            completeness = rewriteComplete.getContentAsString(Charset.defaultCharset());
        }
        
        ChatClient chatClient;
        if (modelName.equalsIgnoreCase("gpt-4")) {
            logger.debug("using GPT-4");
            chatClient = openAiGpt4ChatClient;
        } else {
            logger.debug("using GPT-4o");
            chatClient = openAiGpt4oChatClient;
        }

        logger.debug("asking chat client");
        String response = chatClient.prompt()
                .system(systemMessage)
                .user(u -> {
                    u.text(prompt);
                    u.param("prompt", text);
                    u.param("rules", rules);
                    u.param("completeness", completeness);
                })
                .call()
                .content();
        logger.info(response);
        
        String regex;
        if (leichteSprache) {
            regex = "<leichtesprache>(.*?)</leichtesprache>";
        } else {
            regex = "<einfachesprache>(.*?)</einfachesprache>";
        }
        
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);

        boolean matchFound = matcher.find();
        if (matchFound) {
            return matcher.group(1).trim();
        } else {
            return "something went wrong";
        }        
    }
}
