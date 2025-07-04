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
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@AnonymousAllowed
@PageTitle("Словарь переменных")
@Route(value = "variables_dictionary", layout = MainLayout.class)
public class VariablesForMainWorksDictionary extends VerticalLayout {

    public VariablesForMainWorksDictionary (VariablesForMainWorksService service){
        DefaultCrudFormFactory<VariablesForMainWorks> formFactory = new DefaultCrudFormFactory<>(VariablesForMainWorks.class){
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("clazz", "name", "description");

        GridCrud<VariablesForMainWorks> crud = new GridCrud<>(VariablesForMainWorks.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("clazz", "name", "description");

        setSizeFull();
        this.add(crud);

        crud.setOperations(service::findAll, service::save, service::save, service::delete);

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");

    }

}
