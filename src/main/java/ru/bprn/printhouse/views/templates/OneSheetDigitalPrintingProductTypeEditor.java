package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.StandartSizeService;
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

        private final NumberField sizeX = new NumberField("Ширина изделия (мм)");
        private final NumberField sizeY = new NumberField("Высота изделия (мм)");
        private final NumberField bleed = new NumberField("Вылет (мм)");

        private final TextField materialFormula = new TextField("Формула материала");
        private final TextField nameField = new TextField("Название цепочки работ");
        private final ComboBox<AbstractMaterials> defaultMaterial = new ComboBox<>("Материал по умолчанию");
        private final MultiSelectComboBox<AbstractMaterials> selectedMaterials = new MultiSelectComboBox<>("Выбранные материалы");
        private final ComboBox<Formulas> formulasComboBox = new ComboBox<>("Формула расчета материала");
        private final ComboBox<StandartSize> standartSize = new ComboBox<>("Выберите размер изделия");
        private final OneSheetDigitalPrintingProductType entity;

        public OneSheetDigitalPrintingProductTypeEditor(OneSheetDigitalPrintingProductType entity, Consumer<Object> onSave,
                                                        PrintSheetsMaterialService materialService, FormulasService formulasService,
                                                        VariablesForMainWorksService variablesForMainWorksService, StandartSizeService standartSizeService) {
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

            standartSize.setItemLabelGenerator(StandartSize::getName);
            standartSize.setItems(standartSizeService.findAll());
            standartSize.setAllowCustomValue(false);
            standartSize.setClearButtonVisible(true);
            standartSize.setPlaceholder("Выберите размер...");

            binder.forField(nameField).bind(OneSheetDigitalPrintingProductType::getName, OneSheetDigitalPrintingProductType::setName);
            binder.forField(sizeX).bind(OneSheetDigitalPrintingProductType::getProductSizeX, OneSheetDigitalPrintingProductType::setProductSizeX);
            binder.forField(sizeY).bind(OneSheetDigitalPrintingProductType::getProductSizeY, OneSheetDigitalPrintingProductType::setProductSizeY);
            binder.forField(bleed).bind(OneSheetDigitalPrintingProductType::getBleed, OneSheetDigitalPrintingProductType::setBleed);
            binder.forField(materialFormula).bind(OneSheetDigitalPrintingProductType::getMaterialFormula, OneSheetDigitalPrintingProductType::setMaterialFormula);
            binder.forField(defaultMaterial).bind(OneSheetDigitalPrintingProductType::getDefaultMaterial, OneSheetDigitalPrintingProductType::setDefaultMaterial);
            binder.forField(selectedMaterials).bind(OneSheetDigitalPrintingProductType::getSelectedMaterials, OneSheetDigitalPrintingProductType::setSelectedMaterials);
            binder.forField(formulasComboBox).bind(productType -> {
                        return  formulasComboBox.getListDataView().getItems()
                                .filter(f -> f.getFormula().equals(productType.getMaterialFormula()))
                                .findFirst()
                                .orElse(null);
                    },
                    (productType, formula) -> {
                        if (formula != null) {
                            productType.setMaterialFormula(formula.getFormula());
                        } else {
                            productType.setMaterialFormula(null);
                        }
                    }
            );
            binder.forField(standartSize).bind(productType ->{
                return standartSize.getListDataView().getItems()
                        .filter(f-> f.getLength().equals(productType.getProductSizeX())
                                && f.getWidth().equals(productType.getProductSizeY()))
                        .findFirst()
                        .orElse(null);
            },
                    (productType, standartSize) ->{
                        if (standartSize != null) {
                            productType.setProductSizeX(standartSize.getLength());
                            productType.setProductSizeY(standartSize.getWidth());
                        }
                        else {
                            productType.setProductSizeX(null);
                            productType.setProductSizeY(null);
                        }
                    });

            selectedMaterials.addValueChangeListener(event -> {
                Set<AbstractMaterials> selected = event.getValue();
                defaultMaterial.setItems(selected);
                if (!selected.contains(defaultMaterial.getValue())) {
                    defaultMaterial.clear();
                }
            });

            formulasComboBox.addValueChangeListener(e-> materialFormula.setValue(e.getValue().getFormula()));

            standartSize.addValueChangeListener(e->{
                sizeX.setValue(e.getValue().getLength());
                sizeY.setValue(e.getValue().getWidth());
            });

            add(buildForm());
            addButtons();
            edit(entity);
            initializeVariables(variablesForMainWorksService.findAll());
        }

        @Override
        protected Component buildForm() {
            FormLayout form = new FormLayout();
            form.setColumnWidth("7em");
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
            var row3 = new FormLayout.FormRow();
            row3.add(formulasComboBox, 3);
            row3.add(materialFormula, 3);
            var row4 = new FormLayout.FormRow();
            row4.add(selectedMaterials,3);
            row4.add(defaultMaterial,3);

            form.add(row1, row2, row3, row4);
            form.setExpandColumns(true);
            form.setWidthFull();

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


