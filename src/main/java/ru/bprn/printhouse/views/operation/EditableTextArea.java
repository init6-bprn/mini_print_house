package ru.bprn.printhouse.views.operation;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.shared.Registration;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.dictionary.FormulasEditor;
import ru.bprn.printhouse.views.operation.service.FormulaEditor;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.FormulaDialog;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;

import java.util.Collections;
import java.util.List;

public class EditableTextArea<T> extends Composite<FormLayout.FormRow> implements HasValue<HasValue.ValueChangeEvent<String>, String> {

    private final TextArea area = new TextArea();
    private final Button selectButton = new Button("Выбрать");
    private final Button createButton = new Button(VaadinIcon.PLUS.create());
    private final Button editButton = new Button("Править");

    private final FormulasService formulasService;
    private final TypeOfOperationService worksService;
    private final FormulaValidationService formulaValidationService;
    private final ProductTypeVariableService productTypeVariableService;
    private final TemplateVariableService templateVariableService;


    private List<Variable> operationVariables = Collections.emptyList();

    public EditableTextArea(String label, FormulasService formulasService,
                            TypeOfOperationService worksService,
                            FormulaValidationService formulaValidationService,
                            ProductTypeVariableService productTypeVariableService,
                            TemplateVariableService templateVariableService) {
        this.formulasService = formulasService;
        this.worksService = worksService;
        this.formulaValidationService = formulaValidationService;
        this.productTypeVariableService = productTypeVariableService;
        this.templateVariableService = templateVariableService;

        area.setLabel(label);
        area.setHeight("80px");
        area.setReadOnly(true); // Поле всегда только для чтения
        setupButtons();
        setupLayout();
    }

    private void setupButtons() {
        selectButton.addClickListener(e -> openFormulaDialog());
        createButton.addClickListener(e -> openNewFormulaEditor());
        createButton.setTooltipText("Создать новую формулу в справочнике");
        editButton.addClickListener(e -> openFormulaEditor());

        selectButton.getElement().getStyle().set("align-self", "baseline");
        editButton.getElement().getStyle().set("align-self", "baseline");
        createButton.getElement().getStyle().set("align-self", "baseline");
        area.getElement().getStyle().set("align-self", "baseline");
    }

    private void setupLayout() {
        FormLayout.FormRow row = getContent();
        //FormLayout.FormRow row = new FormLayout.FormRow();
        row.add(selectButton, 1);
        row.add(createButton, 1);
        row.add(area, 3);
        row.add(editButton, 1);
    }


    private void openFormulaDialog() {
        new FormulaDialog(formulasService, worksService, formula -> {
            setValue(formula.getFormula());
            Notification.show("Вы выбрали: " + formula.getName());
        }).open();
    }

    private void openFormulaEditor() {
        new FormulaEditor(getValue(), this::setValue, operationVariables,
                formulaValidationService, worksService, productTypeVariableService, templateVariableService).open();
    }

    private void openNewFormulaEditor() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Создание новой формулы в справочнике");
        dialog.setModal(true);
        dialog.setDraggable(true);

        FormulasEditor editor = new FormulasEditor(
                // Этот callback будет вызван при нажатии "Save" в редакторе
                savedObject -> {
                    // Мы не вызываем saveFormula из FormulasDictionary, а обрабатываем все здесь.
                    handleNewFormulaSave(savedObject, dialog);
                },
                worksService, formulaValidationService,
                productTypeVariableService, templateVariableService
        );
        editor.edit(new ru.bprn.printhouse.data.entity.Formulas());
        dialog.add(editor);
        dialog.open();
    }

    /**
     * Этот метод вызывается, когда новая формула успешно сохранена в FormulasEditor.
     * @param savedObject Сохраненный объект Formulas.
     */
    private void handleNewFormulaSave(Object savedObject, Dialog dialog) {
        if (savedObject instanceof ru.bprn.printhouse.data.entity.Formulas newFormula) {
            if (newFormula.getName() != null && !newFormula.getName().isBlank()) {
                formulasService.save(newFormula); // Сохраняем в базу через сервис
                setValue(newFormula.getFormula()); // Устанавливаем текст новой формулы в наше поле
                Notification.show("Новая формула '" + newFormula.getName() + "' создана и применена.", 3000, Notification.Position.MIDDLE);
                dialog.close();
            } else {
                // Если имя пустое, просто закрываем диалог без сохранения
                dialog.close();
            }
        }
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

    public void setEnabled(boolean enabled) {
        area.setEnabled(enabled);
        selectButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
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

    public void setVariableContext(List<Variable> operationVariables) {
        this.operationVariables = operationVariables != null ? operationVariables : Collections.emptyList();
    }
}
