package dev.edigonzales.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.SwissGerman;



@Service
public class AnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzeService.class);

    @Value("classpath:/data/word_scores.parquet")
    private Resource wordScoresParquet;

    @Value("classpath:/data/opennlp-de-ud-gsd-sentence-1.0-1.9.3.bin")
    private Resource openNlpSentenceDetectionModel;

    @Value("classpath:/data/opennlp-de-ud-gsd-tokens-1.0-1.9.3.bin")
    private Resource openNlpTokensModel;

    @Value("classpath:/data/opennlp-de-ud-gsd-pos-1.0-1.9.3.bin")
    private Resource openNlpPosModel;
    
    private HashMap<String,Integer> wordScores = null;
        
    public double getUnderstandability(String text) throws IOException {
        text = punctuateParagraphsAndBulletedLists(text);
        
        Map<String, Double> textStatistics = extractStatistics(text);
        double score = calculateUnderstandability(textStatistics);
        
        return score;
    }
    
    /*
     * Calculate understandability score from text metrics.
     * 
     * We derived this formula from a dataset of legal and administrative texts, as well as 
     * Einfache and Leichte Sprache. We trained a Logistic Regression model to differentiate 
     * between complex and simple texts. From the most significant model coefficients we 
     * devised this formula to estimate a text's understandability.
     * 
     * It is not a perfect measure, but it gives a good indication of how easy a text is to understand.
     * We do not take into account a couple of text features that are relevant to understandability, 
     * such as the use of passive voice, the use of pronouns, or the use of complex sentence 
     * structures. These features are not easily extracted from text and would require a more complex 
     * model to calculate.
     */
    private double calculateUnderstandability(Map<String,Double> statistics) {
        double commonWordScore = statistics.get("common_word_score");
        double readabilityIndex = statistics.get("readability_index");
        double sentenceLengthMean = statistics.get("sentence_length_mean");
        double sentenceLengthStd = statistics.get("sentence_length_std");

        double cws = (commonWordScore - 7.8) / 1.1;
        double rix = (readabilityIndex - 3.9) / 1.7;
        double sls = (sentenceLengthStd - 6.4) / 4.2;
        double slm = (sentenceLengthMean - 11.7) / 3.7;
        cws = 1 - cws;

        double score = ((cws * 0.2 + rix * 0.325 + sls * 0.225 + slm * 0.15) + 1.3) * 3.5;
        logger.debug("score 1: " + score);
        
       // We clip the score to a range of 0 to 20.
       score = 20 - score;
       if (score < 0) {
           score = 0;
       } else if (score > 20) {
           score = 20;
       }
       logger.debug("score 2: " + score);
       return score;
    }
    
    /* Extract statistics from text.
     */
    private Map extractStatistics(String text) throws IOException {
        JLanguageTool langTool = new JLanguageTool(new SwissGerman());

        // Calculate common word score and readability index (rix)
        int docLength = 0;
        int docScores = 0;
        int longWordCount = 0;
        List<Integer> sentencesWordCount = new ArrayList<>();
        List<AnalyzedSentence> analyzedSentences = langTool.analyzeText(text);
        for (AnalyzedSentence analyzedSentence : analyzedSentences) {
            int sentenceWordCount = 0;
            for (AnalyzedTokenReadings analyzedTokens : analyzedSentence.getTokensWithoutWhitespace()) {
                if (analyzedTokens.getReadings().size() > 0) {
                    String token = analyzedTokens.getReadings().get(0).getToken();
                    if (!Pattern.matches("\\p{Punct}", token) && token.trim().length() > 0) {
                        String lemma = analyzedTokens.getReadings().get(0).getLemma();
                        if (lemma != null) {
                            if (wordScores.containsKey(lemma.toLowerCase())) {
                                // Unterschied zu ZH-Lösung: z.B. wird zweite nicht gefunden, weil das Lemma weiterhin zweite ist.
                                // Und im Parquet-File "zweiter" steht.
                                docScores += wordScores.get(lemma.toLowerCase());
                            }
                            // Wenn hier die langen "Wörter" gezählt werden, passt es besser mit dem RIX der ZH-Lösung zusammen
                            // (also rein durch ausprobieren). 
                            if(token.length() >= 6) {
                                longWordCount++;                                
                            }
                        }
                        docLength++;
                        sentenceWordCount++;
                        // Tendenziell höher als die ZH-Lösung
//                        if(token.length() >= 6) {
//                            longWordCount++;
//                        }
                    }
                }
            }
            sentencesWordCount.add(sentenceWordCount);
        }
        System.out.println("docLength: " + docLength);        
        System.out.println("docScores: " + docScores);
        System.out.println("sentencesWordCount: " + sentencesWordCount);
        System.out.println("longWordCount: " + longWordCount);
        System.out.println("analyzedSentences.size(): " + analyzedSentences.size());
        
        double commonWordScore = (docScores / docLength) / 1000.0;
        double readabilityIndex = longWordCount / (double) analyzedSentences.size();
        System.err.println("commonWordScore: " + commonWordScore);
        System.err.println("readabilityIndex: " + readabilityIndex);
        
        // Calculate the standard deviation and mean of the sentences
        double sentenceLengthMean = sentencesWordCount.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = sentencesWordCount.stream().mapToDouble(length -> Math.pow(length - sentenceLengthMean, 2)).average().orElse(0.0);
        double sentenceLengthStd = Math.sqrt(variance);

        System.err.println("sentenceLengthStd: " + sentenceLengthStd);
        System.err.println("sentenceLengthMean: " + sentenceLengthMean);
        
        HashMap<String,Double> statistics = new HashMap<>();
        statistics.put("common_word_score", commonWordScore);
        statistics.put("readability_index", readabilityIndex);
        statistics.put("sentence_length_mean", sentenceLengthMean);
        statistics.put("sentence_length_std", sentenceLengthStd);
        
        return statistics;
    }
    
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /*
     * Get common word scores from the parquet file.
     * This is a list of common German words in texts written in Einfache and Leichte Sprache. 
     * We use this ranking to calculate, how common the vocabulary in the text is and therefore 
     * how easy the text is to understand. We have lemmatized and lower cased the words.
     * Also note that the German `ß` has been replaced with `ss`.
     */
    @PostConstruct
    private void getWordScores() throws IOException {
        wordScores = new HashMap<>();
        String file = wordScoresParquet.getFile().getAbsolutePath();
        Path path = new Path(file);
        InputFile inputFile = HadoopInputFile.fromPath(path, new Configuration());
        
        try(ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(inputFile).build()) {
            GenericRecord next = null;
            while((next = reader.read()) != null) {
                wordScores.put(next.get("lemma").toString(), Integer.valueOf(next.get("score").toString()));
            }
        }
    }
        
    /* 
     * Add a dot to lines that do not end with a dot and do some additional clean up to correctly 
     * calculate the understandability score.
     * 
     * Texts in Einfache and Leichte Sprache often do not end sentences with a dot. 
     * This function adds a dot to lines that do not end with a dot. It also 
     * removes bullet points and hyphens from the beginning of lines and removes 
     * all line breaks and multiple spaces. This is necessary to correctly calculate 
     * the understandability score.
     */
    private String punctuateParagraphsAndBulletedLists(String text) {
        // TODO Ist das wirklich nötig bei mir? Führt bei mir dazu, dass es einen leeren Satz gibt 
        // und ein zusätzlicher Punkt.
        // Add a space to the end of the text to properly process the last sentence.
        // text = text + " ";

        // This regex pattern matches lines that do not end with a dot and are not empty.
        String pattern = "^(?!.*\\.$).+";
        
        // Compile the pattern with the MULTILINE flag
        Pattern regex = Pattern.compile(pattern, Pattern.MULTILINE);
        Matcher matcher = regex.matcher(text);

        // Replace lines not ending with a dot with the same line plus a dot.
        StringBuffer newText = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(newText, matcher.group() + ".");
        }
        matcher.appendTail(newText);

        // Convert the StringBuffer back to a String
        String result = newText.toString();

        // Remove bullet points and hyphens from the beginning of lines.
        pattern = "^[\\s]*[-•]";
        regex = Pattern.compile(pattern, Pattern.MULTILINE);
        matcher = regex.matcher(result);
        result = matcher.replaceAll("");

        // Remove all line breaks and multiple spaces.
        result = result.replace("\n", " ");
        pattern = "\\s+";
        regex = Pattern.compile(pattern);
        matcher = regex.matcher(result);
        result = matcher.replaceAll(" ");

        logger.debug("text after regex hell: " + result);
        return result.strip();        
    }
}
