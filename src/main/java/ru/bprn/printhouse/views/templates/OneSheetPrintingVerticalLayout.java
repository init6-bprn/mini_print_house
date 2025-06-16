package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.templates.entity.OneSheetPrinting;

import java.util.Objects;

@UIScope
@AnonymousAllowed
public class OneSheetPrintingVerticalLayout extends VerticalLayout
        implements HasBinder {

    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;

    private final CreateFormula dialogFormula;
    private final Select<Formulas> formulaCombo = new Select<>();
    private final Select<Material> materialSelect = new Select<>();
    private SelectMaterailsDialog dialog;

    @Getter
    private final BeanValidationBinder<OneSheetPrinting> templateBinder;

    public OneSheetPrintingVerticalLayout(
            FormulasService formulasService,
            StandartSizeService standartSizeService,
            GapService gapService,
            VariablesForMainWorksService variables,
            MaterialService materialService)
    {
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
        templateBinder = new BeanValidationBinder<>(OneSheetPrinting.class);
        dialogFormula = new CreateFormula(formulasService, variables);

        add(addSizeOfProductSection());
        addMaterialSection();
        add(addOrientation());

        dialogFormula.addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened()) {
                var oldSelection = formulaCombo.getOptionalValue();
                formulaCombo.setItems(formulasService.findAll());
                oldSelection.ifPresent(formulaCombo::setValue);
            }
        });
    }

    private HorizontalLayout addSizeOfProductSection() {
        var hLayout = new HorizontalLayout();
        hLayout.setAlignItems(Alignment.START);

        var length = new NumberField("Длина");
        length.setWidth(13, Unit.PERCENTAGE);
        templateBinder.forField(length).asRequired().bind(OneSheetPrinting::getProductSizeX, OneSheetPrinting::setProductSizeX);

        var width = new NumberField("Ширина");
        width.setWidth(13, Unit.PERCENTAGE);
        templateBinder.forField(width).asRequired().bind(OneSheetPrinting::getProductSizeY, OneSheetPrinting::setProductSizeY);

        var sizeOfPaperCombo = new Select<StandartSize>("Размер изделия",e -> {
            if (e.getValue()!=null) {
                length.setValue(e.getValue().getLength());
                width.setValue(e.getValue().getWidth());
            }
        });
        sizeOfPaperCombo.setWidth(30, Unit.PERCENTAGE);
        sizeOfPaperCombo.setItems(standartSizeService.findAll());
        sizeOfPaperCombo.setEmptySelectionAllowed(true);
        templateBinder.forField(sizeOfPaperCombo).asRequired().bind(OneSheetPrinting::getStandartSize, OneSheetPrinting::setStandartSize);

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
        templateBinder.forField(bleedCombo).withValidator(Objects::nonNull, "Обязательно заполнить!").bind(OneSheetPrinting::getBleed, OneSheetPrinting::setBleed);

        hLayout.add(sizeOfPaperCombo, length, width, layout, bleedCombo, dialog);
        return hLayout;
    }

    private HorizontalLayout addOrientation() {
        var hl = new HorizontalLayout();
        hl.setSizeFull();
        var radioGroup = new RadioButtonGroup<String>();
        templateBinder.forField(radioGroup).asRequired().bind(OneSheetPrinting::getOrientation, OneSheetPrinting::setOrientation);
        radioGroup.setItems("Автоматически", "Вертикальная", "Горизонтальная");
        radioGroup.setValue("Автоматически");
        radioGroup.addValueChangeListener(e -> templateBinder.getBean().setOrientation(e.getValue()));

        hl.add(radioGroup);
        return hl;
    }

    private void addMaterialSection() {
        dialog = new SelectMaterailsDialog("Выберите материалы для печати");
        dialog.getGrid().setItems(materialService.findAll());
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
        templateBinder.forField(materialSelect).asRequired().bind(OneSheetPrinting::getDefaultMaterial, OneSheetPrinting::setDefaultMaterial);
        templateBinder.forField(dialog.getGrid().asMultiSelect()).bind(OneSheetPrinting::getMaterials, OneSheetPrinting::setMaterials);

        var materialFormula = new Select<Formulas>();
        materialFormula.setLabel("Формула расчета материала");
        materialFormula.setItems(formulasService.findAll());
        materialFormula.setEmptySelectionAllowed(false);
        materialFormula.setPrefixComponent(addPrefix());
        templateBinder.forField(materialFormula).asRequired().bind(OneSheetPrinting::getMaterialFormula, OneSheetPrinting::setMaterialFormula);

        add(new HorizontalLayout(Alignment.BASELINE, button, materialSelect, materialFormula));
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

    @Override
    public Boolean isValid() {
        return templateBinder.isValid();
    }

    @Override
    public String[] getBeanAsString(){
        return JSONToObjectsHelper.getBeanAsJSONStr(templateBinder.getBean());
    }
}