package ru.bprn.printhouse.views.products;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesModuleService;

import java.util.List;

@PageTitle("Каталог продукции")
@Route(value = "catalog", layout = MainLayout.class)
@AnonymousAllowed
public class ProductCatalogView extends VerticalLayout { //TODO: Заменить на более подходящий Layout

    private final TemplatesModuleService templatesModuleService;
    private final TextField searchField = new TextField();
    private final FlexLayout productContainer = new FlexLayout();

    public ProductCatalogView(TemplatesModuleService templatesModuleService) {
        this.templatesModuleService = templatesModuleService;

        setSizeFull();
        setPadding(false);
        getStyle().set("padding", "var(--lumo-space-m)");

        configureSearch();

        productContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        productContainer.setJustifyContentMode(JustifyContentMode.START);
        productContainer.getStyle().set("gap", "1em");

        add(searchField, productContainer);

        loadProducts(null);
    }

    private void configureSearch() {
        searchField.setPlaceholder("Поиск по названию...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("50%");
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> loadProducts(e.getValue()));
    }

    private void loadProducts(String filter) {
        productContainer.removeAll();
        List<Templates> templates = templatesModuleService.findAllTemplates(filter);

        for (Templates template : templates) {
            for (AbstractProductType productType : template.getProductTypes()) {
                productContainer.add(new ProductCard(template, productType));
            }
        }
    }
}