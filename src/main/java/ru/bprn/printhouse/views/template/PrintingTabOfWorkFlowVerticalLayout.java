package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.select.SelectVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;

import java.util.Objects;

@UIScope
@AnonymousAllowed
public class PrintingTabOfWorkFlowVerticalLayout extends VerticalLayout
        implements HasBinder, ExtraLeaves, Price {

    private final PrintMashineService printerService;
    private final CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService;

    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;

    private final CreateFormula dialogFormula;
    private final Select<Formulas> formulaCombo = new Select<>();
    private final Select<PrintMashine> printerCombo = new Select<>();
    private final Select<QuantityColors> backQuantityOfColor = new Select<>();
    private final Select<QuantityColors> coverQuantityOfColor = new Select<>();
    private final Select<Material> materialSelect = new Select<>();
    private SelectMaterailsDialog dialog;

    @Getter
    private final BeanValidationBinder<DigitalPrinting> templateBinder;

    public PrintingTabOfWorkFlowVerticalLayout(
            PrintMashineService printerService,
            CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService,
            FormulasService formulasService,
            StandartSizeService standartSizeService,
            GapService gapService)
    {
        this.printerService = printerService;
        this.costOfPrintSizeLeafAndColorService = costOfPrintSizeLeafAndColorService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        templateBinder = new BeanValidationBinder<>(DigitalPrinting.class);
        dialogFormula = new CreateFormula(formulasService);

        addPrinterSection();
        addMaterialSection();
        add(addMaterialBlock());
        add(addOrientation());
        add(addFormula());
        add(addSizeOfProductSection());

        dialogFormula.addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened()) {
                var oldSelection = formulaCombo.getOptionalValue();
                formulaCombo.setItems(formulasService.findAll());
                oldSelection.ifPresent(formulaCombo::setValue);
            }
        });
        postConstruct();
    }

    private HorizontalLayout addSizeOfProductSection() {
        var hLayout = new HorizontalLayout();
        hLayout.setAlignItems(Alignment.START);

        var length = new NumberField("Длина");
        length.setWidth(13, Unit.PERCENTAGE);
        templateBinder.forField(length).asRequired().bind(DigitalPrinting::getProductSizeX, DigitalPrinting::setProductSizeX);

        var width = new NumberField("Ширина");
        width.setWidth(13, Unit.PERCENTAGE);
        templateBinder.forField(width).asRequired().bind(DigitalPrinting::getProductSizeY, DigitalPrinting::setProductSizeY);

        var sizeOfPaperCombo = new Select<StandartSize>("Размер изделия",e -> {
            if (e.getValue()!=null) {
                length.setValue(e.getValue().getLength());
                width.setValue(e.getValue().getWidth());
            }
        });
        sizeOfPaperCombo.setWidth(30, Unit.PERCENTAGE);
        sizeOfPaperCombo.setItems(standartSizeService.findAll());
        sizeOfPaperCombo.setEmptySelectionAllowed(true);
        templateBinder.forField(sizeOfPaperCombo).asRequired().bind(DigitalPrinting::getStandartSize, DigitalPrinting::setStandartSize);

        var dialog = new SizeDialog(standartSizeService);

        dialog.addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened()) {
                if (dialog.getStandartSize()!= null) {
                    sizeOfPaperCombo.setItems(standartSizeService.findAll());
                    sizeOfPaperCombo.setValue(dialog.getStandartSize());

                }
            }
        });

        var label = new NativeLabel("Добавить");
        label.getStyle().set("padding-top", "var(--lumo-space-s)")
                .set("font-size", "var(--lumo-font-size-xs)");
        var addSizeButton = new Button("Add");
        addSizeButton.setAriaLabel("Add");
        var layout = new VerticalLayout(label, addSizeButton);
        layout.setWidth(14, Unit.PERCENTAGE);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");
        addSizeButton.addClickListener(e-> {
            if ((length.getValue()!=null)&&(width.getValue()!=null)) {
                dialog.setX(length.getValue());
                dialog.setY(width.getValue());
                dialog.setModal(true);
                dialog.open();
            }
        });

        var bleedCombo = new Select<Gap>("Припуск", e->{});
        bleedCombo.setWidth(30, Unit.PERCENTAGE);
        bleedCombo.setItems(gapService.findAllBleeds("Bleed"));
        templateBinder.forField(bleedCombo).withValidator(Objects::nonNull, "Обязательно заполнить!").bind(DigitalPrinting::getBleed, DigitalPrinting::setBleed);

        hLayout.add(sizeOfPaperCombo, length, width, layout, bleedCombo, dialog);
        return hLayout;
    }

    private HorizontalLayout addOrientation() {
        var hl = new HorizontalLayout();
        hl.setSizeFull();
        var radioGroup = new RadioButtonGroup<String>();
        templateBinder.forField(radioGroup).asRequired().bind(DigitalPrinting::getOrientation, DigitalPrinting::setOrientation);
        radioGroup.setItems("Автоматически", "Вертикальная", "Горизонтальная");
        radioGroup.setValue("Автоматически");
        radioGroup.addValueChangeListener(e -> templateBinder.getBean().setOrientation(e.getValue()));

        hl.add(radioGroup);
        return hl;
    }

    private void addMaterialSection() {
        dialog = new SelectMaterailsDialog("Выберите материалы для печати");
        dialog.addOpenedChangeListener(openedChangeEvent -> {
            if (openedChangeEvent.isOpened()) dialog.setSelectedMaterial(templateBinder.getBean().getMaterials());
            else {
                var oldValue = materialSelect.getOptionalValue();
                materialSelect.setItems(dialog.getGrid().getSelectedItems());
                oldValue.ifPresent(materialSelect::setValue);
            }
        });

        var button = new Button("Выбор материалов", e -> dialog.open());
        materialSelect.setLabel("Установка основного материала");
        materialSelect.setEmptySelectionAllowed(false);
        materialSelect.addAttachListener(attachEvent -> {
           materialSelect.setItems(templateBinder.getBean().getMaterials());
           materialSelect.setValue(templateBinder.getBean().getDefaultMaterial());
        });
        templateBinder.forField(materialSelect).asRequired().bind(DigitalPrinting::getDefaultMaterial, DigitalPrinting::setDefaultMaterial);
        templateBinder.forField(dialog.getGrid().asMultiSelect()).bind(DigitalPrinting::getMaterials, DigitalPrinting::setMaterials);

        add(new HorizontalLayout(Alignment.BASELINE, button, materialSelect));
    }

    private <T> void comboBoxViewFirstElement(Select<T> combo) {
        if (combo!=null)
            if (combo.getListDataView().getItemCount()>0)
                combo.setValue(combo.getListDataView().getItem(0));
    }

    //---------------------Селектор для выбора формул с редактированием и созданием новой-------
    private Div addFormula() {
        var div = new Div();
        div.setWidth("50%");

        formulaCombo.setLabel("Формула расчета:");
        formulaCombo.setSizeFull();
        formulaCombo.setEmptySelectionAllowed(false);
        formulaCombo.addThemeVariants(SelectVariant.LUMO_ALIGN_RIGHT);
        formulaCombo.setPrefixComponent(addPrefix());
        templateBinder.forField(formulaCombo).bind(DigitalPrinting::getFormula, DigitalPrinting::setFormula);
        div.add(formulaCombo);
        return div;
    }

    private Div addPrefix() {
        var div = new Div();
        var create = new Button(VaadinIcon.PLUS.create(), buttonClickEvent -> {
            dialogFormula.setFormulaBean(new Formulas());
            dialogFormula.open();
        });

        var update = new Button(VaadinIcon.EDIT.create(), buttonClickEvent -> {
            if (formulaCombo.getOptionalValue().isPresent()) {
                dialogFormula.setFormulaBean(formulaCombo.getOptionalValue().get());
                dialogFormula.open();
            }
        });
        div.add(create, update);
        return div;
    }
//------------------------------------------------------------------------------------------------------

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
                dialog.getGrid().setItems(e.getValue().getMaterials());
                coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
                comboBoxViewFirstElement(coverQuantityOfColor);
                backQuantityOfColor.setItems(e.getValue().getQuantityColors());
                comboBoxViewFirstElement(backQuantityOfColor);
                if (templateBinder.getBean()!=null) templateBinder.getBean().setMargins(e.getValue().getGap());
            }
        });

        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);

    }

    private HorizontalLayout addMaterialBlock() {
        var intField = new IntegerField("Листов приводки:");
        var hl = new HorizontalLayout();
        hl.setAlignItems(Alignment.BASELINE);

        intField.setValue(0);
        templateBinder.forField(intField).asRequired().bind(DigitalPrinting::getQuantityOfExtraLeaves, DigitalPrinting::setQuantityOfExtraLeaves);

        hl.add(intField);
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
    public int getExtraLeaves() {
        return templateBinder.getBean().getQuantityOfExtraLeaves();
    }

    @Override
    public double getPriceOfOperation() {
        double total = .0;
        CostOfPrintSizeLeafAndColor costCover = costOfPrintSizeLeafAndColorService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf
                (templateBinder.getBean().getPrintMashine(),
                        templateBinder.getBean().getQuantityColorsCover(),
                        templateBinder.getBean().getDefaultMaterial().getSizeOfPrintLeaf());
        if (costCover != null) total += costCover.getCoast();
        CostOfPrintSizeLeafAndColor costBack = costOfPrintSizeLeafAndColorService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf
                (templateBinder.getBean().getPrintMashine(),
                        templateBinder.getBean().getQuantityColorsBack(),
                        templateBinder.getBean().getDefaultMaterial().getSizeOfPrintLeaf());
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
        var material = printerService.findAll().getFirst().getMaterials();
        if (!printers.isEmpty()) {
            printerCombo.setItems(printers);
            printerCombo.setValue(printers.getFirst());

            dialog.getGrid().setItems(material);
            dialog.getGrid().select(material.stream().findFirst().get());
            materialSelect.setItems(dialog.getGrid().getSelectedItems());

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