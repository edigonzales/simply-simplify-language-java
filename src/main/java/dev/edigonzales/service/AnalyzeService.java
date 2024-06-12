package dev.edigonzales.service;

import java.io.IOException;
import java.util.HashMap;
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;


@Service
public class AnalyzeService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzeService.class);

    @Value("classpath:/data/word_scores.parquet")
    private Resource wordScoresParquet;

    private HashMap<String,Integer> wordScores = null;;
        
    public double getUnderstandability(String text) throws IOException {
        text = punctuateParagraphsAndBulletedLists(text);
        
        extractTextFeatures(text);
        
        return 1;
    }
    
    /* Extract text features from text.
     */
    private void extractTextFeatures(String text) throws IOException {
        System.err.println(this.wordScores);
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
        // Add a space to the end of the text to properly process the last sentence.
        text = text + " ";

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
