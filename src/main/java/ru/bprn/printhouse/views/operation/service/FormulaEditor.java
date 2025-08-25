package ru.bprn.printhouse.views.operation.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.function.Consumer;

public class FormulaEditor extends Dialog {

    private final Select<TypeOfOperation> typeSelect = new Select<>();
    private final Consumer<String> consumer;
    private final TypeOfOperationService worksService;
    private final VariablesForMainWorksService variablesService;
    private final TextArea formulaField = new TextArea("Формула");

    public  FormulaEditor(String formula, Consumer<String> consumer, TypeOfOperationService worksService, VariablesForMainWorksService variablesService) {
        this.consumer = consumer;
        this.worksService = worksService;
        this.variablesService = variablesService;
        setHeaderTitle("Редактирование формул");
        setWidth("900px");
        setHeight("700px");
        setModal(true);
        setDraggable(true);
        setResizable(true);

        formulaField.setValue(formula);
        configureForm();

        VerticalLayout layout = new VerticalLayout(createFormLayout(), createVariableButtons(), createButtons());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        add(layout);

    }

    private void configureForm() {
        typeSelect.setLabel("Тип работы");
        typeSelect.setItems(worksService.findAll());
        typeSelect.setItemLabelGenerator(TypeOfOperation::getName);

        formulaField.setMaxRows(3);
        formulaField.setWidthFull();
        formulaField.setClearButtonVisible(true);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(typeSelect, formulaField);
        formLayout.setColspan(formulaField, 2);
        return formLayout;
    }

    private Component createButtons() {
        Button save = new Button("Сохранить", click -> {
            String s = formulaField.getValue();
            if (validateFormula(s)) {
                consumer.accept(s);
            }
        });

        Button cancel = new Button("Закрыть", click -> close());

        HorizontalLayout buttons = new HorizontalLayout(cancel, save);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        return buttons;
    }

    private boolean validateFormula(String formula) {
        StringBuilder script = new StringBuilder();
        for (VariablesForMainWorks var : variablesService.findAll()) {
            script.append(var.getName()).append(" = 1;");
        }
        script.append(formula).append(";");

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        try {
            engine.eval(script.toString());
            return true;
        } catch (ScriptException e) {
            return false;
        }
    }

    private Component createVariableButtons() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWrap(true);
        layout.setSpacing(true);

        for (VariablesForMainWorks var : variablesService.findAll()) {
            Button button = new Button(var.getName(), click -> {
                formulaField.setValue(formulaField.getValue() + var.getName() + " ");
            });
            button.setTooltipText(var.getDescription());
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
            layout.add(button);
        }

        return layout;
    }
}
