package ru.bprn.printhouse.views.machine.printers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.ValueProvider;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.PrintingMaterialService;
import ru.bprn.printhouse.views.templates.AbstractEditor;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.Optional;
import java.util.function.Consumer;

public class DigitalPrintingMachineEditor extends AbstractEditor<DigitalPrintingMachine> {

    private final PrintingMaterialService materialService;

    private final TextField name = new TextField("Название");
    private final MultiSelectComboBox<AbstractMaterials> materials = new MultiSelectComboBox<>("Материалы для печати");
    private final NumberField gapTop = new NumberField("Верхнее непечатное поле, мм");
    private final NumberField gapBottom = new NumberField("Нижнее непечатное поле, мм");
    private final NumberField gapLeft = new NumberField("Левое непечатное поле, мм");
    private final NumberField gapRight = new NumberField("Правое непечатное поле, мм");
    private final NumberField maxLength = new NumberField("Макс. длина листа, мм");
    private final NumberField maxWidth = new NumberField("Макс. ширина листа, мм");
    private final NumberField clickSize = new NumberField("Длина клика, мм");

    public DigitalPrintingMachineEditor(DigitalPrintingMachine machine, Consumer<Object> onSave, PrintingMaterialService materialService) {
        super(onSave);
        this.materialService = materialService;

        add(buildForm());
        addButtons();

        if (machine != null) {
            edit(machine);
        }
    }

    @Override
    protected Component buildForm() {
        binder.forField(name).asRequired().bind(DigitalPrintingMachine::getName, DigitalPrintingMachine::setName);

        // Bindings for variables
        addBindingWithValidation(gapTop, "gap_top");
        addBindingWithValidation(gapBottom, "gap_bottom");
        addBindingWithValidation(gapLeft, "gap_left");
        addBindingWithValidation(gapRight, "gap_right");
        addBindingWithValidation(maxLength, "max_length");
        addBindingWithValidation(maxWidth, "max_width");
        addBindingWithValidation(clickSize, "click_size");

        materials.setSelectedItemsOnTop(true);
        materials.setItems(materialService.findAllAsAbstract());
        binder.forField(materials).bind(DigitalPrintingMachine::getAbstractMaterials, DigitalPrintingMachine::setAbstractMaterials);

        FormLayout formLayout = new FormLayout();
        formLayout.add(name, maxLength, maxWidth, clickSize, gapTop, gapBottom, gapLeft, gapRight, materials);
        return formLayout;
    }

    @Override
    public void edit(DigitalPrintingMachine entity) {
        super.edit(entity);
        if (entity != null) {
            materials.setValue(entity.getAbstractMaterials());
            updateFieldSettings(entity);
        }
    }

    private void addBindingWithValidation(NumberField field, String key) {
        binder.forField(field).bind(variableProvider(key), variableSetter(key));
    }

    private void updateFieldSettings(DigitalPrintingMachine machine) {
        updateSingleField(gapTop, "gap_top", machine);
        updateSingleField(gapBottom, "gap_bottom", machine);
        updateSingleField(gapLeft, "gap_left", machine);
        updateSingleField(gapRight, "gap_right", machine);
        updateSingleField(maxLength, "max_length", machine);
        updateSingleField(maxWidth, "max_width", machine);
        updateSingleField(clickSize, "click_size", machine);
    }

    private void updateSingleField(NumberField field, String key, DigitalPrintingMachine machine) {
        getVariable(machine, key).ifPresent(variable -> {
            tryParseDouble(variable.getMinValue()).ifPresentOrElse(field::setMin, () -> field.setMin(Double.NEGATIVE_INFINITY));
            tryParseDouble(variable.getMaxValue()).ifPresentOrElse(field::setMax, () -> field.setMax(Double.POSITIVE_INFINITY));
            tryParseDouble(variable.getStep()).ifPresent(step -> {
                field.setStep(step > 0 ? step : 1);
                field.setStepButtonsVisible(step > 0);
            });
        });
    }

    private ValueProvider<DigitalPrintingMachine, Double> variableProvider(String key) {
        return machine -> getVariable(machine, key).map(v -> (Double) v.getValueAsObject()).orElse(0.0);
    }

    private com.vaadin.flow.data.binder.Setter<DigitalPrintingMachine, Double> variableSetter(String key) {
        return (machine, value) -> getVariable(machine, key).ifPresent(v -> v.setValue(String.valueOf(value)));
    }

    private Optional<Variable> getVariable(DigitalPrintingMachine machine, String key) {
        if (machine == null || machine.getVariables() == null) return Optional.empty();
        return machine.getVariables().stream().filter(v -> key.equals(v.getKey())).findFirst();
    }

    private Optional<Double> tryParseDouble(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(Double.parseDouble(s.replace(',', '.')));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}