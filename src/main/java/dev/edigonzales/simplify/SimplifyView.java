package dev.edigonzales.simplify;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import dev.edigonzales.analyze.AnalyzeResponse;
import dev.edigonzales.analyze.AnalyzeService;
import dev.edigonzales.export.WordExportService;
import dev.edigonzales.statistics.StatisticsService;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.vaadin.flow.component.orderedlayout.FlexComponent;

@PageTitle("Sprache einfach vereinfachen")
@Route(value = "")
@RouteAlias(value = "")
public class SimplifyView extends Composite<VerticalLayout> {
    
    private static final int MAX_CHARACTERS = 20_000;
    
    private SimplifyService simplifyService;
    
    private AnalyzeService analyzeService;

    private WordExportService wordExportService;

    // Limits for the understandability score to determine if the text is easy, medium or hard to understand.
    private static final int LIMIT_HARD = 13;
    private static final int LIMIT_MEDIUM = 16;

    public SimplifyView(SimplifyService simplifyService, AnalyzeService analyzeService, WordExportService wordExportService) {
        this.simplifyService = simplifyService;
        this.analyzeService = analyzeService;
        this.wordExportService = wordExportService;
        
        H3 h3 = new H3();
        Paragraph textSmall = new Paragraph();
        
        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout verticalCol1 = new VerticalLayout();
        VerticalLayout verticalCol2 = new VerticalLayout();
       
        TextArea textAreaBefore = new TextArea();
        TextArea textAreaAfter = new TextArea();
        Paragraph textSmallResult = new Paragraph();
        
        HorizontalLayout layoutRow2 = new HorizontalLayout();
        Paragraph textSmallBefore = new Paragraph();
        Paragraph textSmallAfter = new Paragraph();
        Div textSmallDummy = new Div();
        
        HorizontalLayout layoutRow3 = new HorizontalLayout();
        Checkbox checkboxLeichteSprache = new Checkbox();
        Checkbox checkboxCondensedText = new Checkbox();
        RadioButtonGroup radioGroup = new RadioButtonGroup();
        
        HorizontalLayout layoutRow4 = new HorizontalLayout();
        Button buttonAnalyse = new Button();
        Button buttonSimplify = new Button();
        
        getContent().setWidth("100%");
        getContent().setHeight("100%");
        getContent().getStyle().set("flex-grow", "1");
        
        h3.setText("Sprache einfach vereinfachen");
        h3.setWidth("max-content");
        Html htmlSmallText = new Html("<span>⚠ Achtung: Diese App ist ein Prototyp. Nutze die App <span style=\"color:red;font-weight:700;\">nur für öffentliche, nicht sensible Daten</span>. Die App liefert lediglich einen Textentwurf. Überprüfe das Ergebnis immer und passe es an, wenn nötig.</span>");
        textSmall.add(htmlSmallText);
        textSmall.setWidth("100%");
        textSmall.getStyle().set("font-size", "var(--lumo-font-size-s)");
        
        layoutRow.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow);
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.getStyle().set("flex-grow", "1");
        layoutRow.setAlignItems(FlexComponent.Alignment.STRETCH);
        layoutRow.setHeight("100%");
                
        verticalCol1.setAlignItems(FlexComponent.Alignment.STRETCH);
        verticalCol1.setPadding(false);
        
        textAreaBefore.setLabel("Ausgangstext, den du vereinfachen möchtest");
        textAreaBefore.setWidth("100%");
        textAreaBefore.getStyle().set("flex-grow", "1");
        textAreaBefore.getStyle().set("font-size", "0.9em");
        textAreaBefore.setMaxLength(MAX_CHARACTERS);
        verticalCol1.add(textAreaBefore);
        textSmallBefore.setText("");
        textSmallBefore.setWidth("100%");
        textSmallBefore.getStyle().set("flex-grow", "0");
        textSmallBefore.getStyle().set("font-size", "var(--lumo-font-size-s)");
        verticalCol1.add(textSmallBefore);

        verticalCol2.setAlignItems(FlexComponent.Alignment.STRETCH);
        verticalCol2.setPadding(false);

        textAreaAfter.setLabel("Dein vereinfachter Text");
        textAreaAfter.setWidth("100%");
        textAreaAfter.getStyle().set("flex-grow", "1");
        textAreaAfter.getStyle().set("font-size", "0.9em");
        textAreaAfter.setReadOnly(true);
        verticalCol2.add(textAreaAfter);
        textSmallAfter.setText("");
        textSmallAfter.setWidth("100%");
        textSmallAfter.getStyle().set("flex-grow", "0");
        textSmallAfter.getStyle().set("font-size", "var(--lumo-font-size-s)");
        verticalCol2.add(textSmallAfter);

        textSmallResult.setText("Lorem ipsum dolor sit amet.");
        textSmallResult.getStyle().set("flex-grow", "1");
        textSmallResult.setWidth("50%");
        textSmallResult.getStyle().set("font-size", "var(--lumo-font-size-s)");
        
//        layoutRow2.setWidthFull();
//        getContent().setFlexGrow(1.0, layoutRow2);
//        layoutRow2.addClassName(Gap.MEDIUM);
//        layoutRow2.getStyle().set("flex-grow", "1");
//
//        textSmallBefore.setText("Lorem ipsum dolor sit amet.");
//        textSmallBefore.setWidth("100%");
//        textSmallBefore.getStyle().set("font-size", "var(--lumo-font-size-xs)");
//
//        textSmallAfter.setText("Lorem ipsum dolor sit amet.");
//        textSmallAfter.getStyle().set("flex-grow", "1");
//        textSmallAfter.setWidth("100%");
//        textSmallAfter.getStyle().set("font-size", "var(--lumo-font-size-xs)");
//        
//        textSmallDummy.setText("----");
//        textSmallDummy.getStyle().set("flex-grow", "1");
//        textSmallDummy.setWidth("45%");
//        textSmallDummy.getStyle().set("font-size", "var(--lumo-font-size-xs)");
                
        layoutRow3.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow3);
        layoutRow3.addClassName(Gap.MEDIUM);
        layoutRow3.setWidth("100%");
        layoutRow3.setHeight("min-content");
        
        checkboxLeichteSprache.setLabel("Leichte Sprache");
        //checkboxLeichteSprache.setHelperText("Leichte Sprache: Lorem ipsum...");
        checkboxLeichteSprache.getStyle().set("flex-grow", "0");
        layoutRow3.setAlignSelf(FlexComponent.Alignment.END, checkboxLeichteSprache);
        //checkboxLeichteSprache.setWidth("min-content");
        checkboxCondensedText.setLabel("Text verdichten");
        //checkboxCondensedText.setHelperText("Das Modell konzentriert sich auf essentielle Informationen <br> und versucht, Unwichtiges wegzulassen.");
        checkboxCondensedText.getStyle().set("flex-grow", "0");
        layoutRow3.setAlignSelf(FlexComponent.Alignment.END, checkboxCondensedText);        
        radioGroup.setLabel("Sprachmodell");
        radioGroup.getStyle().set("flex-grow", "1");
        radioGroup.setItems("GPT-4", "GPT-4o", "Llama3");
        radioGroup.setValue("GPT-4o");
        //radioGroup.setReadOnly(true);
        layoutRow3.setAlignSelf(FlexComponent.Alignment.END, radioGroup);
        //radioGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        
        layoutRow4.setWidthFull();
        getContent().setFlexGrow(1.0, layoutRow4);
        layoutRow4.addClassName(Gap.MEDIUM);
        layoutRow4.setWidth("100%");
        layoutRow4.setHeight("min-content");
        layoutRow4.setAlignItems(Alignment.START);
        layoutRow4.setJustifyContentMode(JustifyContentMode.END);

        buttonAnalyse.setText("Analysieren");
        buttonAnalyse.setWidth("min-content");
        buttonSimplify.setText("Vereinfachen");
        buttonSimplify.setWidth("min-content");
        buttonSimplify.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        buttonSimplify.setEnabled(false);
        
        getContent().add(h3);
        getContent().add(textSmall);
        getContent().add(layoutRow);
        layoutRow.add(verticalCol1);
        layoutRow.add(verticalCol2);
//        layoutRow.add(textSmallResult);        
        getContent().add(layoutRow3);
        layoutRow3.add(checkboxLeichteSprache);
        layoutRow3.add(checkboxCondensedText);
        layoutRow3.add(radioGroup);
        getContent().add(layoutRow4);
        layoutRow4.add(buttonAnalyse);
        layoutRow4.add(buttonSimplify);
                
        buttonSimplify.addClickListener(ev -> {
            try {
                SimplifyResponse simplifyResponse = simplifyService.call(textAreaBefore.getValue(), checkboxLeichteSprache.getValue(), checkboxCondensedText.getValue(), radioGroup.getValue().toString().toLowerCase());
                textAreaAfter.setValue(simplifyResponse.simplifiedText());
                {
                    int score = Double.valueOf(simplifyResponse.sourceStatistics().score()).intValue();
                    String cefrLevel = simplifyResponse.sourceStatistics().cefrLevel();
                    
                    textSmallBefore.removeAll();
                    Html htmlText;
                    if (score < LIMIT_HARD) {
                        htmlText = new Html("<span>Dein Ausgangstext ist <span style=\"color:red;font-weight:700;\">schwer verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:red;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                    } else if (score >= LIMIT_HARD && score < LIMIT_MEDIUM) {
                        htmlText = new Html("<span>Dein Ausgangstext ist <span style=\"color:orange;font-weight:700;\">nur mässig verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:orange;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                    } else {
                        htmlText = new Html("<span>Dein Ausgangstext ist <span style=\"color:green;font-weight:700;\">gut verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:green;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                    }                    
                    textSmallBefore.add(htmlText);                    
                }
                {
                    int score = Double.valueOf(simplifyResponse.targetStatistics().score()).intValue();
                    String cefrLevel = simplifyResponse.targetStatistics().cefrLevel();
                    
                    textSmallAfter.removeAll();
                    Html htmlText;
                    if (score < LIMIT_HARD) {
                        htmlText = new Html("<span>Dein vereinfachter Text ist <span style=\"color:red;font-weight:700;\">schwer verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:red;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                    } else if (score >= LIMIT_HARD && score < LIMIT_MEDIUM) {
                        htmlText = new Html("<span>Dein vereinfachter Text ist <span style=\"color:orange;font-weight:700;\">nur mässig verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:orange;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                    } else {
                        htmlText = new Html("<span>Dein vereinfachter Text ist <span style=\"color:green;font-weight:700;\">gut verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:green;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                    }
                    textSmallAfter.add(htmlText);
                    
                    StreamResource streamResource = new StreamResource("simplify.docx", () -> wordExportService.createWordFile(simplifyResponse.simplifiedText()));
                    Anchor link = new Anchor(streamResource, "herunterladen");
                    link.getElement().setAttribute("download", true);
                    textSmallAfter.add(" Den vereinfachten Text ");
                    textSmallAfter.add(link);
                    textSmallAfter.add(".");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        buttonAnalyse.addClickListener(ev -> {
            try {
                AnalyzeResponse analyzeResponse = analyzeService.call(textAreaBefore.getValue(), checkboxLeichteSprache.getValue(), checkboxCondensedText.getValue(), radioGroup.getValue().toString().toLowerCase());
                textAreaAfter.setValue(analyzeResponse.analyzeText());
                
                {
                    int score = Double.valueOf(analyzeResponse.sourceStatistics().score()).intValue();
                    String cefrLevel = analyzeResponse.sourceStatistics().cefrLevel();
                    
                    textSmallBefore.removeAll();
                    if (score < LIMIT_HARD) {
                        Html htmlText = new Html("<span>Dein Ausgangstext ist <span style=\"color:red;font-weight:700;\">schwer verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:red;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                        textSmallBefore.add(htmlText);
                    } else if (score >= LIMIT_HARD && score < LIMIT_MEDIUM) {
                        Html htmlText = new Html("<span>Dein Ausgangstext ist <span style=\"color:orange;font-weight:700;\">nur mässig verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:orange;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                        textSmallBefore.add(htmlText);
                    } else {
                        Html htmlText = new Html("<span>Dein Ausgangstext ist <span style=\"color:green;font-weight:700;\">gut verständlich</span>: " + score + " von 20 Punkten. Das entspricht etwa dem <span style=\"color:green;font-weight:700;\">Sprachniveau "+cefrLevel+"</span>.</span>");
                        textSmallBefore.add(htmlText);
                    }                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        textAreaBefore.setValue(sampleText01);
    }
    
    private InputStream getInputStream(String value) {
        String svg = "<?xml version='1.0' encoding='UTF-8' standalone='no'?>"
            + "<svg  xmlns='http://www.w3.org/2000/svg' "
            + "xmlns:xlink='http://www.w3.org/1999/xlink'>"
            + "<rect x='10' y='10' height='100' width='100' "
            + "style=' fill: #90C3D4'/><text x='30' y='30' fill='red'>"
            + value + "</text>" + "</svg>";
        return new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8));
    }

    private String sampleText01 = """
            Als Vernehmlassungsverfahren wird diejenige Phase innerhalb des Vorverfahrens der Gesetzgebung bezeichnet, in der Vorhaben des Bundes von erheblicher politischer, finanzieller, wirtschaftlicher, ökologischer, sozialer oder kultureller Tragweite auf ihre sachliche Richtigkeit, Vollzugstauglichkeit und Akzeptanz hin geprüft werden. 
            
            Die Vorlage wird zu diesem Zweck den Kantonen, den in der Bundesversammlung vertretenen Parteien, den Dachverbänden der Gemeinden, Städte und der Berggebiete, den Dachverbänden der Wirtschaft sowie weiteren, im Einzelfall interessierten Kreisen unterbreitet.
            """;
}
