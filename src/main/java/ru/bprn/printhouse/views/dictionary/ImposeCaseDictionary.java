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
import ru.bprn.printhouse.data.entity.ImposeCase;
import ru.bprn.printhouse.data.service.ImposeCaseService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь спуска полос")
@Route(value = "impose_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class ImposeCaseDictionary extends VerticalLayout{

        public ImposeCaseDictionary(ImposeCaseService imposeCaseService) {

            DefaultCrudFormFactory<ImposeCase> formFactory = new DefaultCrudFormFactory<>(ImposeCase.class) {
                @Override
                protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                    Component nameField = (Component) fields.get(0);
                    formLayout.setColspan(nameField, 2);
                }
            };
            formFactory.setUseBeanValidation(true);
            formFactory.setVisibleProperties("name", "sheets");

            GridCrud<ImposeCase> crud = new GridCrud<>(ImposeCase.class, new HorizontalSplitCrudLayout(), formFactory);
            crud.setClickRowToUpdate(true);
            crud.setUpdateOperationVisible(false);
            crud.getGrid().setColumns("name", "sheets");

            setSizeFull();
            this.add(crud);

            crud.setOperations(
                    imposeCaseService::findAll,
                    imposeCase -> imposeCaseService.save(imposeCase),
                    imposeCase -> imposeCaseService.save(imposeCase),
                    imposeCase -> imposeCaseService.delete(imposeCase)
            );

            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
