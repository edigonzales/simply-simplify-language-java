package dev.edigonzales.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.Log;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.SwissGerman;

import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.de.GermanWordTokenizer;


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
        
        extractTextFeatures(text);
        
        return 1;
    }
    
    /* Extract text features from text.
     */
    private void extractTextFeatures(String text) throws IOException {
//        System.err.println(this.wordScores);
        
//        SentenceModel sentenceModel = new SentenceModel(openNlpSentenceDetectionModel.getFile());
//        SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);
//        
//        String[] sentences = sentenceDetector.sentDetect(text);
//        
//        for (String sentence : sentences) {
//            logger.info(sentence.toString());            
//        }
//        
//        TokenizerModel model = new TokenizerModel(openNlpTokensModel.getFile());
//        TokenizerME tokenizer = new TokenizerME(model);
//        String[] tokens = tokenizer.tokenize(text);
//        
//        for (String token : tokens) {
//            if (Pattern.matches("\\p{Punct}", token) || isNumeric(token)) {
//                continue;
//            }
//            System.err.println(token);
//        }

        
        JLanguageTool langTool = new JLanguageTool(new SwissGerman());

        int docLength = 0;
        int docScores = 0;
        List<AnalyzedSentence> analyzedSentences = langTool.analyzeText(text);
        for (AnalyzedSentence analyzedSentence : analyzedSentences) {
            for (AnalyzedTokenReadings analyzedTokens : analyzedSentence.getTokensWithoutWhitespace()) {
                if (analyzedTokens.getReadings().size() > 0) {
                    String token = analyzedTokens.getReadings().get(0).getToken();
                    if (!Pattern.matches("\\p{Punct}", token) && token.trim().length() > 0) {
                        String lemma = analyzedTokens.getReadings().get(0).getLemma();
                        if (lemma != null) {
                            if (wordScores.containsKey(lemma.toLowerCase())) {
                                docScores += wordScores.get(lemma.toLowerCase());
                            }
                        }
                        docLength++;
                    }
                }
            }
        }
        
        double commonWordScore = docScores / docLength;
        System.err.println(commonWordScore);
        
        
        System.err.println("text: " + text);
        for (AnalyzedSentence analyzedSentence : analyzedSentences) {
            System.out.println(analyzedSentence.getText());
        }
        
        
//        Tokenizer tokenizer = new GermanWordTokenizer();
//        List<String> tokens = tokenizer.tokenize(text);

        // Print the tokens
//        for (String token : tokens) {
//            System.out.println(token);
//        }

        
//        List<String> filteredTokens = tokens.stream()
//                .filter(token -> !token.trim().isEmpty())
//                .filter(token -> !isNumeric(token))
//                .filter(token -> {
//                    if (Pattern.matches("\\p{Punct}", token)) {
//                        return false;
//                    }
//                    return true;
//                })
//                .collect(Collectors.toList());
//
//        
//        for (String token : filteredTokens) {
//            System.out.println(token);
//        }

        

        
//        JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("de"));
//        langTool.tok

        
//        POSModel posModel = new POSModel(openNlpPosModel.getFile());
//        POSTaggerME posTagger = new POSTaggerME(posModel);
        
//        Stream<String> tokensStream = Arrays.stream(tokens).filter(f->{
//            System.err.println(f);
//            return true;
//        }); 
//        logger.info(tokensStream.toString());
//        
//        tokensStream
//            .filter(t -> {
//                System.err.println(t);
//                if (Pattern.matches("\\p{Punct}", t)) {
//                    return false;
//                }
//                return true;
//            })
//            .filter(t -> {
//                System.err.println(t);
//                return true;
//            });


//        for (String token : tokens) {
//            logger.info("{}", Pattern.matches("\\p{Punct}", token));
//
//            
//            logger.info(token.toString());            
//        }



        
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
     * We use this ranking to calculate, how common the vocabulary in the text ist and therefore 
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
    
//    def get_word_scores():
//        """Get commond word scores from the parquet file.
//
//        This is a list of common German words in texts written in Einfache and Leichte Sprache. We use this ranking to calculate, how common the vocabulary in the text ist and therefore how easy the text is to understand.
//
//        We have lemmatized and lower cased the words. Also note that the German `ß` has been replaced with `ss`.
//        """
//        word_scores = pd.read_parquet("word_scores.parq")
//        word_scores = dict(zip(word_scores["lemma"], word_scores["score"]))
//        return word_scores

    
    
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
        // TODO Ist das wirklich nötig? Führt bei mir dazu, dass es einen leeren Satz gibt 
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
