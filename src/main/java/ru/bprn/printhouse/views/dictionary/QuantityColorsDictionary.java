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
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь количества цветов")
@Route(value = "quantity_colors_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class QuantityColorsDictionary extends VerticalLayout{

        public QuantityColorsDictionary(QuantityColorsService qcService) {

            DefaultCrudFormFactory<QuantityColors> formFactory = new DefaultCrudFormFactory<>(QuantityColors.class) {
                @Override
                protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                    Component nameField = (Component) fields.get(0);
                    formLayout.setColspan(nameField, 2);
                }
            };
            formFactory.setUseBeanValidation(true);
            formFactory.setVisibleProperties("name");

            GridCrud<QuantityColors> crud = new GridCrud<>(QuantityColors.class, new HorizontalSplitCrudLayout(), formFactory);
            crud.setClickRowToUpdate(true);
            crud.setUpdateOperationVisible(false);
            crud.getGrid().setColumns("name");

            setSizeFull();
            this.add(crud);

            crud.setOperations(
                    () -> qcService.findAll(),
                    user -> qcService.save(user),
                    user -> qcService.save(user),
                    user -> qcService.delete(user)
            );

            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
