package ru.bprn.printhouse.views.operation;

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
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь типов дополнительных работ")
@Route(value = "type_of_works_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class TypeOfOperationView extends VerticalLayout {

    public TypeOfOperationView(TypeOfOperationService service) {

        DefaultCrudFormFactory<TypeOfOperation> formFactory = new DefaultCrudFormFactory<>(TypeOfOperation.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("name");

        GridCrud<TypeOfOperation> crud = new GridCrud<>(TypeOfOperation.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("name");

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                service::findAll,
                service::save,
                service::save,
                service::delete
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");
    }

}
