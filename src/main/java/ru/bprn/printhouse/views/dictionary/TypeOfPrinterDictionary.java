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
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import ru.bprn.printhouse.data.entity.TypeOfPrinter;
import ru.bprn.printhouse.data.service.TypeOfPrinterService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь типа принтера")
@Route(value = "type_of_printer_dictionary", layout = MainLayout.class)
@AnonymousAllowed

public class TypeOfPrinterDictionary extends VerticalLayout {

    public TypeOfPrinterDictionary(TypeOfPrinterService topService) {

        DefaultCrudFormFactory<TypeOfPrinter> formFactory = new DefaultCrudFormFactory<TypeOfPrinter>(TypeOfPrinter.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
                formFactory.setUseBeanValidation(true);
                formFactory.setVisibleProperties("name");

        GridCrud<TypeOfPrinter> crud = new GridCrud<>(TypeOfPrinter.class, new HorizontalSplitCrudLayout(), formFactory);
                crud.setClickRowToUpdate(true);
                crud.setUpdateOperationVisible(false);
                crud.getGrid().setColumns("name");

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                () -> topService.findAll(),
                user -> topService.save(user),
                user -> topService.save(user),
                user -> topService.delete(user)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        getStyle().set("text-align", "center");
    }



}
