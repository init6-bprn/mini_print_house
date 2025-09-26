package ru.bprn.printhouse.views.operation.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FormulaEditor extends Dialog {

    private final TextArea formulaField = new TextArea("Формула");
    private final Select<TypeOfOperation> typeSelect = new Select<>();
    private final TabSheet tabSheet = new TabSheet();

    private final Consumer<String> consumer;
    private final FormulaValidationService validationService;
    private final List<Variable> contextVariables;
    private final ProductTypeVariableService productTypeVariableService;
    private final TemplateVariableService templateVariableService;

    private static final List<String> MATH_FUNCTIONS = Arrays.asList(
            "Math.max(a, b)", "Math.min(a, b)", "Math.abs(a)", "Math.round(a)", "Math.ceil(a)", "Math.floor(a)", "Math.pow(a, b)"
    );

    /**
     * Универсальный конструктор редактора формул.
     * @param formula Текущее значение формулы.
     * @param consumer Callback для сохранения результата.
     * @param contextVariables Список переменных из текущего контекста (например, из операции). Может быть null.
     * @param validationService Сервис валидации.
     * @param worksService Сервис для получения типов работ.
     * @param productTypeVariableService Сервис для получения переменных компонентов продукта.
     * @param templateVariableService Сервис для получения переменных шаблона.
     */
    public FormulaEditor(String formula, Consumer<String> consumer, List<Variable> contextVariables, FormulaValidationService validationService, TypeOfOperationService worksService, ProductTypeVariableService productTypeVariableService, TemplateVariableService templateVariableService) {
        this.consumer = consumer;
        this.contextVariables = contextVariables;
        this.validationService = validationService;
        this.productTypeVariableService = productTypeVariableService;
        this.templateVariableService = templateVariableService;

        setHeaderTitle("Редактирование формул");
        setWidth("900px");
        setHeight("700px");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        formulaField.setValue(formula);

        configureFields(worksService);
        createTabs();

        VerticalLayout layout = new VerticalLayout(
                new HorizontalLayout(typeSelect),
                formulaField,
                tabSheet
        );
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);

        add(layout);
        getFooter().add(createButtons());
    }

    private void configureFields(TypeOfOperationService worksService) {
        typeSelect.setLabel("Тип работы (для фильтрации)");
        typeSelect.setItems(worksService.findAll());
        typeSelect.setItemLabelGenerator(TypeOfOperation::getName);
        typeSelect.setWidth("300px");

        formulaField.setHeight("150px");
        formulaField.setWidthFull();
        formulaField.setClearButtonVisible(true);
    }

    private void createTabs() {
        tabSheet.add("Переменные шаблона",
                createVariableTabContent(templateVariableService.getVariablesFor(Templates.class)));
        tabSheet.add("Переменные однолистовой печати",
                createVariableTabContent(productTypeVariableService.getVariablesFor(OneSheetDigitalPrintingProductType.class)));
        if (contextVariables != null && !contextVariables.isEmpty()) {
            tabSheet.add("Переменные контекста (операции)", createVariableTabContent(contextVariables));
        }
        tabSheet.add("Математические функции", createMathTabContent());
        tabSheet.setSizeFull();
    }

    private Component createVariableTabContent(List<Variable> variables) {
        if (variables == null || variables.isEmpty()) {
            return new Div(new Text("Нет доступных переменных."));
        }
        Div container = new Div();
        container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexWrap.WRAP, LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);

        variables.stream()
                .filter(v -> v.getKey() != null && !v.getKey().toLowerCase().contains("formula"))
                .forEach(var -> {
            Button button = new Button(var.getKey(), click -> insertText(var.getKey()));
            button.setTooltipText(var.getDescription());
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
            container.add(button);
        });
        return container;
    }

    private Component createMathTabContent() {
        Div container = new Div();
        container.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexWrap.WRAP, LumoUtility.Gap.SMALL, LumoUtility.Padding.SMALL);
        for (String func : MATH_FUNCTIONS) {
            Button button = new Button(func, click -> insertText(func));
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
            container.add(button);
        }
        return container;
    }

    private Component createButtons() {
        Button save = new Button("Сохранить", click -> {
            String s = formulaField.getValue();
            FormulaValidationService.ValidationResult result = validateFormula(s);
            if (result.isValid()) {
                consumer.accept(s);
                close();
            } else {
                Notification.show(result.getErrorMessage(), 5000, Notification.Position.MIDDLE);
            }
        });

        Button cancel = new Button("Закрыть", click -> close());

        HorizontalLayout buttons = new HorizontalLayout(cancel, save);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();
        return buttons;
    }

    private FormulaValidationService.ValidationResult validateFormula(String formula) {
        List<List<Variable>> allVariableLists = new ArrayList<>();
        allVariableLists.add(contextVariables);
        allVariableLists.add(templateVariableService.getVariablesFor(Templates.class));
        allVariableLists.add(productTypeVariableService.getVariablesFor(OneSheetDigitalPrintingProductType.class));

        //noinspection unchecked
        return validationService.validate(formula, allVariableLists.toArray(new List[0]));
    }

    private void insertText(String text) {
        // Выполняем JavaScript для вставки текста в позицию курсора
        formulaField.getElement().executeJs(
                "const start = this.inputElement.selectionStart;" +
                        "const end = this.inputElement.selectionEnd;" +
                        "const originalValue = this.inputElement.value;" +
                        "this.inputElement.value = originalValue.substring(0, start) + $0 + originalValue.substring(end);" +
                        "this.inputElement.focus();" +
                        "this.inputElement.setSelectionRange(start + $0.length, start + $0.length);", text);
    }
}
