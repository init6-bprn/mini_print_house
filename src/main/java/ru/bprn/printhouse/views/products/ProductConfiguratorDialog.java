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
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.products.service.CalculationReport;
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
        updatePrice();
    }

    private void configureQuantityField() {
        quantityField.setWidthFull();
        quantityField.setStepButtonsVisible(true);
        quantityField.setValueChangeMode(ValueChangeMode.LAZY);

        getVariable(template, "quantity").ifPresent(quantityVar -> {
            tryParseInt(quantityVar.getValue()).ifPresent(quantityField::setValue);
            tryParseInt(quantityVar.getMinValue()).ifPresent(quantityField::setMin);
            tryParseInt(quantityVar.getMaxValue()).ifPresent(quantityField::setMax);
            tryParseInt(quantityVar.getStep()).ifPresent(quantityField::setStep);
        });

        quantityField.addValueChangeListener(e -> updatePrice());
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
                updatePrice();
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
        enabledCheckbox.addValueChangeListener(event -> operation.setSwitchOff(!event.getValue()));
        enabledCheckbox.addValueChangeListener(e -> updatePrice());
        operationContent.add(enabledCheckbox);

        // Добавляем выбор материала для операции (например, цветности печати)
        if (!operation.getOperation().getListOfMaterials().isEmpty()) {
            ComboBox<AbstractMaterials> materialComboBox = new ComboBox<>("Материал операции");
            materialComboBox.setItems(operation.getOperation().getListOfMaterials());
            materialComboBox.setItemLabelGenerator(AbstractMaterials::getName);
            materialComboBox.setValue(operation.getSelectedMaterial());
            materialComboBox.setWidthFull();

            materialComboBox.addValueChangeListener(event -> operation.setSelectedMaterial(event.getValue()));
            materialComboBox.addValueChangeListener(e -> updatePrice());
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
                    updatePrice();
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
                    updatePrice();
                });
                yield field;
            }
            case BOOLEAN -> {
                var field = new Checkbox(label);
                field.setId(variable.getKey()); // Устанавливаем ID
                field.setValue((Boolean) variable.getValueAsObject());
                field.addValueChangeListener(e -> {
                    variable.setValue(e.getValue()); // Обновляем значение в объекте Variable
                    updatePrice();
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
                    updatePrice();
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

        priceLabel.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "600")
                .set("margin-right", "auto"); // Прижимаем цену влево

        getFooter().add(priceLabel, cancelButton, addToCartButton);
    }

    private void updatePrice() {
        Map<String, Object> userInputs = new HashMap<>();

        // Собираем все значения из полей
        for (Component component : priceInfluencingComponents) {
            component.getId().ifPresent(id -> {
                switch (component) {
                    case IntegerField field -> userInputs.put(id, field.getValue());
                    case NumberField field -> userInputs.put(id, field.getValue());
                    case Checkbox field -> userInputs.put(id, field.getValue());
                    case TextField field -> userInputs.put(id, field.getValue());
                    case ComboBox field -> {
                        if ("mainMaterial".equals(id)) {
                            // Особая логика для основного материала
                        } else if ("operationMaterial".equals(id)) {
                            // Особая логика для материала операции
                        }
                    }
                    default -> {}
                }
            });
        }

        Integer quantity = (Integer) userInputs.get("quantity");
        if (quantity == null || quantity <= 0) {
            priceLabel.setText("Введите тираж");
            return;
        }

        // Вызываем сервис расчета
        CalculationReport report = priceCalculationService.calculateTotalPrice(template, userInputs);
        // System.out.println(report.getDescription()); // Для отладки

        // Отображаем результат
        double priceInRubles = report.getTotalPriceInKopecks() / 100.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
        priceLabel.setText("Итого: " + currencyFormat.format(priceInRubles));
    }

    private Optional<Variable> getVariable(Templates template, String key) {
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
}