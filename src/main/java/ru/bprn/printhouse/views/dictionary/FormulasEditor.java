package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.FormulaEditor;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.AbstractEditor;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;

import java.util.List;
import java.util.function.Consumer;

public class FormulasEditor extends AbstractEditor<Formulas> {

    private final TextField name = new TextField("Название формулы");
    private final TextArea description = new TextArea("Описание");
    private final Select<TypeOfOperation> typeOfOperationSelect = new Select<>();
    private final TextArea formulaArea = new TextArea("Формула");
    private final Button openEditorButton = new Button("Редактор", VaadinIcon.EDIT.create());

    private final TypeOfOperationService typeOfOperationService;
    private final FormulaValidationService formulaValidationService;
    private final ProductTypeVariableService productTypeVariableService;
    private final TemplateVariableService templateVariableService;

    public FormulasEditor(Consumer<Object> onSave,
                          TypeOfOperationService typeOfOperationService,
                          FormulaValidationService formulaValidationService,
                          ProductTypeVariableService productTypeVariableService,
                          TemplateVariableService templateVariableService) {
        super(onSave);

        this.typeOfOperationService = typeOfOperationService;
        this.formulaValidationService = formulaValidationService;
        this.productTypeVariableService = productTypeVariableService;
        this.templateVariableService = templateVariableService;

        // Настройка полей
        typeOfOperationSelect.setItems(this.typeOfOperationService.findAll());
        typeOfOperationSelect.setLabel("Тип работы");
        typeOfOperationSelect.setItemLabelGenerator(TypeOfOperation::getName);

        formulaArea.setHeight("100px");
        formulaArea.getElement().getStyle().set("align-self", "baseline");
        openEditorButton.getElement().getStyle().set("align-self", "baseline");

        openEditorButton.addClickListener(e -> {
            FormulaEditor editor = new FormulaEditor(
                    formulaArea.getValue(),
                    formulaArea::setValue,
                    null, // contextVariables - нет в этом контексте
                    this.formulaValidationService,
                    this.typeOfOperationService,
                    this.productTypeVariableService,
                    this.templateVariableService
            );
            editor.open();
        });

        // Привязка к биндеру
        binder.forField(name).asRequired("Название не может быть пустым").bind(Formulas::getName, Formulas::setName);
        binder.forField(description).bind(Formulas::getDescription, Formulas::setDescription);
        binder.forField(typeOfOperationSelect).bind(Formulas::getTypeOfOperation, Formulas::setTypeOfOperation);
        binder.forField(formulaArea).asRequired("Формула не может быть пустой").bind(Formulas::getFormula, Formulas::setFormula);

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
        form.setExpandColumns(true);
        form.setWidthFull();

        var row1 = new FormLayout.FormRow();
        row1.add(name, 6);

        var row2 = new FormLayout.FormRow();
        row2.add(description, 6);

        var row3 = new FormLayout.FormRow();
        row3.add(typeOfOperationSelect, 6);

        var row4 = new FormLayout.FormRow();
        row4.add(formulaArea, 5);
        row4.add(openEditorButton, 1);

        form.add(row1, row2, row3, row4);
        return form;
    }

    @Override
    public void edit(Formulas entity) {
        if (entity == null) {
            clear();
        } else {
            super.edit(entity);
        }
    }
}