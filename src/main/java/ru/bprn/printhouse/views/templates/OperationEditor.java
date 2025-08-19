package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;

import java.util.List;
import java.util.function.Consumer;

public class OperationEditor extends AbstractEditor<Operation> {

    private final TextField name = new TextField("Название шаблона:");
    private final Select<TypeOfOperation> typeOfOperationSelect = new Select<>();
    private final Checkbox switchOn = new Checkbox("Пользователь может отключить эту работу (uncheck - не может)", true);
    private final Checkbox haveMachine = new Checkbox("Есть оборудование (uncheck - нет)", true);
    private final Select<AbstractMachine> machineSelect = new Select<>();
    private final TextField machineFormula = new TextField("Формула расчета времени работы оборудования");
    private final Checkbox haveWorker = new Checkbox("Есть работа(работник) (uncheck - нет)", true);
    private final TextField workerFormula = new TextField("Формула расчета времени работы работника");
    private final Checkbox haveMaterial = new Checkbox("Есть расходный материал (uncheck - нет)", true);
    private final Select<AbstractMaterials> defaultMaterial = new Select<>("Материал по умолчанию", e->{});
    private final MultiSelectComboBox<AbstractMaterials> selectedMaterials = new MultiSelectComboBox<>("Выбранные материалы");
    private final TextField materialFormula = new TextField("Формула расчета количества расходных материалов");
    private final MapEditorView mapEditorView = new MapEditorView();
    private FormulaDialog formulaDialog;
    private final Button getMaterialButton = new Button("Выбрать", e->{
        formulaDialog.open();
    });


    private final TypeOfOperationService typeOfOperationService;

    public OperationEditor(Operation operation, Consumer<Object> onSave,
                           TypeOfOperationService typeOfOperationService, AbstractMaterialService materialService,
                           FormulasService formulasService, VariablesForMainWorksService variablesForMainWorksService) {
        super(onSave);
        this.typeOfOperationService = typeOfOperationService;
        typeOfOperationSelect.setItems(typeOfOperationService.findAll());

        getMaterialButton.setAriaLabel("Выбор формулы");

        selectedMaterials.setItems(materialService.findAll());
        selectedMaterials.setItemLabelGenerator(AbstractMaterials::getName);

        defaultMaterial.setItemLabelGenerator(AbstractMaterials::getName);
        if (operation != null && operation.getListOfMaterials() != null) setElements();

        typeOfOperationSelect.setLabel("Тип работы:");
        machineSelect.setLabel("Выберите устройство:");

        this.binder.forField(name).bind(Operation::getName, Operation::setName);
        this.binder.forField(typeOfOperationSelect).bind(Operation::getTypeOfOperation, Operation::setTypeOfOperation);
        this.binder.forField(switchOn).bind(Operation::isSwitchOff, Operation::setSwitchOff);
        this.binder.forField(haveMachine).bind(Operation::isHaveMachine, Operation::setHaveMachine);
        this.binder.forField(machineSelect).bind(Operation::getAbstractMachine, Operation::setAbstractMachine);
        this.binder.forField(machineFormula).bind(Operation::getMachineTimeFormula, Operation::setMachineTimeFormula);
        this.binder.forField(haveWorker).bind(Operation::isHaveAction, Operation::setHaveAction);
        this.binder.forField(haveMaterial).bind(Operation::isHaveMaterial, Operation::setHaveMaterial);
        this.binder.forField(selectedMaterials).bind(Operation::getListOfMaterials, Operation::setListOfMaterials);
        this.binder.forField(defaultMaterial).bind(Operation::getDefaultMaterial, Operation::setDefaultMaterial);
        this.binder.forField(workerFormula).bind(Operation::getActionFormula, Operation::setActionFormula);
        this.binder.forField(materialFormula).bind(Operation::getMatFormula, Operation::setMaterialFormula);

        formulaDialog = new FormulaDialog(formulasService, variablesForMainWorksService,
                typeOfOperationService, selectedFormula -> {
            Notification.show("Вы выбрали: " + selectedFormula);
            machineFormula.setValue(selectedFormula.getFormula());
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
        var row1_1 = new FormLayout.FormRow();
        row1_1.add(switchOn, 6);
        var row2 = new FormLayout.FormRow();
        row2.add(typeOfOperationSelect,6);
        var row3 = new FormLayout.FormRow();
        row3.add(haveMachine, 6);
        row3.add(machineSelect, 6);
        var row4 = new FormLayout.FormRow();
        getMaterialButton.getElement().getStyle().set("align-self", "baseline");
        machineFormula.getElement().getStyle().set("align-self", "baseline");
        row4.add(machineFormula, 5);
        row4.add(getMaterialButton,1);
        var row5 = new FormLayout.FormRow();
        row5.add(haveWorker, 6);
        row5.add(workerFormula, 6);
        var row6 = new FormLayout.FormRow();
        row6.add(haveMaterial, 6);
        row6.add(selectedMaterials, 3);
        row6.add(defaultMaterial, 3);
        var row7 = new FormLayout.FormRow();
        row7.add(materialFormula, 6);
        var row8 = new FormLayout.FormRow();
        row8.add(mapEditorView, 6);

        form.add(row1, row1_1, row2, row3, row4, row5, row6, row7, row8);
        form.setExpandColumns(true);
        form.setWidthFull();


        return form;
    }

    private void setElements() {
        defaultMaterial.setItems(binder.getBean().getListOfMaterials());
        mapEditorView.setMap(binder.getBean().getVariables());
    }

    public void edit(Operation entity) {
        this.currentEntity = entity;
        binder.setBean(entity);
        setElements();
    }
}
