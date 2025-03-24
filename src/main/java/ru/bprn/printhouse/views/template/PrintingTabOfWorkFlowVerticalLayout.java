package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.CostOfPrintSizeLeafAndColorService;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;
import ru.bprn.printhouse.data.service.PrintMashineService;

@UIScope
@AnonymousAllowed
public class PrintingTabOfWorkFlowVerticalLayout extends VerticalLayout
        implements HasBinder, HasMaterial, ExtraLeaves, Price {

    private final PrintMashineService printerService;
    private final CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService;

    private final SizeOfPrintLeaf size;
    private final FormulasService formulasService;

    private CreateFormula dialogFormula;
    private Formulas formula;
    private final Select<Formulas> formulaCombo = new Select<>();
    private final Select<PrintMashine> printerCombo = new Select<>();
    private final Select<QuantityColors> backQuantityOfColor = new Select<>();
    private final Select<QuantityColors> coverQuantityOfColor = new Select<>();
    private final Select<Material> materialSelect = new Select<>();
    private static SelectMaterailsDialog dialog;

    @Getter
    private final BeanValidationBinder<DigitalPrinting> templateBinder;

    public PrintingTabOfWorkFlowVerticalLayout(PrintMashineService printerService,
                                               CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService,
                                               FormulasService formulasService, SizeOfPrintLeaf size){

        this.printerService = printerService;
        this.costOfPrintSizeLeafAndColorService = costOfPrintSizeLeafAndColorService;
        this.size = size;
        this.formulasService = formulasService;
        templateBinder = new BeanValidationBinder<>(DigitalPrinting.class);
        dialog = new SelectMaterailsDialog("Выберите материалы для печати");
        addPrinterSection();
        this.add(addMaterialBlock());
        this.add(addFormula());
        addMaterialSection();
        postConstruct();

    }

    private void addMaterialSection() {

        var button = new Button("Нажмите для выбора материала", buttonClickEvent -> {
            dialog.setMaterials(printerCombo.getValue().getMaterials());
            dialog.open();
        });
        //materialSelect.addFocusListener(selectFocusEvent -> materialSelect.setItems(dialog.getSelected()));
        templateBinder.forField(materialSelect).bind(DigitalPrinting::getDefaultMaterial, DigitalPrinting::setDefaultMaterial);
        //templateBinder.forField(dialog).bind(DigitalPrinting::getMaterials, DigitalPrinting::setMaterials);

        this.add(button, materialSelect);
    }

    private <T> void comboBoxViewFirstElement(Select<T> combo) {
        if (combo!=null)
            if (combo.getListDataView().getItemCount()>0)
                combo.setValue(combo.getListDataView().getItem(0));
    }

    private Div addFormula() {
        var div = new Div();
        div.setWidth("50%");

        formulaCombo.setAriaLabel("Формула расчета:");
        formulaCombo.setSizeFull();
        formulaCombo.setEmptySelectionAllowed(false);
        formulaCombo.addThemeVariants(SelectVariant.LUMO_ALIGN_RIGHT);
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

        // Цветность лица
        templateBinder.forField(coverQuantityOfColor).asRequired().bind(DigitalPrinting::getQuantityColorsCover, DigitalPrinting::setQuantityColorsCover);
        coverQuantityOfColor.setLabel("Лицо");
        coverQuantityOfColor.setEmptySelectionAllowed(false);

        // Цветность оборота
        templateBinder.forField(backQuantityOfColor).asRequired().bind(DigitalPrinting::getQuantityColorsBack, DigitalPrinting::setQuantityColorsBack);
        backQuantityOfColor.setLabel("Оборот");
        backQuantityOfColor.setEmptySelectionAllowed(false);

        // Принтеры
        printerCombo.setLabel("Принтер:");
        printerCombo.setEmptySelectionAllowed(false);
        templateBinder.forField(printerCombo).asRequired().bind(DigitalPrinting::getPrintMashine, DigitalPrinting::setPrintMashine);

        printerCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                dialog.setMaterials(e.getValue().getMaterials());
                coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
                comboBoxViewFirstElement(coverQuantityOfColor);
                backQuantityOfColor.setItems(e.getValue().getQuantityColors());
                comboBoxViewFirstElement(backQuantityOfColor);
            }
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
    public String[] getBeanAsString(){
        return JSONToObjectsHelper.getBeanAsJSONStr(templateBinder.getBean());
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

    private void postConstruct() {
        var printers = printerService.findAll();
        if (printers!= null) {
            printerCombo.setItems(printers);
            printerCombo.setValue(printers.getFirst());

            dialog.setMaterials(printers.getFirst().getMaterials());
            //dialog.setSelectedMaterial(materialSelect.getListDataView().);

            coverQuantityOfColor.setItems(printers.getFirst().getQuantityColors());
            backQuantityOfColor.setItems(printers.getFirst().getQuantityColors());
            comboBoxViewFirstElement(coverQuantityOfColor);
            comboBoxViewFirstElement(backQuantityOfColor);

            var formulas = formulasService.findAll();
            if (!formulas.isEmpty()) {
                formulaCombo.setItems(formulasService.findAll());
                formulaCombo.setValue(formulasService.findAll().getFirst());
            }

        }

    }

}