package ru.bprn.printhouse.views.products;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.Optional;

public class ProductCard extends VerticalLayout {

    public ProductCard(Templates template, AbstractProductType productType) {
        setWidth("340px");
        getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "1em");

        // 1. Изображение
        Image productImage = new Image("images/placeholder.png", "Product image");
        productImage.setWidthFull();

        // 2. Название
        H3 name = new H3(productType.getName());
        name.getStyle().set("margin-top", "0.5em").set("margin-bottom", "0.2em");

        // 3. Описание
        Span description = new Span(template.getDescription() != null ? template.getDescription() : "Описание отсутствует");
        description.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        // 4. Блок быстрого заказа
        IntegerField quantityField = new IntegerField();
        quantityField.setWidth("50%");
        quantityField.setPlaceholder("Тираж");
        quantityField.setStepButtonsVisible(true);

        getVariable(template, "quantity").ifPresent(quantityVar -> {
            tryParseInt(quantityVar.getValue()).ifPresent(quantityField::setValue);
            tryParseInt(quantityVar.getMinValue()).ifPresent(quantityField::setMin);
            tryParseInt(quantityVar.getMaxValue()).ifPresent(quantityField::setMax);
            tryParseInt(quantityVar.getStep()).ifPresent(quantityField::setStep);
        });
        Button addToCartButton = new Button("Купить", VaadinIcon.CART.create());
        addToCartButton.setWidth("45%");
        addToCartButton.addClickListener(e -> {
            // Здесь будет логика добавления в корзину с конфигурацией по умолчанию
            Notification.show(String.format("Добавлено в корзину: %s, %d шт.", productType.getName(), quantityField.getValue()));
        });

        HorizontalLayout quickOrderLayout = new HorizontalLayout(quantityField, addToCartButton);
        quickOrderLayout.setWidthFull();
        quickOrderLayout.setAlignItems(Alignment.BASELINE);

        // 5. Кнопка "Настроить"
        Button configureButton = new Button("Настроить и заказать");
        configureButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        configureButton.setWidthFull();
        configureButton.addClickListener(e -> {
            ProductConfiguratorDialog dialog = new ProductConfiguratorDialog(template, productType);
            dialog.open();
        });

        add(productImage, name, description, quickOrderLayout, configureButton);
        setSpacing(false);
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