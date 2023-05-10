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
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.data.service.TypeOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь типов материалов")
@Route(value = "type_of_material_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class TypeOfMaterialDictionary extends VerticalLayout{

        public TypeOfMaterialDictionary(TypeOfMaterialService tomService) {

            DefaultCrudFormFactory<TypeOfMaterial> formFactory = new DefaultCrudFormFactory<TypeOfMaterial>(TypeOfMaterial.class) {
                @Override
                protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                    Component nameField = (Component) fields.get(0);
                    formLayout.setColspan(nameField, 2);
                }
            };
            formFactory.setUseBeanValidation(true);
            formFactory.setVisibleProperties("name");

            GridCrud<TypeOfMaterial> crud = new GridCrud<>(TypeOfMaterial.class, new HorizontalSplitCrudLayout(), formFactory);
            crud.setClickRowToUpdate(true);
            crud.setUpdateOperationVisible(false);
            crud.getGrid().setColumns("name");

            setSizeFull();
            this.add(crud);

            crud.setOperations(
                    () -> tomService.findAll(),
                    user -> tomService.save(user),
                    user -> tomService.save(user),
                    user -> tomService.delete(user)
            );

            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
