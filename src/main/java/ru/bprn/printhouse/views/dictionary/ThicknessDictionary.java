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
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.service.ThicknessService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь вариантов плотности")
@Route(value = "thickness_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class ThicknessDictionary extends VerticalLayout{

        public ThicknessDictionary(ThicknessService thService) {

            DefaultCrudFormFactory<Thickness> formFactory = new DefaultCrudFormFactory<>(Thickness.class) {
                @Override
                protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                    Component nameField = (Component) fields.get(0);
                    formLayout.setColspan(nameField, 2);
                }
            };
            formFactory.setUseBeanValidation(true);
            formFactory.setVisibleProperties("thickness");

            GridCrud<Thickness> crud = new GridCrud<>(Thickness.class, new HorizontalSplitCrudLayout(), formFactory);
            crud.setClickRowToUpdate(true);
            crud.setUpdateOperationVisible(false);
            crud.getGrid().setColumns("thickness");

            setSizeFull();
            this.add(crud);

            crud.setOperations(
                    () -> thService.findAll(),
                    thickness -> thService.save(thickness),
                    thickness -> thService.save(thickness),
                    thickness -> thService.delete(thickness)
            );

            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
