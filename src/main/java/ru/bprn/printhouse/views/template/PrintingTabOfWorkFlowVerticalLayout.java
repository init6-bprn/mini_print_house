package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.CostOfPrintSizeLeafAndColorService;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;

@UIScope
@AnonymousAllowed
public class PrintingTabOfWorkFlowVerticalLayout extends VerticalLayout
        implements HasBinder, HasMargins, HasMaterial, ExtraLeaves, Price {

    private final PrintMashineService printerService;
    private final QuantityColorsService quantityColorsService;
    private final CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService;

    private final ObjectMapper objectMapper;
    private final SizeOfPrintLeaf size;
    private final FormulasService formulasService;
    private CreateFormula dialogFormula;
    private Formulas formula;
    private ComboBox<Formulas> formulaCombo = new ComboBox<>("Формула расчета");

    @Getter
    private final BeanValidationBinder<DigitalPrinting> templateBinder;

    public PrintingTabOfWorkFlowVerticalLayout(PrintMashineService printerService, QuantityColorsService quantityColorsService,
                                               CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService,
                                               FormulasService formulasService, SizeOfPrintLeaf size){

        this.printerService = printerService;
        this.quantityColorsService = quantityColorsService;
        this.costOfPrintSizeLeafAndColorService = costOfPrintSizeLeafAndColorService;
        this.size = size;
        this.formulasService = formulasService;
        objectMapper = new ObjectMapper();
        templateBinder = new BeanValidationBinder<>(DigitalPrinting.class);
        addPrinterSection();
        this.add(addMaterialBlock());
        this.add(addFormula());
    }

    private Div addFormula() {
        var div = new Div();
        div.setWidth("50%");
        formulaCombo.setItems(formulasService.findAll());
        formulaCombo.setAllowCustomValue(false);
        formulaCombo.setSizeFull();
        formulaCombo.addThemeVariants(ComboBoxVariant.LUMO_ALIGN_RIGHT);
        formulaCombo.addValueChangeListener(e -> formula = e.getValue());
        formulaCombo.setPrefixComponent(addPrefix());
        templateBinder.forField(formulaCombo).bind(DigitalPrinting::getFormula, DigitalPrinting::setFormula);
        div.add(formulaCombo);
        return div;
    }

    private Div addPrefix(){
        var div = new Div();
        var create = new Button(VaadinIcon.PLUS.create(), buttonClickEvent -> {
            if (dialogFormula == null) dialogFormula = new CreateFormula(new Formulas(), formulasService);
            else dialogFormula.setFormulaBean(new Formulas());
            dialogFormula.open();
            formulaCombo.setItems(formulasService.findAll());
            templateBinder. refreshFields();
            formulaCombo.setValue(dialogFormula.getFormulaBean());
        });

        var update = new Button(VaadinIcon.EDIT.create(), buttonClickEvent -> {
            if (formula!=null) {
                if (dialogFormula == null) dialogFormula = new CreateFormula(formula, formulasService);
                else dialogFormula.setFormulaBean(formula);
                dialogFormula.open();
                templateBinder.refreshFields();
            }
        });
        div.add(create, update);
        return div;
    }


    private void addPrinterSection() {

        var hLayout = new HorizontalLayout();

        var coverQuantityOfColor = new ComboBox<QuantityColors>();
        templateBinder.forField(coverQuantityOfColor).asRequired().bind(DigitalPrinting::getQuantityColorsCover, DigitalPrinting::setQuantityColorsCover);
        coverQuantityOfColor.setItems(quantityColorsService.findAll());

        var backQuantityOfColor = new ComboBox<QuantityColors>();
        templateBinder.forField(backQuantityOfColor).asRequired().bind(DigitalPrinting::getQuantityColorsBack, DigitalPrinting::setQuantityColorsBack);
        backQuantityOfColor.setItems(quantityColorsService.findAll());

        // Принтеры
        var printerCombo = new ComboBox<PrintMashine>();
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        printerCombo.setItems(printerService.findAll());
        templateBinder.forField(printerCombo).asRequired().bind(DigitalPrinting::getPrintMashine, DigitalPrinting::setPrintMashine);

        // Цветность лица
        coverQuantityOfColor.setLabel("Лицо");

        // Цветность оборота
        backQuantityOfColor.setLabel("Оборот");

        printerCombo.addValueChangeListener(e -> {
            var oldValue = coverQuantityOfColor.getValue();
            coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
            coverQuantityOfColor.setValue(oldValue);
            var oldValue2 = backQuantityOfColor.getValue();
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
            backQuantityOfColor.setValue(oldValue2);
        });

        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);

    }

    private HorizontalLayout addMaterialBlock() {
        var checkBox = new Checkbox("Нужна приводка?");

        var intField = new IntegerField("Количество листов:");
        var hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.BASELINE);

        intField.setValue(0);
        checkBox.setValue(false);
        checkBox.addValueChangeListener(e -> {
            intField.setEnabled(e.getValue());
            if (!intField.isEnabled()) intField.setValue(0);
        });

        templateBinder.forField(intField).asRequired().bind(DigitalPrinting::getQuantityOfExtraLeaves, DigitalPrinting::setQuantityOfExtraLeaves);
        templateBinder.forField(checkBox).bind(DigitalPrinting::isNeedExtraLeaves, DigitalPrinting::setNeedExtraLeaves);

        hl.add(checkBox, intField);
        return hl;
    }

    @Override
    public Boolean isValid() {
        return templateBinder.isValid();
    }

    @Override
    public String getBeanAsString(){
        try {
            return objectMapper.writeValueAsString(templateBinder.getBean());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void setBeanFromString(String str){
        try {
            if (!str.equals("null")) templateBinder.setBean(objectMapper.readValue(str, DigitalPrinting.class));
            else templateBinder.setBean(new DigitalPrinting());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Gap getMargins() {
        return templateBinder.getBean().getPrintMashine().getGap();
    }

    @Override
    public String getMaterialFormula() {
        return "";
    }


    @Override
    public int getExtraLeaves() {
        return templateBinder.getBean().getQuantityOfExtraLeaves();
    }

    @Override
    public double getPriceOfOperation() {
        double total = .0;
        CostOfPrintSizeLeafAndColor costCover = costOfPrintSizeLeafAndColorService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf
                (templateBinder.getBean().getPrintMashine(), templateBinder.getBean().getQuantityColorsCover(), size);
        if (costCover != null) total += costCover.getCoast();
        CostOfPrintSizeLeafAndColor costBack = costOfPrintSizeLeafAndColorService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf
                (templateBinder.getBean().getPrintMashine(), templateBinder.getBean().getQuantityColorsBack(), size);
        if (costBack!=null) total += costBack.getCoast();
    return total;
    }

    @Override
    public double getPriceOfWork() {
        return 0;
    }

    @Override
    public double getPriceOfAmmo() {
        return 0;
    }

    @Override
    public int getTimeOfOperationPerSec() {
        return 0;
    }

    @Override
    public String getFormula() {
        return formulaCombo.getValue().getFormula();
    }

}