package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.EditableTextArea;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ProductOperationEditor extends AbstractEditor<ProductOperation> {

    // UI Components
    private final ComboBox<Operation> operationComboBox = new ComboBox<>("Операция");
    private final IntegerField sequenceField = new IntegerField("Последовательность");
    private final NumberField effectiveWasteFactorField = new NumberField("Фактор брака");
    private final ComboBox<AbstractMaterials> selectedMaterialComboBox = new ComboBox<>("Выбранный материал");
    private final Checkbox switchOffAllowed = new Checkbox("Пользователь может отключить эту работу (uncheck - не может)", true);

    // Reusing EditableTextArea for custom formulas
    private final EditableTextArea<ProductOperation> customMachineTimeFormulaArea;
    private final EditableTextArea<ProductOperation> customActionFormulaArea;
    private final EditableTextArea<ProductOperation> customMaterialFormulaArea;

    // Layout for dynamic variable fields
    private final FormLayout variablesLayout = new FormLayout();
    
    // Services needed for populating ComboBoxes and EditableTextAreas
    private final OperationService operationService;
    private final AbstractMaterialService materialService;
    private final FormulasService formulasService;
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final TypeOfOperationService typeOfOperationService;

    public ProductOperationEditor(Consumer<Object> onSave,
                                  OperationService operationService,
                                  AbstractMaterialService materialService,
                                  FormulasService formulasService,
                                  VariablesForMainWorksService variablesForMainWorksService,
                                  TypeOfOperationService typeOfOperationService) {
        super(onSave);
        this.operationService = operationService;
        this.materialService = materialService;
        this.formulasService = formulasService;
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.typeOfOperationService = typeOfOperationService;

        // Initialize ComboBoxes
        operationComboBox.setItems(operationService.findAll());
        operationComboBox.setItemLabelGenerator(Operation::getName);

        selectedMaterialComboBox.setItems(materialService.findAll());
        selectedMaterialComboBox.setItemLabelGenerator(AbstractMaterials::getName);

        // Initialize EditableTextAreas
        customMachineTimeFormulaArea = new EditableTextArea<>("Формула времени оборудования",
                formulasService, typeOfOperationService, variablesForMainWorksService);
        customActionFormulaArea = new EditableTextArea<>("Формула времени работника",
                formulasService, typeOfOperationService, variablesForMainWorksService);
        customMaterialFormulaArea = new EditableTextArea<>("Формула расхода материала",
                formulasService, typeOfOperationService, variablesForMainWorksService);

        // Bind fields
        binder.forField(operationComboBox).bind(ProductOperation::getOperation, ProductOperation::setOperation);
        binder.forField(sequenceField).bind(ProductOperation::getSequence, ProductOperation::setSequence);
        binder.forField(effectiveWasteFactorField).bind(ProductOperation::getEffectiveWasteFactor, ProductOperation::setEffectiveWasteFactor);
        binder.forField(selectedMaterialComboBox).bind(ProductOperation::getSelectedMaterial, ProductOperation::setSelectedMaterial);
        binder.forField(switchOffAllowed).bind(ProductOperation::isSwitchOff, ProductOperation::setSwitchOff);
        binder.forField(customMachineTimeFormulaArea).bind(ProductOperation::getCustomMachineTimeFormula, ProductOperation::setCustomMachineTimeFormula);
        binder.forField(customActionFormulaArea).bind(ProductOperation::getCustomActionFormula, ProductOperation::setCustomActionFormula);
        binder.forField(customMaterialFormulaArea).bind(ProductOperation::getCustomMaterialFormula, ProductOperation::setCustomMaterialFormula);

        // Add components to the layout
        add(buildForm());
        addButtons();
    }

    @Override
    protected Component buildForm() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("120px", 2),
                new FormLayout.ResponsiveStep("240px", 3),
                new FormLayout.ResponsiveStep("360px", 4),
                new FormLayout.ResponsiveStep("480px", 5),
                new FormLayout.ResponsiveStep("600px", 6)
        );

        form.add(switchOffAllowed, 6);
        form.add(operationComboBox, 6);
        form.add(sequenceField, 2);
        form.add(effectiveWasteFactorField, 2);
        form.add(selectedMaterialComboBox, 2);
        form.add(customMachineTimeFormulaArea, 6);
        form.add(customActionFormulaArea, 6);
        form.add(customMaterialFormulaArea, 6);

        form.setWidthFull();
        return form;
    }

    @Override
    public void edit(ProductOperation entity) {
        super.edit(entity);
        populateVariableEditors(entity);
    }

    private void populateVariableEditors(ProductOperation entity) {
        // Очищаем layout и основной layout, где он находится
        variablesLayout.removeAll();
        remove(variablesLayout);

        List<Variable> variables = (entity != null && entity.getCustomVariables() != null)
                ? entity.getCustomVariables()
                : Collections.emptyList();

        if (!variables.isEmpty()) {
            add(new H4("Пользовательские переменные"));
            variablesLayout.setResponsiveSteps(
                    new FormLayout.ResponsiveStep("0", 1),
                    new FormLayout.ResponsiveStep("300px", 2),
                    new FormLayout.ResponsiveStep("600px", 3)
            );

            for (Variable variable : variables) {
                Component editor = createEditorForVariable(variable); // Создаем редактор для каждой переменной
                if (editor != null) {
                    variablesLayout.add(editor); // И добавляем его в layout
                }
            }
            add(variablesLayout);
        }
    }

    private Component createEditorForVariable(final Variable variable) {
        String label = (variable.getDescription() != null && !variable.getDescription().isEmpty())
                ? variable.getDescription()
                : variable.getKey();

        Binder<Variable> variableBinder = new Binder<>(Variable.class);
        Component valueEditor;

        switch (variable.getType()) {
            case INTEGER:
                IntegerField integerField = new IntegerField(label);
                var intBinding = variableBinder.forField(integerField);

                tryParseInt(variable.getMinValue()).ifPresent(min ->
                        intBinding.withValidator(v -> v == null || v >= min, "Значение меньше минимального: " + min));
                tryParseInt(variable.getMaxValue()).ifPresent(max ->
                        intBinding.withValidator(v -> v == null || v <= max, "Значение больше максимального: " + max));

                tryParseInt(variable.getStep()).ifPresent(step -> {
                    if (step > 0) {
                        integerField.setStep(step);
                        integerField.setHasControls(true); // Показываем кнопки +/-
                    }
                });

                intBinding.bind(v -> (Integer) v.getValueAsObject(), (v, val) -> v.setValue(val));
                valueEditor = integerField;
                break;
            case DOUBLE:
                NumberField numberField = new NumberField(label);
                var doubleBinding = variableBinder.forField(numberField);

                tryParseDouble(variable.getMinValue()).ifPresent(min ->
                        doubleBinding.withValidator(v -> v == null || v >= min, "Значение меньше минимального: " + min));
                tryParseDouble(variable.getMaxValue()).ifPresent(max ->
                        doubleBinding.withValidator(v -> v == null || v <= max, "Значение больше максимального: " + max));

                tryParseDouble(variable.getStep()).ifPresent(step -> {
                    if (step > 0) {
                        numberField.setStep(step);
                        numberField.setHasControls(true); // Показываем кнопки +/-
                    }
                });

                doubleBinding.bind(v -> (Double) v.getValueAsObject(), (v, val) -> v.setValue(val));
                valueEditor = numberField;
                break;
            case BOOLEAN:
                Checkbox checkbox = new Checkbox(label);
                variableBinder.forField(checkbox).bind(v -> (Boolean) v.getValueAsObject(), (v, val) -> v.setValue(val));
                valueEditor = checkbox;
                break;
            default: // STRING
                TextField textField = new TextField(label);
                var stringBinding = variableBinder.forField(textField);

                stringBinding.withValidator(new StringLengthValidator(
                        "Длина строки не соответствует ограничениям",
                        tryParseInt(variable.getMinValue()).orElse(null),
                        tryParseInt(variable.getMaxValue()).orElse(null)
                ));

                if (variable.getPattern() != null && !variable.getPattern().isBlank()) {
                    stringBinding.withValidator(new RegexpValidator("Значение не соответствует шаблону", variable.getPattern()));
                }

                stringBinding.bind(v -> (String) v.getValueAsObject(), (v, val) -> v.setValue(val));
                valueEditor = textField;
                break;
        }
        variableBinder.setBean(variable);

        // Чекбокс для управления свойством 'show'
        Checkbox showCheckbox = new Checkbox("Показывать");
        showCheckbox.setTooltipText("Разрешить пользователю изменять эту переменную в карточке продукта");
        showCheckbox.setValue(variable.isShow());
        showCheckbox.addValueChangeListener(event -> variable.setShow(event.getValue()));

        HorizontalLayout layout = new HorizontalLayout(valueEditor, showCheckbox);
        layout.setAlignItems(Alignment.BASELINE);
        return layout;
    }

    private java.util.Optional<Integer> tryParseInt(String s) {
        if (s == null || s.isBlank()) return java.util.Optional.empty();
        try {
            return java.util.Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return java.util.Optional.empty();
        }
    }

    private java.util.Optional<Double> tryParseDouble(String s) {
        if (s == null || s.isBlank()) return java.util.Optional.empty();
        try {
            // Использование NumberFormat для учета локали (точка или запятая)
            NumberFormat format = NumberFormat.getInstance(getLocale());
            Number number = format.parse(s);
            return java.util.Optional.of(number.doubleValue());
        } catch (java.text.ParseException e) {
            // Попытка прямой замены запятой на точку как запасной вариант
            try {
                return java.util.Optional.of(Double.parseDouble(s.replace(',', '.')));
            } catch (NumberFormatException ex) {
                return java.util.Optional.empty();
            }
        }
    }
}
