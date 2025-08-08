package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

    public class OneSheetDigitalPrintingProductTypeEditor extends AbstractEditor<OneSheetDigitalPrintingProductType> {

        private final NumberField sizeX = new NumberField("Ширина изделия (см)");
        private final NumberField sizeY = new NumberField("Высота изделия (см)");
        private final NumberField bleed = new NumberField("Вылет (см)");

        private final TextField materialFormula = new TextField("Формула материала");
        private final TextField nameField = new TextField("Название цепочки работ");

        private final ComboBox<AbstractMaterials> defaultMaterial = new ComboBox<>("Материал по умолчанию");
        private final MultiSelectComboBox<AbstractMaterials> selectedMaterials = new MultiSelectComboBox<>("Выбранные материалы");
        private final ComboBox<Formulas> formulasComboBox = new ComboBox<>("Формула расчета материала");
        private final OneSheetDigitalPrintingProductType entity;

        public OneSheetDigitalPrintingProductTypeEditor(OneSheetDigitalPrintingProductType entity, Consumer<Object> onSave,
                                                        PrintSheetsMaterialService materialService, FormulasService formulasService,
                                                        VariablesForMainWorksService variablesForMainWorksService) {
            super(onSave);
            this.entity = entity;

            // Настройка компонентов
            sizeX.setMin(0.1);
            sizeY.setMin(0.1);
            bleed.setMin(0.0);

            defaultMaterial.setItemLabelGenerator(AbstractMaterials::getName);
            List<PrintSheetsMaterial> list = materialService.findAll();
            selectedMaterials.setItems(list.stream().map(m -> (AbstractMaterials) m).toList());
            selectedMaterials.setItemLabelGenerator(AbstractMaterials::getName);

            formulasComboBox.setItemLabelGenerator(Formulas::getName);
            formulasComboBox.setItems(formulasService.findAll());
            formulasComboBox.setAllowCustomValue(false);
            formulasComboBox.setClearButtonVisible(true);
            formulasComboBox.setPlaceholder("Выберите формулу...");

            binder.forField(nameField).bind(OneSheetDigitalPrintingProductType::getName, OneSheetDigitalPrintingProductType::setName);
            binder.forField(sizeX).bind(OneSheetDigitalPrintingProductType::getProductSizeX, OneSheetDigitalPrintingProductType::setProductSizeX);
            binder.forField(sizeY).bind(OneSheetDigitalPrintingProductType::getProductSizeY, OneSheetDigitalPrintingProductType::setProductSizeY);
            binder.forField(bleed).bind(OneSheetDigitalPrintingProductType::getBleed, OneSheetDigitalPrintingProductType::setBleed);
            binder.forField(materialFormula).bind(OneSheetDigitalPrintingProductType::getMaterialFormula, OneSheetDigitalPrintingProductType::setMaterialFormula);
            binder.forField(defaultMaterial).bind(OneSheetDigitalPrintingProductType::getDefaultMaterial, OneSheetDigitalPrintingProductType::setDefaultMaterial);
            binder.forField(selectedMaterials).bind(OneSheetDigitalPrintingProductType::getSelectedMaterials, OneSheetDigitalPrintingProductType::setSelectedMaterials);
            binder.forField(formulasComboBox).bind(productType -> {
                        return formulasService.findAll().stream()
                                .filter(f -> f.getName().equals(productType.getMaterialFormula()))
                                .findFirst()
                                .orElse(null);
                    },
                    (productType, formula) -> {
                        if (formula != null) {
                            productType.setMaterialFormula(formula.getName()); // или getCode()
                        } else {
                            productType.setMaterialFormula(null);
                        }
                    }
            );

            selectedMaterials.addValueChangeListener(event -> {
                Set<AbstractMaterials> selected = event.getValue();
                defaultMaterial.setItems(selected);
                if (!selected.contains(defaultMaterial.getValue())) {
                    defaultMaterial.clear();
                }
            });

            add(buildForm());
            addButtons();
            edit(entity);
            initializeVariables(variablesForMainWorksService.findAll());
        }

        @Override
        protected Component buildForm() {
            FormLayout form = new FormLayout();
            form.setResponsiveSteps(List.of(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("500px", 2)
            ));

            form.add(nameField, sizeX, sizeY, bleed, formulasComboBox, materialFormula, defaultMaterial, selectedMaterials);
            return form;
        }

        private void initializeVariables(List<VariablesForMainWorks> allVariables) {
            Map<String, Object> variables = new HashMap<>();

            for (VariablesForMainWorks var : allVariables) {
                if ("OneSheetDigitalPrintingProductType".equals(var.getClazz())) {
                    variables.put(var.getName(), 0.0); // Double по умолчанию
                }
            }

            entity.setVariables(variables);
        }

    }


