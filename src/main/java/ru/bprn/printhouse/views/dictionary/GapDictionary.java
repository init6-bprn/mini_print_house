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
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.data.service.GapService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь отступов")
@Route(value = "gap_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class GapDictionary extends VerticalLayout {

    public GapDictionary(GapService gapService) {

        DefaultCrudFormFactory<Gap> formFactory = new DefaultCrudFormFactory<>(Gap.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("name", "gapTop", "gapBottom", "gapLeft", "GapRight");

        GridCrud<Gap> crud = new GridCrud<>(Gap.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("name", "gapTop", "gapBottom", "gapLeft", "GapRight");

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                () -> gapService.findAll(),
                user -> gapService.save(user),
                user -> gapService.save(user),
                user -> gapService.delete(user)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");
    }


}