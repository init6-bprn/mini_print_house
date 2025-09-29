package ru.bprn.printhouse.views.products;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.products.service.PriceOfMaterialService;

@PageTitle("Цены на материалы")
@Route(value = "price-of-material", layout = MainLayout.class)
@AnonymousAllowed
public class PriceOfMaterialView extends VerticalLayout {

    public PriceOfMaterialView(PriceOfMaterialService priceService, AbstractMaterialService materialService) {
        setSizeFull();

        // Filter
        ComboBox<AbstractMaterials> materialFilter = new ComboBox<>("Фильтр по материалу");
        materialFilter.setItems(materialService.findAll());
        materialFilter.setItemLabelGenerator(AbstractMaterials::getName);
        materialFilter.setClearButtonVisible(true);

        // CRUD
        GridCrud<PriceOfMaterial> crud = new GridCrud<>(PriceOfMaterial.class);
        crud.getCrudLayout().addFilterComponent(materialFilter);

        // Grid configuration
        crud.getGrid().setColumns("material", "price", "currency", "effectiveDate");
        crud.getGrid().getColumnByKey("material").setHeader("Материал");
        crud.getGrid().getColumnByKey("price").setHeader("Цена");
        crud.getGrid().getColumnByKey("currency").setHeader("Валюта");
        crud.getGrid().getColumnByKey("effectiveDate").setHeader("Дата установки");
        crud.getGrid().setColumnReorderingAllowed(true);

        // Form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("material", "price", "currency", "effectiveDate");
        crud.getCrudFormFactory().setFieldProvider("material", new ComboBoxProvider<>(materialService.findAll()));
        crud.getCrudFormFactory().setFieldCaptions("Материал", "Цена", "Валюта", "Дата установки");

        // Operations
        crud.setOperations(() -> priceService.findByMaterial(materialFilter.getValue()), priceService::save, priceService::save, priceService::delete);
        materialFilter.addValueChangeListener(e -> crud.refreshGrid());

        add(crud);
    }
}