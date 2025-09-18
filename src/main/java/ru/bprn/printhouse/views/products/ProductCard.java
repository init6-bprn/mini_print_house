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
import ru.bprn.printhouse.views.products.ProductConfiguratorDialog;
import ru.bprn.printhouse.views.templates.entity.Templates;

public class ProductCard extends VerticalLayout {

    public ProductCard(Templates template) {
        setWidth("340px");
        getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "1em");

        // 1. Изображение
        Image productImage = new Image("images/placeholder.png", "Product image");
        productImage.setWidthFull();

        // 2. Название
        H3 name = new H3(template.getName());
        name.getStyle().set("margin-top", "0.5em").set("margin-bottom", "0.2em");

        // 3. Описание
        Span description = new Span(template.getDescription() != null ? template.getDescription() : "Описание отсутствует");
        description.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        // 4. Блок быстрого заказа
        IntegerField quantityField = new IntegerField();
        quantityField.setWidth("50%");
        quantityField.setPlaceholder("Тираж");
        quantityField.setValue(template.getQuantity()); // Тираж по умолчанию из шаблона
        quantityField.setStepButtonsVisible(true);
        quantityField.setMin(template.getMinQuantity());
        quantityField.setMax(template.getMaxQuantity());
        quantityField.setStep(template.getStep());

        Button addToCartButton = new Button("Купить", VaadinIcon.CART.create());
        addToCartButton.setWidth("45%");
        addToCartButton.addClickListener(e -> {
            // Здесь будет логика добавления в корзину с конфигурацией по умолчанию
            Notification.show(String.format("Добавлено в корзину: %s, %d шт.", template.getName(), quantityField.getValue()));
        });

        HorizontalLayout quickOrderLayout = new HorizontalLayout(quantityField, addToCartButton);
        quickOrderLayout.setWidthFull();
        quickOrderLayout.setAlignItems(Alignment.BASELINE);

        // 5. Кнопка "Настроить"
        Button configureButton = new Button("Настроить и заказать");
        configureButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        configureButton.setWidthFull();
        configureButton.addClickListener(e -> {
            ProductConfiguratorDialog dialog = new ProductConfiguratorDialog(template);
            dialog.open();
        });

        add(productImage, name, description, quickOrderLayout, configureButton);
        setSpacing(false);
    }
}