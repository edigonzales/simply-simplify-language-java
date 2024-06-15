package dev.edigonzales.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WordExportService {
    private static final Logger logger = LoggerFactory.getLogger(WordExportService.class);

    public InputStream createWordFile(String text){
        XWPFDocument document = new XWPFDocument(); 
        try {
            File outFile = Files.createTempFile("simplify_"+UUID.randomUUID().toString(), ".docx").toFile();
            FileOutputStream out = new FileOutputStream(outFile);
              
            System.err.println(outFile.getAbsolutePath());
            
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(text);
                  
            document.write(out);
            out.close();

            FileInputStream fis = new FileInputStream(outFile);
            return fis;            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
