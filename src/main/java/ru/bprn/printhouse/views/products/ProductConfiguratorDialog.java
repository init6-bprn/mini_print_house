package ru.bprn.printhouse.views.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import ru.bprn.printhouse.views.products.entity.FinalCalculation;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.products.service.PriceCalculationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ProductConfiguratorDialog extends Dialog {

    private final Templates template;
    private final AbstractProductType productType;
    private final PriceCalculationService priceCalculationService;
    private final VerticalLayout content = new VerticalLayout();
    private final IntegerField quantityField = new IntegerField("Тираж");
    private final Span priceLabel = new Span();
    private final Span weightLabel = new Span();
    private final Span timeLabel = new Span();

    // Хранилище для компонентов, которые влияют на цену
    private final List<Component> priceInfluencingComponents = new ArrayList<>();

    public ProductConfiguratorDialog(Templates template, AbstractProductType productType, PriceCalculationService priceCalculationService) {
        this.template = template;
        this.productType = productType;
        this.priceCalculationService = priceCalculationService;

        setHeaderTitle("Настройка продукта: " + productType.getName());
        setWidth("600px");
        setDraggable(true);
        setResizable(true);

        content.setPadding(false);
        content.setSpacing(true);

        configureQuantityField();
        content.add(quantityField);

        // Создаем секции для каждого компонента продукта (AbstractProductType)
        content.add(createProductTypeSection(this.productType));

        add(content);
        configureFooter();

        // Первоначальный расчет цены
        updateCalculationResults();
    }

    private void configureQuantityField() {
        quantityField.setWidthFull();
        quantityField.setStepButtonsVisible(true);
        quantityField.setValueChangeMode(ValueChangeMode.LAZY);
        quantityField.setId("quantity"); // Устанавливаем ID для поля тиража

        getVariable(template, "quantity").ifPresent(quantityVar -> {
            tryParseInt(quantityVar.getValue()).ifPresent(quantityField::setValue);
            tryParseInt(quantityVar.getMinValue()).ifPresent(quantityField::setMin);
            tryParseInt(quantityVar.getMaxValue()).ifPresent(quantityField::setMax);
            tryParseInt(quantityVar.getStep()).ifPresent(quantityField::setStep);
        });

        quantityField.addValueChangeListener(e -> updateCalculationResults());
        priceInfluencingComponents.add(quantityField);
    }

    private Component createProductTypeSection(AbstractProductType productType) {
        VerticalLayout sectionLayout = new VerticalLayout();
        sectionLayout.setSpacing(false);
        sectionLayout.setPadding(false);
        sectionLayout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-m)");

        sectionLayout.add(new H3(productType.getName()));

        // Добавляем выбор основного материала (бумаги)
        if (productType.getDefaultMaterial() != null || !productType.getSelectedMaterials().isEmpty()) {
            ComboBox<AbstractMaterials> materialComboBox = new ComboBox<>("Основной материал");
            materialComboBox.setItems(productType.getSelectedMaterials());
            materialComboBox.setItemLabelGenerator(AbstractMaterials::getName);
            materialComboBox.setValue(productType.getDefaultMaterial());
            materialComboBox.setWidthFull();

            materialComboBox.addValueChangeListener(event -> {
                // Обновляем сам объект productType перед пересчетом
                if (productType instanceof OneSheetDigitalPrintingProductType osdpt) {
                    osdpt.setDefaultMaterial((PrintSheetsMaterial) event.getValue());
                }
                updateCalculationResults();
            });
            priceInfluencingComponents.add(materialComboBox);
            sectionLayout.add(materialComboBox);
        }

        // Добавляем настраиваемые операции
        for (ProductOperation operation : productType.getProductOperations()) {
            sectionLayout.add(createOperationDetails(operation));
        }

        return sectionLayout;
    }

    private Component createOperationDetails(ProductOperation operation) {
        String operationDisplayName = operation.getOperation().getName() +
                (operation.getName() != null && !operation.getName().isBlank() ? " - " + operation.getName() : "");

        Details details = new Details();
        details.setSummaryText(operationDisplayName);
        details.setOpened(true);

        VerticalLayout operationContent = new VerticalLayout();
        operationContent.setPadding(false);
        operationContent.setSpacing(true);

        // Чекбокс для включения/отключения операции
        Checkbox enabledCheckbox = new Checkbox("Выполнять эту операцию", !operation.isSwitchOff());
        enabledCheckbox.addValueChangeListener(event -> {
            operation.setSwitchOff(!event.getValue()); // Сначала обновляем состояние объекта
            updateCalculationResults();                             // Затем запускаем пересчет
        });
        operationContent.add(enabledCheckbox);

        // Добавляем выбор материала для операции (например, цветности печати)
        if (!operation.getOperation().getListOfMaterials().isEmpty()) {
            ComboBox<AbstractMaterials> materialComboBox = new ComboBox<>("Материал операции");
            materialComboBox.setItems(operation.getOperation().getListOfMaterials());
            materialComboBox.setItemLabelGenerator(AbstractMaterials::getName);
            materialComboBox.setValue(operation.getSelectedMaterial());
            materialComboBox.setWidthFull();

            materialComboBox.addValueChangeListener(event -> operation.setSelectedMaterial(event.getValue()));
            materialComboBox.addValueChangeListener(e -> updateCalculationResults());
            priceInfluencingComponents.add(materialComboBox);
            operationContent.add(materialComboBox);
        }

        // Поля для редактирования видимых переменных
        List<Variable> visibleVariables = operation.getCustomVariables().stream()
                .filter(Variable::isShow)
                .toList();

        if (!visibleVariables.isEmpty()) {
            for (Variable var : visibleVariables) {
                Component editor = createVariableEditor(var);
                operationContent.add(editor);
                priceInfluencingComponents.add(editor);
            }
        }

        details.add(operationContent);
        return details;
    }

    private Component createVariableEditor(Variable variable) {
        String label = variable.getDescription() != null && !variable.getDescription().isBlank()
                ? variable.getDescription()
                : variable.getKey();

        return switch (variable.getType()) {
            case INTEGER -> {
                var field = new IntegerField(label);
                field.setId(variable.getKey()); // Устанавливаем ID
                field.setValueChangeMode(ValueChangeMode.LAZY);
                field.setValue((Integer) variable.getValueAsObject());
                field.addValueChangeListener(e -> {
                    variable.setValue(e.getValue()); // Обновляем значение в объекте Variable
                    updateCalculationResults();
                });
                yield field;
            }
            case DOUBLE -> {
                var field = new NumberField(label);
                field.setId(variable.getKey()); // Устанавливаем ID
                field.setValueChangeMode(ValueChangeMode.LAZY);
                field.setValue((Double) variable.getValueAsObject());
                field.addValueChangeListener(e -> {
                    variable.setValue(e.getValue()); // Обновляем значение в объекте Variable
                    updateCalculationResults();
                });
                yield field;
            }
            case BOOLEAN -> {
                var field = new Checkbox(label);
                field.setId(variable.getKey()); // Устанавливаем ID
                field.setValue((Boolean) variable.getValueAsObject());
                field.addValueChangeListener(e -> {
                    variable.setValue(e.getValue()); // Обновляем значение в объекте Variable
                    updateCalculationResults();
                });
                yield field;
            }
            default -> { // STRING
                var field = new TextField(label);
                field.setId(variable.getKey()); // Устанавливаем ID
                field.setValueChangeMode(ValueChangeMode.LAZY);
                field.setValue((String) variable.getValueAsObject());
                field.addValueChangeListener(e -> {
                    variable.setValue(e.getValue()); // Обновляем значение в объекте Variable
                    updateCalculationResults();
                });
                yield field;
            }
        };
    }

    private void configureFooter() {
        Button addToCartButton = new Button("Добавить в корзину", VaadinIcon.CART.create());
        addToCartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        addToCartButton.addClickListener(e -> {
            // TODO: Реализовать логику добавления в корзину
            Notification.show(String.format("Добавлено в корзину: %s, %d шт. (с кастомной конфигурацией)", //TODO: Заменить на productType.getName()
                    productType.getName(), quantityField.getValue()));
            close();
        });

        Button cancelButton = new Button("Отмена", e -> close());

        // Контейнер для всех результатов расчета
        VerticalLayout resultsLayout = new VerticalLayout(priceLabel, weightLabel, timeLabel);
        resultsLayout.setSpacing(false);
        resultsLayout.setPadding(false);
        resultsLayout.getStyle().set("margin-right", "auto"); // Прижимаем влево

        priceLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600");

        weightLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        timeLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        getFooter().add(resultsLayout, cancelButton, addToCartButton);
    }

    private void updateCalculationResults() {
        // Все изменения в полях уже применены к объектам `template`, `productType` и `operation`
        // благодаря слушателям `addValueChangeListener`.
        // Поэтому нам нужно передать в сервис только тираж.

        Integer quantity = quantityField.getValue();
        if (quantity == null || quantity <= 0) {
            priceLabel.setText("Введите тираж");
            weightLabel.setText(null);
            timeLabel.setText(null);
            return;
        }
        
        Map<String, Object> userInputs = new HashMap<>();
        userInputs.put("quantity", quantity);
        
        // Вызываем сервис расчета
        FinalCalculation calculation = priceCalculationService.calculate(template, userInputs);

        // Проверяем на наличие ошибок
        if (calculation.hasErrors()) {
            priceLabel.setText("Ошибка расчета");
            weightLabel.setText(null);
            timeLabel.setText(null);
            Notification.show(calculation.getErrors().get(0), 5000, Notification.Position.MIDDLE);
            return;
        }
        
        // Отображаем цену
        double priceInRubles = calculation.getFinalPrice() / 100.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
        priceLabel.setText("Итого: " + currencyFormat.format(priceInRubles));

        // Отображаем вес
        double weightInGrams = calculation.getTotalWeight();
        if (weightInGrams > 0) {
            weightLabel.setText(weightInGrams < 1000 ? String.format("Вес: %.0f гр.", weightInGrams) : String.format("Вес: %.2f кг.", weightInGrams / 1000.0));
        } else {
            weightLabel.setText(null);
        }

        // Отображаем время
        double timeInSeconds = calculation.getTotalManufacturingTime();
        if (timeInSeconds > 0) {
            timeLabel.setText("Время: " + formatSeconds(timeInSeconds));
        } else {
            timeLabel.setText(null);
        }
    }

    // --- Вспомогательные методы ---
    private Optional<Variable> getVariable(Templates t, String key) {
        if (template == null || template.getVariables() == null) {
            return Optional.empty();
        }
        return template.getVariables().stream()
                .filter(v -> key.equals(v.getKey()))
                .findFirst();
    }

    private Optional<Integer> tryParseInt(String s) {
        if (s == null || s.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String formatSeconds(double totalSeconds) {
        if (totalSeconds < 1) return "< 1 сек.";

        long seconds = (long) totalSeconds;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("ч ");
        if (minutes > 0) sb.append(minutes).append("м ");
        if (remainingSeconds > 0 || sb.length() == 0) sb.append(remainingSeconds).append("с");

        return sb.toString().trim();
    }
}