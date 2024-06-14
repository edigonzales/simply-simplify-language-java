package dev.edigonzales.analyze;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import dev.edigonzales.simplify.SimplifyService;
import dev.edigonzales.statistics.StatisticsResponse;
import dev.edigonzales.statistics.StatisticsService;

public class AnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(SimplifyService.class);

    @Value("classpath:/prompts/openai_es.st")
    private Resource openAiAnalyzePromptES;

    @Value("classpath:/prompts/openai_ls.st")
    private Resource openAiAnalyzePromptLS;

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
    
    public void call(String text, boolean leichteSprache, boolean condenseText, String modelName) throws IOException {
        double sourceScore = statisticsService.getUnderstandability(text);
        String sourceCefrLevel = StatisticsService.getCefrLevel(sourceScore);
        StatisticsResponse textStatisticsSource = new StatisticsResponse(sourceScore, sourceCefrLevel); 

        
    }
    
}
