package dev.edigonzales.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
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
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

import dev.edigonzales.service.SimplifyService;

import com.vaadin.flow.component.orderedlayout.FlexComponent;

@PageTitle("Sprache einfach vereinfachen")
@Route(value = "")
@RouteAlias(value = "")
public class SimplifyView extends Composite<VerticalLayout> {

    private static final int MAX_CHARACTERS = 20_000;
    
    SimplifyService simplifyService;
    
    public SimplifyView(SimplifyService simplifyService) {
        this.simplifyService = simplifyService;
        
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
        Checkbox checkbox = new Checkbox();
        RadioButtonGroup radioGroup = new RadioButtonGroup();
        
        HorizontalLayout layoutRow4 = new HorizontalLayout();
        Button buttonAnalyse = new Button();
        Button buttonSimplify = new Button();
        
        getContent().setWidth("100%");
        getContent().setHeight("100%");
        getContent().getStyle().set("flex-grow", "1");
        
        h3.setText("Sprache einfach vereinfachen");
        h3.setWidth("max-content");
        textSmall.setText(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        textSmall.setWidth("100%");
        textSmall.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        
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
        textSmallBefore.setText("Lorem ipsum dolor sit amet.");
        textSmallBefore.setWidth("100%");
        textSmallBefore.getStyle().set("flex-grow", "0");
        textSmallBefore.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        verticalCol1.add(textSmallBefore);

        verticalCol2.setAlignItems(FlexComponent.Alignment.STRETCH);
        verticalCol2.setPadding(false);

        textAreaAfter.setLabel("Dein vereinfachter Text");
        textAreaAfter.setWidth("100%");
        textAreaAfter.getStyle().set("flex-grow", "1");
        textAreaAfter.getStyle().set("font-size", "0.9em");
        //textAreaAfter.setReadOnly(true);
        verticalCol2.add(textAreaAfter);
        textSmallAfter.setText("Lorem ipsum dolor sit amet.");
        textSmallAfter.setWidth("100%");
        textSmallAfter.getStyle().set("flex-grow", "0");
        textSmallAfter.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        verticalCol2.add(textSmallAfter);

        textSmallResult.setText("Lorem ipsum dolor sit amet.");
        textSmallResult.getStyle().set("flex-grow", "1");
        textSmallResult.setWidth("50%");
        textSmallResult.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        
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
        
        checkbox.setLabel("Leichte Sprache");
        checkbox.getStyle().set("flex-grow", "1");
        layoutRow3.setAlignSelf(FlexComponent.Alignment.END, checkbox);
        radioGroup.setLabel("Sprachmodell");
        radioGroup.getStyle().set("flex-grow", "1");
        radioGroup.setItems("GPT-4", "GPT-4o", "Llama3");
        radioGroup.setValue("GPT-4");
        radioGroup.setReadOnly(true);
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
        
        getContent().add(h3);
        getContent().add(textSmall);
        getContent().add(layoutRow);
        layoutRow.add(verticalCol1);
        layoutRow.add(verticalCol2);
        layoutRow.add(textSmallResult);        
        getContent().add(layoutRow3);
        layoutRow3.add(checkbox);
        layoutRow3.add(radioGroup);
        getContent().add(layoutRow4);
        layoutRow4.add(buttonAnalyse);
        layoutRow4.add(buttonSimplify);
        
        buttonSimplify.addClickListener(e -> {
            
            System.err.println(e);
            System.err.println(textAreaBefore.getValue());
            simplifyService.simplifyText();
        });
        
    }
}
