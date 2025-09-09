package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.EditableTextArea;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProductOperationEditor extends AbstractEditor<ProductOperation> {

    // UI Components
    private final ComboBox<Operation> operationComboBox = new ComboBox<>("Операция");
    private final IntegerField sequenceField = new IntegerField("Последовательность");
    private final NumberField effectiveWasteFactorField = new NumberField("Фактор брака");
    private final ComboBox<AbstractMaterials> selectedMaterialComboBox = new ComboBox<>("Выбранный материал");

    // Reusing EditableTextArea for custom formulas
    private final EditableTextArea<ProductOperation> customMachineTimeFormulaArea;
    private final EditableTextArea<ProductOperation> customActionFormulaArea;
    private final EditableTextArea<ProductOperation> customMaterialFormulaArea;

    // Reusing MapEditorView for custom variables
    private final MapEditorView mapEditorView;

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

        // Initialize MapEditorView
        mapEditorView = new MapEditorView(new ArrayList<>(), this::handleVariablesChange);

        // Bind fields
        binder.forField(operationComboBox).bind(ProductOperation::getOperation, ProductOperation::setOperation);
        binder.forField(sequenceField).bind(ProductOperation::getSequence, ProductOperation::setSequence);
        binder.forField(effectiveWasteFactorField).bind(ProductOperation::getEffectiveWasteFactor, ProductOperation::setEffectiveWasteFactor);
        binder.forField(selectedMaterialComboBox).bind(ProductOperation::getSelectedMaterial, ProductOperation::setSelectedMaterial);
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

        form.add(operationComboBox, 6);
        form.add(sequenceField, 2);
        form.add(effectiveWasteFactorField, 2);
        form.add(selectedMaterialComboBox, 2);
        form.add(customMachineTimeFormulaArea, 6);
        form.add(customActionFormulaArea, 6);
        form.add(customMaterialFormulaArea, 6);
        form.add(mapEditorView, 6);

        form.setWidthFull();
        return form;
    }

    @Override
    public void edit(ProductOperation entity) {
        super.edit(entity);
        // Update MapEditorView with the current entity's variables
        if (entity != null && entity.getCustomVariables() != null) {
            mapEditorView.setVariables(entity.getCustomVariables());
        } else {
            mapEditorView.setVariables(new ArrayList<>());
        }
    }

    private void handleVariablesChange(List<Variable> updatedVariables) {
        if (currentEntity != null) {
            currentEntity.setCustomVariables(updatedVariables);
        }
    }
}
