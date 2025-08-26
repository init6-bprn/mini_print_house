package ru.bprn.printhouse.views.operation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.operation.service.FormulaEditor;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.FormulaDialog;

public class EditableTextArea<T> extends Composite<FormLayout.FormRow> implements HasValue<HasValue.ValueChangeEvent<String>, String> {

    private final TextArea area = new TextArea();
    private final Button selectButton = new Button("Выбрать");
    private final Button editButton = new Button("Править");

    private final FormulasService formulasService;
    private final TypeOfOperationService worksService;
    private final VariablesForMainWorksService variablesService;

    public EditableTextArea(String label, FormulasService formulasService,
                            TypeOfOperationService worksService,
                            VariablesForMainWorksService variablesService) {
        this.formulasService = formulasService;
        this.worksService = worksService;
        this.variablesService = variablesService;

        area.setLabel(label);
        setupButtons();
        setupLayout();
    }

    private void setupButtons() {
        selectButton.addClickListener(e -> openFormulaDialog());
        editButton.addClickListener(e -> openFormulaEditor());

        selectButton.getElement().getStyle().set("align-self", "baseline");
        editButton.getElement().getStyle().set("align-self", "baseline");
        area.getElement().getStyle().set("align-self", "baseline");
    }

    private void setupLayout() {
        FormLayout.FormRow row = getContent();
        //FormLayout.FormRow row = new FormLayout.FormRow();
        row.add(selectButton, 1);
        row.add(area, 4);
        row.add(editButton, 1);
    }


    private void openFormulaDialog() {
        new FormulaDialog(formulasService, worksService, formula -> {
            setValue(formula.getFormula());
            Notification.show("Вы выбрали: " + formula.getName());
        }).open();
    }

    private void openFormulaEditor() {
        new FormulaEditor(getValue(), this::setValue, worksService, variablesService).open();
    }

    // === Реализация HasValue ===
    @Override
    public void setValue(String value) {
        if (value == null) area.setValue("");
        else area.setValue(value);
    }

    @Override
    public String getValue() {
        return area.getValue();
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super ValueChangeEvent<String>> valueChangeListener) {
        return area.addValueChangeListener(valueChangeListener);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        area.setReadOnly(readOnly);
        selectButton.setEnabled(!readOnly);
        editButton.setEnabled(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return area.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        area.setRequiredIndicatorVisible(required);
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return area.isRequiredIndicatorVisible();
    }

    // === Дополнительные методы ===
    public void setLabel(String label) {
        area.setLabel(label);
    }

    public String getLabel() {
        return area.getLabel();
    }
}
