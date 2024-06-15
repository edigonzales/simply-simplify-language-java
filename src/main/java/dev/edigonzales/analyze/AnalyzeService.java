package dev.edigonzales.analyze;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import dev.edigonzales.statistics.StatisticsResponse;
import dev.edigonzales.statistics.StatisticsService;

@Service
public class AnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzeService.class);

    @Value("classpath:/prompts/openai_analysis_es.st")
    private Resource openAiAnalyzePromptES;

    @Value("classpath:/prompts/openai_analysis_ls.st")
    private Resource openAiAnalyzePromptLS;

    @Value("classpath:/prompts/rules_es.txt")
    private Resource rulesES;

    @Value("classpath:/prompts/rules_ls.txt")
    private Resource rulesLS;

    @Value("classpath:/prompts/system_message_es.txt")
    private Resource systemMessageES;

    @Value("classpath:/prompts/system_message_ls.txt")
    private Resource systemMessageLS;

    private StatisticsService statisticsService;
    
    private ChatClient openAiGpt4ChatClient;

    private ChatClient openAiGpt4oChatClient;

    public AnalyzeService(
            StatisticsService statisticsService,
            @Qualifier("gpt-4") ChatClient openAiGpt4ChatClient,
            @Qualifier("gpt-4o") ChatClient openAiGpt4oChatClient
            ) {
        this.statisticsService = statisticsService;
        this.openAiGpt4ChatClient = openAiGpt4ChatClient;
        this.openAiGpt4oChatClient = openAiGpt4oChatClient;
    }
    
    public AnalyzeResponse call(String text, boolean leichteSprache, boolean condenseText, String modelName) throws IOException {
        double sourceScore = statisticsService.getUnderstandability(text);
        String sourceCefrLevel = StatisticsService.getCefrLevel(sourceScore);
        StatisticsResponse textStatisticsSource = new StatisticsResponse(sourceScore, sourceCefrLevel); 

        String systemMessage;
        String prompt;
        String rules;
        if (leichteSprache) {
            prompt = openAiAnalyzePromptLS.getContentAsString(Charset.defaultCharset());
            systemMessage = systemMessageLS.getContentAsString(Charset.defaultCharset());
            rules = rulesLS.getContentAsString(Charset.defaultCharset());
        } else {
            prompt = openAiAnalyzePromptES.getContentAsString(Charset.defaultCharset());
            systemMessage = systemMessageES.getContentAsString(Charset.defaultCharset());
            rules = rulesES.getContentAsString(Charset.defaultCharset());
        }
        
        ChatClient chatClient;
        if (modelName.equalsIgnoreCase("gpt-4")) {
            logger.debug("analyze using GPT-4");
            chatClient = openAiGpt4ChatClient;
       } else {
            logger.debug("analyze using GPT-4o");
            chatClient = openAiGpt4oChatClient;
        }

        logger.debug("asking chat client");
        String response = chatClient.prompt()
                .system(systemMessage)
                .user(u -> {
                    u.text(prompt);
                    u.param("prompt", text);
                    u.param("rules", rules);
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
            return new AnalyzeResponse(targetText, textStatisticsSource);
        } else {
            return new AnalyzeResponse("something went wrong", null);
        }                
    }
    
}
