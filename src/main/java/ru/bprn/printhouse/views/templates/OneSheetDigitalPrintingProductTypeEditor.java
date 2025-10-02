package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.operation.EditableTextArea;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;

import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.function.Consumer;

    public class OneSheetDigitalPrintingProductTypeEditor extends AbstractEditor<OneSheetDigitalPrintingProductType> {

        private final NumberField sizeX = new NumberField("Ширина изделия (мм)");
        private final NumberField sizeY = new NumberField("Длина изделия (мм)");
        private final NumberField bleed = new NumberField("Поле на подрезку (мм)");

        private final EditableTextArea<OneSheetDigitalPrintingProductType> materialFormula;
        private final com.vaadin.flow.component.textfield.TextField nameField = new com.vaadin.flow.component.textfield.TextField("Название цепочки работ");
        private final Select<AbstractMaterials> defaultMaterial = new Select<>("Материал по умолчанию", e->{});
        private final MultiSelectComboBox<AbstractMaterials> selectedMaterials = new MultiSelectComboBox<>("Выбранные материалы");
        private final ComboBox<StandartSize> standartSize = new ComboBox<>("Выберите размер изделия");
        private final OneSheetDigitalPrintingProductType entity;
        private final Checkbox multiplicationCheckbox = new Checkbox("Замостить", true);

        public OneSheetDigitalPrintingProductTypeEditor(OneSheetDigitalPrintingProductType entity, Consumer<Object> onSave,
                                                        PrintSheetsMaterialService materialService, FormulasService formulasService,
                                                        StandartSizeService standartSizeService, TypeOfOperationService typeOfOperationService,
                                                        FormulaValidationService formulaValidationService, ProductTypeVariableService productTypeVariableService, TemplateVariableService templateVariableService) {
            super(onSave);
            this.entity = entity;

            materialFormula = new EditableTextArea<>(
                    "Формула материала", formulasService, typeOfOperationService,
                    formulaValidationService, productTypeVariableService, templateVariableService
            );
            materialFormula.setVariableContext(entity.getVariables());

            // Настройка компонентов
            sizeX.setMin(0.1);
            sizeY.setMin(0.1);
            bleed.setMin(0.0);

            selectedMaterials.setItems(materialService.findAll().stream().map(m -> (AbstractMaterials)m).toList());
            selectedMaterials.setItemLabelGenerator(AbstractMaterials::getName);

            defaultMaterial.setItemLabelGenerator(AbstractMaterials::getName);
            if (this.entity.getSelectedMaterials() != null) defaultMaterial.setItems(this.entity.getSelectedMaterials());

            standartSize.setItemLabelGenerator(StandartSize::getName);
            standartSize.setItems(standartSizeService.findAll());
            standartSize.setAllowCustomValue(false);
            standartSize.setClearButtonVisible(true);
            standartSize.setPlaceholder("Выберите размер...");

            multiplicationCheckbox.setTooltipText("Замостить оптимально (uncheck - одно изделие на печатном листе)");

            binder.forField(nameField).bind(OneSheetDigitalPrintingProductType::getName, OneSheetDigitalPrintingProductType::setName);
            binder.forField(sizeX).bind(doubleVariableProvider("productWidth"), doubleVariableSetter("productWidth"));
            binder.forField(sizeY).bind(doubleVariableProvider("productLength"), doubleVariableSetter("productLength"));
            binder.forField(bleed).bind(doubleVariableProvider("bleed"), doubleVariableSetter("bleed"));
            binder.forField(materialFormula).bind(stringVariableProvider("materialFormula"), stringVariableSetter("materialFormula"));
            binder.forField(selectedMaterials).bind(OneSheetDigitalPrintingProductType::getSelectedMaterials, OneSheetDigitalPrintingProductType::setSelectedMaterials);
            binder.forField(defaultMaterial).bind(OneSheetDigitalPrintingProductType::getDefaultMaterial, (product, material) -> product.setDefaultMaterial((PrintSheetsMaterial) material));
            binder.forField(standartSize).bind(productType ->{
                return standartSize.getListDataView().getItems()
                        .filter(f-> f.getLength().equals(doubleVariableProvider("productWidth").apply(productType))
                                && f.getWidth().equals(doubleVariableProvider("productLength").apply(productType)))
                        .findFirst()
                        .orElse(null);
            },
                    (productType, standartSize) ->{
                        if (standartSize != null) {
                            doubleVariableSetter("productWidth").accept(productType, standartSize.getLength());
                            doubleVariableSetter("productLength").accept(productType, standartSize.getWidth());
                        }
                        else {
                            doubleVariableSetter("productWidth").accept(productType, null);
                            doubleVariableSetter("productLength").accept(productType, null);
                        }
                    });
            binder.forField(multiplicationCheckbox).bind(booleanVariableProvider("multiplication"), booleanVariableSetter("multiplication"));

            selectedMaterials.addValueChangeListener(event -> {
                Set<AbstractMaterials> selected = event.getValue();
                defaultMaterial.setItems(selected);
                if (selected != null && !selected.isEmpty() && !selected.contains(defaultMaterial.getValue())) {
                    defaultMaterial.clear(); // Только если новый набор не пустой
                }
            });

            standartSize.addValueChangeListener(e->{
                var value = e.getValue();
                if (value != null) {
                    sizeX.setValue(e.getValue().getLength());
                    sizeY.setValue(e.getValue().getWidth());
                }
            });

            add(buildForm());
            addButtons();
            edit(this.entity);
        }

        @Override
        protected Component buildForm() {
            FormLayout form = new FormLayout();
            form.setColumnWidth("6em");
            form.setResponsiveSteps(List.of(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("120px", 2),
                    new FormLayout.ResponsiveStep("240px", 3),
                    new FormLayout.ResponsiveStep("360px", 4),
                    new FormLayout.ResponsiveStep("480px", 5),
                    new FormLayout.ResponsiveStep("600px", 6)
            ));
            form.setAutoResponsive(true);
            form.setExpandFields(true);
            var row1 = new FormLayout.FormRow();
            row1.add(nameField, 6);
            var row2 = new FormLayout.FormRow();
            row2.add(standartSize,3);
            row2.add(sizeX,1);
            row2.add(sizeY, 1);
            row2.add(bleed, 1);
            var row4 = new FormLayout.FormRow();
            row4.add(selectedMaterials,3);
            row4.add(defaultMaterial,3);
            var row5 = new FormLayout.FormRow();
            row5.add(multiplicationCheckbox,6);

            form.add(row1, row2, materialFormula, row4, row5);
            form.setExpandColumns(true);
            form.setWidthFull();

            return form;
        }

        private Optional<Variable> getVariable(AbstractProductType productType, String key) {
            if (productType == null || productType.getVariables() == null) {
                return Optional.empty();
            }
            return productType.getVariables().stream()
                    .filter(v -> key.equals(v.getKey()))
                    .findFirst();
        }

        private com.vaadin.flow.function.ValueProvider<OneSheetDigitalPrintingProductType, Double> doubleVariableProvider(String key) {
            return productType -> getVariable(productType, key)
                    .map(v -> (Double) v.getValueAsObject())
                    .orElse(0.0);
        }

        private com.vaadin.flow.data.binder.Setter<OneSheetDigitalPrintingProductType, Double> doubleVariableSetter(String key) {
            return (productType, value) -> getVariable(productType, key).ifPresent(v -> v.setValue(value));
        }

        private com.vaadin.flow.function.ValueProvider<OneSheetDigitalPrintingProductType, Boolean> booleanVariableProvider(String key) {
            return productType -> getVariable(productType, key)
                    .map(v -> (Boolean) v.getValueAsObject())
                    .orElse(false);
        }

        private com.vaadin.flow.data.binder.Setter<OneSheetDigitalPrintingProductType, Boolean> booleanVariableSetter(String key) {
            return (productType, value) -> getVariable(productType, key).ifPresent(v -> v.setValue(value));
        }

        private com.vaadin.flow.function.ValueProvider<OneSheetDigitalPrintingProductType, String> stringVariableProvider(String key) {
            return productType -> getVariable(productType, key)
                    .map(Variable::getValue)
                    .orElse("");
        }

        private com.vaadin.flow.data.binder.Setter<OneSheetDigitalPrintingProductType, String> stringVariableSetter(String key) {
            return (productType, value) -> getVariable(productType, key).ifPresent(v -> v.setValue(value));
        }
    }
