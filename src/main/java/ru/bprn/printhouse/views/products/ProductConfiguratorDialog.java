package ru.bprn.printhouse.views.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.List;
import java.util.Optional;

public class ProductConfiguratorDialog extends Dialog {

    private final Templates template;
    private final VerticalLayout content = new VerticalLayout();
    private final IntegerField quantityField = new IntegerField("Тираж");

    public ProductConfiguratorDialog(Templates template) {
        this.template = template;

        setHeaderTitle("Настройка продукта: " + template.getName());
        setWidth("600px");
        setDraggable(true);
        setResizable(true);

        content.setPadding(false);
        content.setSpacing(true);

        configureQuantityField();
        content.add(quantityField);

        // Создаем секции для каждого компонента продукта (AbstractProductType)
        for (AbstractProductType productType : template.getProductTypes()) {
            content.add(createProductTypeSection(productType));
        }

        add(content);
        configureFooter();
    }

    private void configureQuantityField() {
        quantityField.setWidthFull();
        quantityField.setStepButtonsVisible(true);

        getVariable(template, "quantity").ifPresent(quantityVar -> {
            tryParseInt(quantityVar.getValue()).ifPresent(quantityField::setValue);
            tryParseInt(quantityVar.getMinValue()).ifPresent(quantityField::setMin);
            tryParseInt(quantityVar.getMaxValue()).ifPresent(quantityField::setMax);
            tryParseInt(quantityVar.getStep()).ifPresent(quantityField::setStep);
        });
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

        // Добавляем выбор основного материала, если это применимо
        if (productType instanceof OneSheetDigitalPrintingProductType osdpt) {
            ComboBox<AbstractMaterials> materialComboBox = new ComboBox<>("Основной материал");
            materialComboBox.setItems(osdpt.getSelectedMat());
            materialComboBox.setItemLabelGenerator(AbstractMaterials::getName);
            materialComboBox.setValue(osdpt.getDefaultMat());
            materialComboBox.setWidthFull();
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
        Checkbox enabledCheckbox = new Checkbox("Включить операцию", !operation.isSwitchOff());
        operationContent.add(enabledCheckbox);

        // Выбор материала для операции
        if (operation.getOperation().isHaveMaterial() && !operation.getOperation().getListOfMaterials().isEmpty()) {
            ComboBox<AbstractMaterials> materialComboBox = new ComboBox<>("Материал операции");
            materialComboBox.setItems(operation.getOperation().getListOfMaterials());
            materialComboBox.setItemLabelGenerator(AbstractMaterials::getName);
            materialComboBox.setValue(operation.getSelectedMaterial());
            materialComboBox.setWidthFull();
            operationContent.add(materialComboBox);
        }

        // Поля для редактирования видимых переменных
        List<Variable> visibleVariables = operation.getCustomVariables().stream()
                .filter(Variable::isShow)
                .toList();

        if (!visibleVariables.isEmpty()) {
            for (Variable var : visibleVariables) {
                operationContent.add(createVariableEditor(var));
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
                IntegerField field = new IntegerField(label);
                field.setValue((Integer) variable.getValueAsObject());
                yield field;
            }
            case DOUBLE -> {
                NumberField field = new NumberField(label);
                field.setValue((Double) variable.getValueAsObject());
                yield field;
            }
            case BOOLEAN -> {
                Checkbox field = new Checkbox(label);
                field.setValue((Boolean) variable.getValueAsObject());
                yield field;
            }
            default -> { // STRING
                TextField field = new TextField(label);
                field.setValue((String) variable.getValueAsObject());
                yield field;
            }
        };
    }

    private void configureFooter() {
        Button addToCartButton = new Button("Добавить в корзину", VaadinIcon.CART.create());
        addToCartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        addToCartButton.addClickListener(e -> {
            // TODO: Реализовать логику добавления в корзину
            Notification.show(String.format("Добавлено в корзину: %s, %d шт. (с кастомной конфигурацией)",
                    template.getName(), quantityField.getValue()));
            close();
        });

        Button cancelButton = new Button("Отмена", e -> close());

        getFooter().add(cancelButton, addToCartButton);
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