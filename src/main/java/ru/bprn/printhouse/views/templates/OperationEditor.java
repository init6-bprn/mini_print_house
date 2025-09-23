package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.machine.service.AbstractMachineService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.EditableTextArea;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.*;
import java.util.function.Consumer;

public class OperationEditor extends AbstractEditor<Operation> {

    private final TextField name = new TextField("Название шаблона:");
    private final Select<TypeOfOperation> typeOfOperationSelect = new Select<>();
    private final Checkbox haveMachine = new Checkbox("Есть оборудование (uncheck - нет)", true);
    private final Select<AbstractMachine> machineSelect = new Select<>();
    private final EditableTextArea<Operation> equipmentFormula;
    private final Checkbox haveWorker = new Checkbox("Есть работа(работник) (uncheck - нет)", true);
    private final EditableTextArea<Operation> workerFormula;
    private final Checkbox haveMaterial = new Checkbox("Есть расходный материал (uncheck - нет)", true);
    private final Select<AbstractMaterials> defaultMaterial = new Select<>("Материал по умолчанию", e->{});
    private final MultiSelectComboBox<AbstractMaterials> selectedMaterials = new MultiSelectComboBox<>("Выбранные материалы");
    private final EditableTextArea<Operation> materialFormula;
    private final EditableTextArea<Operation> operationWasteFormula;
    private final EditableTextArea<Operation> setupWasteFormula;

    private final MapEditorView mapEditorView;



    private final TypeOfOperationService typeOfOperationService;
    @NotNull
    private final AbstractMaterialService materialService;
    @NotNull
    private final OperationService operationService;

    public OperationEditor(Operation operation, Consumer<Object> onSave,
                           TypeOfOperationService typeOfOperationService, AbstractMaterialService materialService,
                           FormulasService formulasService, FormulaValidationService formulaValidationService, ProductTypeVariableService productTypeVariableService,
                           AbstractMachineService abstractMachineService, OperationService operationService) { //
        super(onSave);
        this.operationService = operationService;
        this.typeOfOperationService = typeOfOperationService;
        this.materialService = materialService;
        typeOfOperationSelect.setItems(typeOfOperationService.findAll());

        equipmentFormula = new EditableTextArea<Operation>("Формула времени оборудования", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService);

        workerFormula = new EditableTextArea<Operation>("Формула времени работника", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService);

        materialFormula = new EditableTextArea<Operation>("Формула расхода материала", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService);

        operationWasteFormula = new EditableTextArea<>("Формула брака операции", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService);

        setupWasteFormula = new EditableTextArea<>("Формула приладки", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService);

        selectedMaterials.setItemLabelGenerator(AbstractMaterials::getName);

        defaultMaterial.setItemLabelGenerator(AbstractMaterials::getName);

        typeOfOperationSelect.setLabel("Тип работы:");
        machineSelect.setLabel("Выберите устройство:");
        machineSelect.setItemLabelGenerator(AbstractMachine::getName);
        machineSelect.setItems(abstractMachineService.findAll());

        machineSelect.addValueChangeListener(event -> {
            AbstractMachine selectedMachine = event.getValue();
            if (selectedMachine != null) {
                // Устанавливаем доступные материалы из выбранной машины
                Set<AbstractMaterials> materials = selectedMachine.getAbstractMaterials();
                selectedMaterials.setItems(materials);
            } else {
                selectedMaterials.setItems(Collections.emptySet());
            }
        });

        mapEditorView = new MapEditorView(operation != null ? operation.getVariables() : new ArrayList<>(), this::handleVariablesChange);

        this.binder.forField(name).bind(Operation::getName, Operation::setName);
        this.binder.forField(typeOfOperationSelect).bind(Operation::getTypeOfOperation, Operation::setTypeOfOperation);
        this.binder.forField(haveMachine).bind(Operation::isHaveMachine, Operation::setHaveMachine);
        this.binder.forField(machineSelect).bind(Operation::getAbstractMachine, Operation::setAbstractMachine);
        this.binder.forField(haveWorker).bind(Operation::isHaveAction, Operation::setHaveAction);
        this.binder.forField(haveMaterial).bind(Operation::isHaveMaterial, Operation::setHaveMaterial);
        this.binder.forField(selectedMaterials).bind(Operation::getListOfMaterials, Operation::setListOfMaterials);
        this.binder.forField(defaultMaterial).bind(Operation::getDefaultMaterial, Operation::setDefaultMaterial);

        this.binder.forField(equipmentFormula).bind(stringVariableProvider("machineTimeFormula"), stringVariableSetter("machineTimeFormula"));
        this.binder.forField(workerFormula).bind(stringVariableProvider("actionFormula"), stringVariableSetter("actionFormula"));
        this.binder.forField(materialFormula).bind(stringVariableProvider("materialFormula"), stringVariableSetter("materialFormula"));
        this.binder.forField(operationWasteFormula).bind(stringVariableProvider("operationWasteFormula"), stringVariableSetter("operationWasteFormula"));
        this.binder.forField(setupWasteFormula).bind(stringVariableProvider("setupWasteFormula"), stringVariableSetter("setupWasteFormula"));

        selectedMaterials.addValueChangeListener(event -> {
            Set<AbstractMaterials> selected = event.getValue();
            defaultMaterial.setItems(selected);
            // Если ранее выбранный материал по умолчанию отсутствует в новом списке,
            // или если список стал пустым, очищаем поле "Материал по умолчанию".
            if (defaultMaterial.getValue() != null && (selected == null || !selected.contains(defaultMaterial.getValue()))) {
                defaultMaterial.clear();
            }
        });

        haveMachine.addValueChangeListener(e->{
            boolean selector = e.getValue();
            if (this.binder.getBean() != null) {
                machineSelect.setValue(selector ? machineSelect.getValue() : null);
                machineSelect.setEnabled(selector);
                if (!selector) stringVariableSetter("machineTimeFormula").accept(this.binder.getBean(), "");
                equipmentFormula.setEnabled(selector);
            }
        });

        haveWorker.addValueChangeListener(e->{
            boolean selector = e.getValue();
            if (this.binder.getBean() != null) {
                if (!selector) stringVariableSetter("actionFormula").accept(this.binder.getBean(), "");
                workerFormula.setEnabled(selector);
            }
        });

        haveMaterial.addValueChangeListener(e->{
            boolean selector = e.getValue();
            if (this.binder.getBean() != null) {
                materialFormula.setEnabled(selector);
                defaultMaterial.setValue(selector ? defaultMaterial.getValue() : null);
                defaultMaterial.setEnabled(selector);
                selectedMaterials.setEnabled(selector);
                if (!selector) stringVariableSetter("materialFormula").accept(this.binder.getBean(), "");
            }
        });

        if (operation != null) this.edit(operation);
        add(buildForm());
        addButtons();

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
        row1.add(name, 6);
        var row2 = new FormLayout.FormRow();
        row2.add(typeOfOperationSelect,6);
        var row3 = new FormLayout.FormRow();
        row3.add(haveMachine, 6);
        row3.add(machineSelect, 6);
        var row5 = new FormLayout.FormRow();
        row5.add(haveWorker, 6);
        var row6 = new FormLayout.FormRow();
        row6.add(haveMaterial, 6);
        row6.add(selectedMaterials, 3);
        row6.add(defaultMaterial, 3);
        var row8 = new FormLayout.FormRow();
        row8.add(mapEditorView, 6);

        form.add(row1, row2, row3, equipmentFormula, row5, workerFormula, row6, materialFormula,
                operationWasteFormula, setupWasteFormula, row8);
        form.setExpandColumns(true);
        form.setWidthFull();


        return form;
    }

    public void editor(Operation operation) {
        this.clear();
        if (operation != null) {
            // 1. Устанавливаем список доступных материалов ДО привязки биндера
            if (operation.getAbstractMachine() != null) {
                selectedMaterials.setItems(operation.getAbstractMachine().getAbstractMaterials());
            } else {
                selectedMaterials.setItems(Collections.emptySet());
            }
            // 2. Устанавливаем уже выбранные материалы
            if (operation.getListOfMaterials() != null && !operation.getListOfMaterials().isEmpty()) {
                selectedMaterials.setValue(operation.getListOfMaterials());
                defaultMaterial.setItems(operation.getListOfMaterials());
            }
        }
        edit(operation);

        // Передаем контекст переменных в EditableTextArea
        List<Variable> operationVariables = binder.getBean() != null ? binder.getBean().getVariables() : new ArrayList<>();
        equipmentFormula.setVariableContext(operationVariables);
        workerFormula.setVariableContext(operationVariables);
        materialFormula.setVariableContext(operationVariables);
        operationWasteFormula.setVariableContext(operationVariables);
        setupWasteFormula.setVariableContext(operationVariables);
        mapEditorView.setVariables(binder.getBean().getVariables());
    }

    private void handleVariablesChange(List<Variable> updatedVariables) {
        // Просто сохраняем обновленный список
        this.binder.getBean().setVariables(updatedVariables);
        System.out.println("Список переменных обновлен, всего: " + updatedVariables.size());
    }

    private Optional<Variable> getVariable(Operation operation, String key) {
        if (operation == null || operation.getVariables() == null) {
            return Optional.empty();
        }
        return operation.getVariables().stream()
                .filter(v -> key.equals(v.getKey()))
                .findFirst();
    }

    private com.vaadin.flow.function.ValueProvider<Operation, String> stringVariableProvider(String key) {
        return operation -> getVariable(operation, key).map(Variable::getValue).orElse("");
    }

    private com.vaadin.flow.data.binder.Setter<Operation, String> stringVariableSetter(String key) {
        return (operation, value) -> getVariable(operation, key).ifPresent(v -> v.setValue(value));
    }

}
