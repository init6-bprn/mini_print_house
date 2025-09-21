package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import ru.bprn.printhouse.views.material.service.MaterialService;
import ru.bprn.printhouse.views.price.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.price.service.PriceOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь стоимости материалов")
@Route(value = "material_dictionary", layout = MainLayout.class)
@AnonymousAllowed

public class PriceOfMaterialDictionary extends VerticalLayout {

    public PriceOfMaterialDictionary(PriceOfMaterialService price, MaterialService materialService) {
        DefaultCrudFormFactory<PriceOfMaterial> formFactory = new DefaultCrudFormFactory<>(PriceOfMaterial.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("material", "price");

        GridCrud<PriceOfMaterial> crud = new GridCrud<>(PriceOfMaterial.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("material", "price");

        crud.getCrudFormFactory().setFieldProvider("material",
                new ComboBoxProvider<>(materialService.findAll()));

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                price::findAll,
                price::save,
                price::save,
                price::delete
        );

        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        getStyle().set("text-align", "center");
    }


}

