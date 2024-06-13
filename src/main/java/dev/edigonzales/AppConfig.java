package dev.edigonzales;

import org.languagetool.JLanguageTool;
import org.languagetool.language.SwissGerman;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public JLanguageTool langTool() {
        JLanguageTool langTool = new JLanguageTool(new SwissGerman());
        return langTool;
    }
}
