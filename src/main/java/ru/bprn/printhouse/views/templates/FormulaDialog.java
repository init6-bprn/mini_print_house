package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;
import java.util.stream.Collectors;

public class FormulaDialog extends Dialog {

    private final Grid<Formulas> grid = new Grid<>(Formulas.class, false);
    private final TextField nameFilter = new TextField("Поиск по названию");
    private final Select<TypeOfOperation> typeFilter = new Select<>();

    private final TextField nameField = new TextField("Название формулы");
    private final TextArea formulaField = new TextArea("Формула");
    private final Select<TypeOfOperation> typeSelect = new Select<>();

    private final BeanValidationBinder<Formulas> binder = new BeanValidationBinder<>(Formulas.class);
    private final FormulasService formulasService;
    private final VariablesForMainWorksService variablesService;
    private final TypeOfOperationService worksService;
    private final FormulaSelectionListener selectionListener;

    private Formulas currentFormula;
    private Formulas selectedFormula;

    public FormulaDialog(FormulasService formulasService,
                         VariablesForMainWorksService variablesService,
                         TypeOfOperationService worksService,
                         FormulaSelectionListener selectionListener) {
        this.formulasService = formulasService;
        this.variablesService = variablesService;
        this.worksService = worksService;
        this.selectionListener = selectionListener;

        setHeaderTitle("Редактирование формул");
        setWidth("900px");
        setHeight("700px");
        setModal(true);
        setDraggable(true);
        setResizable(true);

        Button selectButton = new Button("Выбрать", event -> {
            if (selectedFormula != null) {
                selectionListener.onFormulaSelected(selectedFormula); // 🔁 callback
                close(); // Закрываем диалог
            } else {
                Notification.show("Сначала выберите формулу");
            }
        });

        // Кнопка отмены
        Button cancelButton = new Button("Отмена", event -> close());

        HorizontalLayout buttons = new HorizontalLayout(selectButton, cancelButton);

        configureFilters();
        configureGrid();
        configureForm();

        VerticalLayout layout = new VerticalLayout(
                createFilterLayout(),
                grid,
                buttons,
                createFormLayout(),
                createVariableButtons(),
                createButtons()
        );
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        add(layout);
        updateGrid();
    }

    private void configureFilters() {
        nameFilter.setPlaceholder("Введите название...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.setWidth("300px");
        nameFilter.addValueChangeListener(e -> updateGrid());

        typeFilter.setItems(worksService.findAll());
        typeFilter.setItemLabelGenerator(TypeOfOperation::getName);
        typeFilter.setPlaceholder("Тип работы");
        typeFilter.setWidth("300px");
        typeFilter.addValueChangeListener(e -> updateGrid());
    }

    private HorizontalLayout createFilterLayout() {
        HorizontalLayout filters = new HorizontalLayout(nameFilter, typeFilter);
        filters.setAlignItems(FlexComponent.Alignment.END);
        return filters;
    }

    private void configureGrid() {
        grid.addColumn(Formulas::getName).setHeader("Название");
        //grid.addColumn(f -> f.getTypeOfOperation().getName()).setHeader("Тип работы");
        grid.addColumn(Formulas::getFormula).setHeader("Формула");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setHeight("250px");

        grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(formula -> {
            selectedFormula = formula;
            editFormula(formula);
            selectionListener.onFormulaSelected(formula);
        }));
    }

    private void configureForm() {
        typeSelect.setLabel("Тип работы");
        typeSelect.setItems(worksService.findAll());
        typeSelect.setItemLabelGenerator(TypeOfOperation::getName);

        formulaField.setMaxRows(3);
        formulaField.setWidthFull();
        formulaField.setClearButtonVisible(true);

        binder.forField(nameField)
                .asRequired("Название обязательно")
                .bind(Formulas::getName, Formulas::setName);

        binder.forField(typeSelect)
                .asRequired("Тип работы обязателен")
                .bind(Formulas::getTypeOfOperation, Formulas::setTypeOfOperation);

        binder.forField(formulaField)
                .withValidator(this::validateFormula, "Формула некорректна")
                .bind(Formulas::getFormula, Formulas::setFormula);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(nameField, typeSelect, formulaField);
        formLayout.setColspan(formulaField, 2);
        return formLayout;
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

    private Component createButtons() {
        Button save = new Button("Сохранить", click -> {
            if (binder.isValid()) {
                try {
                    binder.writeBean(currentFormula);
                    formulasService.save(currentFormula);
                    updateGrid();
                    Notification.show("Формула сохранена");
                } catch (ValidationException e) {
                    Notification.show("Ошибка сохранения");
                }
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

    private void editFormula(Formulas formula) {
        this.currentFormula = formula;
        binder.setBean(formula);
    }

    private void updateGrid() {
        String name = nameFilter.getValue().trim().toLowerCase();
        TypeOfOperation selectedType = typeFilter.getValue();

        List<Formulas> filtered = formulasService.findAll().stream()
                .filter(f -> f.getName().toLowerCase().contains(name))
                .filter(f -> selectedType == null || f.getTypeOfOperation().equals(selectedType))
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }
}
