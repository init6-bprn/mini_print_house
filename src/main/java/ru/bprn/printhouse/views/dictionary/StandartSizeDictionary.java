package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь стандартных размеров изделий")
@Route(value = "standart_size_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class StandartSizeDictionary extends VerticalLayout {
    public StandartSizeDictionary (StandartSizeService standartSizeService) {
        DefaultCrudFormFactory<StandartSize> formFactory = new DefaultCrudFormFactory<StandartSize>(StandartSize.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("name", "length", "width");

        GridCrud<StandartSize> crud = new GridCrud<>(StandartSize.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("name", "length", "width");

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                () -> standartSizeService.findAll(),
                leaf -> standartSizeService.save(leaf),
                leaf -> standartSizeService.save(leaf),
                leaf -> standartSizeService.delete(leaf)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");
    }

}
