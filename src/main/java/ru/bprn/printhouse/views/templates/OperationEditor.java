package ru.bprn.printhouse.views.templates;

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
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.EditableTextArea;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import com.vaadin.flow.component.Component;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.*;
import java.util.function.Consumer;

public class OperationEditor extends AbstractEditor<Operation> {

    private final TextField name = new TextField("Название шаблона:");
    private final Select<TypeOfOperation> typeOfOperationSelect = new Select<>();
    private final Select<AbstractMachine> machineSelect = new Select<>();
    private final EditableTextArea<Operation> equipmentFormula;
    private final EditableTextArea<Operation> workerFormula;
    private final Select<AbstractMaterials> defaultMaterial = new Select<>("Материал по умолчанию", e->{});
    private final MultiSelectComboBox<AbstractMaterials> selectedMaterials = new MultiSelectComboBox<>("Выбранные материалы");
    private final EditableTextArea<Operation> materialFormula;
    private final EditableTextArea<Operation> wasteFormula;
    private final EditableTextArea<Operation> setupWasteFormula;

    private final MapEditorView mapEditorView;



    private final TypeOfOperationService typeOfOperationService;
    @NotNull
    private final AbstractMaterialService materialService;
    @NotNull
    private final OperationService operationService;

    public OperationEditor(Operation operation, Consumer<Object> onSave,
                           TypeOfOperationService typeOfOperationService, AbstractMaterialService materialService,
                           FormulasService formulasService, FormulaValidationService formulaValidationService, ProductTypeVariableService productTypeVariableService, TemplateVariableService templateVariableService,
                           AbstractMachineService abstractMachineService, OperationService operationService) { //
        super(onSave);
        this.operationService = operationService;
        this.typeOfOperationService = typeOfOperationService;
        this.materialService = materialService;
        typeOfOperationSelect.setItems(typeOfOperationService.findAll());
        
        equipmentFormula = new EditableTextArea<>("Формула времени оборудования", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService, templateVariableService);

        workerFormula = new EditableTextArea<>("Формула времени работника", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService, templateVariableService);

        materialFormula = new EditableTextArea<>("Формула расхода материала", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService, templateVariableService);

        wasteFormula = new EditableTextArea<>("Формула брака операции", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService, templateVariableService);

        setupWasteFormula = new EditableTextArea<>("Формула приладки", formulasService,
                typeOfOperationService, formulaValidationService, productTypeVariableService, templateVariableService);

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
        this.binder.forField(machineSelect).bind(Operation::getAbstractMachine, Operation::setAbstractMachine);
        this.binder.forField(selectedMaterials).bind(Operation::getListOfMaterials, Operation::setListOfMaterials);
        this.binder.forField(defaultMaterial).bind(Operation::getDefaultMaterial, Operation::setDefaultMaterial);

        this.binder.forField(equipmentFormula).bind(Operation::getMachineTimeExpression, Operation::setMachineTimeExpression);
        this.binder.forField(workerFormula).bind(Operation::getActionTimeExpression, Operation::setActionTimeExpression);
        this.binder.forField(materialFormula).bind(Operation::getMaterialAmountExpression, Operation::setMaterialAmountExpression);
        this.binder.forField(wasteFormula).bind(Operation::getWasteExpression, Operation::setWasteExpression);
        this.binder.forField(setupWasteFormula).bind(Operation::getSetupExpression, Operation::setSetupExpression);

        selectedMaterials.addValueChangeListener(event -> {
            Set<AbstractMaterials> selected = event.getValue();
            defaultMaterial.setItems(selected);
            // Если ранее выбранный материал по умолчанию отсутствует в новом списке,
            // или если список стал пустым, очищаем поле "Материал по умолчанию".
            if (defaultMaterial.getValue() != null && (selected == null || !selected.contains(defaultMaterial.getValue()))) {
                defaultMaterial.clear();
            }
        });

        if (operation != null) this.editOperation(operation);
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
        row2.add(typeOfOperationSelect, 6);
        var row3 = new FormLayout.FormRow();
        row3.add(machineSelect, 6);
        var row6 = new FormLayout.FormRow();
        row6.add(selectedMaterials, 3);
        row6.add(defaultMaterial, 3);
        var row8 = new FormLayout.FormRow();
        row8.add(mapEditorView, 6);

        form.add(row1, row2, row3, equipmentFormula, workerFormula, row6, materialFormula,
                wasteFormula, setupWasteFormula, row8);
        form.setExpandColumns(true);
        form.setWidthFull();


        return form;
    }

    public void editOperation(Operation operation) {
        this.clear();

        if (operation != null) {
            AbstractMachine machine = operation.getAbstractMachine();
            Set<AbstractMaterials> availableMaterials = (machine != null)
                    ? machine.getAbstractMaterials()
                    : Collections.emptySet();

            // 1. Устанавливаем доступные материалы для MultiSelectComboBox
            selectedMaterials.setItems(availableMaterials);

            // 2. Устанавливаем доступные материалы для Select (материал по умолчанию)
            // Это должны быть уже ВЫБРАННЫЕ материалы для данной операции.
            Set<AbstractMaterials> selectedOpMaterials = operation.getListOfMaterials();
            if (selectedOpMaterials != null && !selectedOpMaterials.isEmpty()) {
                defaultMaterial.setItems(selectedOpMaterials);
            } else {
                defaultMaterial.setItems(Collections.emptySet());
            }
        } else {
            // Очищаем списки, если редактируется null (например, при создании)
            selectedMaterials.setItems(Collections.emptySet());
            defaultMaterial.setItems(Collections.emptySet());
        }
        // 3. Теперь, когда все списки заполнены, привязываем бин. Binder установит значения.
        edit(operation);

        // Передаем контекст переменных в EditableTextArea
        List<Variable> operationVariables = binder.getBean() != null ? binder.getBean().getVariables() : new ArrayList<>();
        equipmentFormula.setVariableContext(operationVariables);
        workerFormula.setVariableContext(operationVariables);
        materialFormula.setVariableContext(operationVariables);
        wasteFormula.setVariableContext(operationVariables);
        setupWasteFormula.setVariableContext(operationVariables);
        mapEditorView.setVariables(binder.getBean().getVariables());
    }

    private void handleVariablesChange(List<Variable> updatedVariables) {
        if (binder.getBean() != null) {
            binder.getBean().setVariables(updatedVariables);
        }
    }

}
