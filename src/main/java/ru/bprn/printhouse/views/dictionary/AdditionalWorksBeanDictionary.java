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
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.service.AdditionalWorksBeanService;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.TypeOfWorksService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Создание дополнительных работ")
@Route(value = "additional_works", layout = MainLayout.class)
@AnonymousAllowed
public class AdditionalWorksBeanDictionary extends VerticalLayout {

    public AdditionalWorksBeanDictionary(AdditionalWorksBeanService service,
                                         FormulasService formulasService,
                                         TypeOfWorksService typeOfWorksService) {

        DefaultCrudFormFactory<AdditionalWorksBean> formFactory = new DefaultCrudFormFactory<>(AdditionalWorksBean.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("name", "typeOfWorks", "actionFormula", "haveAction", "materialFormula","haveMaterial");

        GridCrud<AdditionalWorksBean> crud = new GridCrud<>(AdditionalWorksBean.class, new HorizontalSplitCrudLayout(), formFactory);
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
