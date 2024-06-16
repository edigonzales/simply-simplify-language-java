package dev.edigonzales.simplify;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import dev.edigonzales.statistics.StatisticsResponse;
import dev.edigonzales.statistics.StatisticsService;

import java.io.IOException;
import java.nio.charset.Charset;
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

    private StatisticsService statisticsService;
    
    private ChatClient openAiGpt4ChatClient;

    private ChatClient openAiGpt4oChatClient;

    private ChatClient ollamaLlama3ChatClient;

    public SimplifyService(
            StatisticsService statisticsService,
            @Qualifier("gpt-4") ChatClient openAiGpt4ChatClient,
            @Qualifier("gpt-4o") ChatClient openAiGpt4oChatClient,
            @Qualifier("llama3") ChatClient ollamaLlama3ChatClient
            ) {
        this.statisticsService = statisticsService;
        this.openAiGpt4ChatClient = openAiGpt4ChatClient;
        this.openAiGpt4oChatClient = openAiGpt4oChatClient;
        this.ollamaLlama3ChatClient = ollamaLlama3ChatClient;
    }

    public SimplifyResponse call(String text, boolean leichteSprache, boolean condenseText, String modelName) throws IOException {
        double sourceScore = statisticsService.getUnderstandability(text);
        String sourceCefrLevel = StatisticsService.getCefrLevel(sourceScore);
        StatisticsResponse textStatisticsSource = new StatisticsResponse(sourceScore, sourceCefrLevel); 
        
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
            logger.debug("simplify using GPT-4");
            chatClient = openAiGpt4ChatClient;
        } else if (modelName.equalsIgnoreCase("gpt-4o")) {
            logger.debug("simplify using GPT-4o");
            chatClient = openAiGpt4oChatClient;
        } else {
            logger.debug("simplify using Llama3");
            chatClient= ollamaLlama3ChatClient;
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
        logger.debug(response);
        
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
            String targetText = matcher.group(1).trim();
            double targetScore = statisticsService.getUnderstandability(targetText);
            String targetCefrLevel = StatisticsService.getCefrLevel(targetScore);
            StatisticsResponse textStatisticsTarget = new StatisticsResponse(targetScore, targetCefrLevel);
            return new SimplifyResponse(targetText, textStatisticsSource, textStatisticsTarget);
        } else {
            return new SimplifyResponse("something went wrong", null, null);
        }        
    }
}
