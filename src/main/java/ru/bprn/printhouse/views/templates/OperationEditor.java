package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.jetbrains.annotations.NotNull;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.machine.service.AbstractMachineService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.EditableTextArea;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final MapEditorView mapEditorView;



    private final TypeOfOperationService typeOfOperationService;
    @NotNull
    private final AbstractMaterialService materialService;

    public OperationEditor(Operation operation, Consumer<Object> onSave,
                           TypeOfOperationService typeOfOperationService, AbstractMaterialService materialService,
                           FormulasService formulasService, VariablesForMainWorksService variablesForMainWorksService,
                           AbstractMachineService abstractMachineService) {
        super(onSave);
        this.typeOfOperationService = typeOfOperationService;
        this.materialService = materialService;
        typeOfOperationSelect.setItems(typeOfOperationService.findAll());

        equipmentFormula = new EditableTextArea<>("Формула расчета времени работы оборудования",
                formulasService, typeOfOperationService, variablesForMainWorksService);

        workerFormula = new EditableTextArea<>("Формула расчета времени работы работника",
                formulasService, typeOfOperationService, variablesForMainWorksService);

        materialFormula = new EditableTextArea<>("Формула расчета количества расходных материалов",
                formulasService, typeOfOperationService, variablesForMainWorksService);

        selectedMaterials.setItems(materialService.findAll());
        selectedMaterials.setItemLabelGenerator(AbstractMaterials::getName);

        defaultMaterial.setItemLabelGenerator(AbstractMaterials::getName);
        if (operation != null && operation.getListOfMaterials() != null) defaultMaterial.setItems();

        typeOfOperationSelect.setLabel("Тип работы:");
        machineSelect.setLabel("Выберите устройство:");
        machineSelect.setItemLabelGenerator(AbstractMachine::getName);
        machineSelect.setItems(abstractMachineService.findAll());

        mapEditorView = new MapEditorView(operation != null ? operation.getVariables() : new ArrayList<>(), this::handleVariablesChange);

        this.binder.forField(name).bind(Operation::getName, Operation::setName);
        this.binder.forField(typeOfOperationSelect).bind(Operation::getTypeOfOperation, Operation::setTypeOfOperation);
        this.binder.forField(haveMachine).bind(Operation::isHaveMachine, Operation::setHaveMachine);
        this.binder.forField(machineSelect).bind(Operation::getAbstractMachine, Operation::setAbstractMachine);
        this.binder.forField(haveWorker).bind(Operation::isHaveAction, Operation::setHaveAction);
        this.binder.forField(haveMaterial).bind(Operation::isHaveMaterial, Operation::setHaveMaterial);
        this.binder.forField(selectedMaterials).bind(Operation::getListOfMaterials, Operation::setListOfMaterials);
        this.binder.forField(defaultMaterial).bind(Operation::getDefaultMaterial, Operation::setDefaultMaterial);
        this.binder.forField(equipmentFormula).bind(Operation::getMachineTimeFormula, Operation::setMachineTimeFormula);
        this.binder.forField(workerFormula).bind(Operation::getActionFormula, Operation::setActionFormula);
        this.binder.forField(materialFormula).bind(Operation::getMaterialFormula, Operation::setMaterialFormula);

        selectedMaterials.addValueChangeListener(event -> {
            Set<AbstractMaterials> selected = event.getValue();
            defaultMaterial.setItems(selected);
            if (selected != null && !selected.isEmpty() && !selected.contains(defaultMaterial.getValue())) {
                defaultMaterial.clear(); // Только если новый набор не пустой
            }
        });

        haveMachine.addValueChangeListener(e->{
            boolean selector = e.getValue();
            if (this.binder.getBean() != null) {
                machineSelect.setValue(selector ? machineSelect.getValue() : null);
                machineSelect.setEnabled(selector);
                if (!selector) this.binder.getBean().setMachineTimeFormula("");
                equipmentFormula.setEnabled(selector);
            }
        });

        haveWorker.addValueChangeListener(e->{
            boolean selector = e.getValue();
            if (this.binder.getBean() != null) {
                if (!selector) this.binder.getBean().setActionFormula("");
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
                if (!selector) this.binder.getBean().setMaterialFormula("");
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

        form.add(row1, row2, row3, equipmentFormula, row5, workerFormula, row6, materialFormula, row8);
        form.setExpandColumns(true);
        form.setWidthFull();


        return form;
    }

    public void editor(Operation operation) {
        this.clear();
        if (!operation.getListOfMaterials().isEmpty()) {
            selectedMaterials.setValue(operation.getListOfMaterials());
            defaultMaterial.setItems(operation.getListOfMaterials());
        }
        edit(operation);
        mapEditorView.setVariables(binder.getBean().getVariables());
    }

    private void handleVariablesChange(List<Variable> updatedVariables) {
        // Просто сохраняем обновленный список
        this.binder.getBean().setVariables(updatedVariables);
        System.out.println("Список переменных обновлен, всего: " + updatedVariables.size());
    }

}
