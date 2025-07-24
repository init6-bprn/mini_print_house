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
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Скорость печати на разных материалах")
@Route(value = "time_of_digital_printing_operation", layout = MainLayout.class)
@AnonymousAllowed
public class TimeOfDigitalPrintOperationDictionary extends VerticalLayout {

    public TimeOfDigitalPrintOperationDictionary(PrintSpeedMaterialDensityService service,
                                                 PrintMashineService printMashineService,
                                                 ThicknessService thicknessService,
                                                 SizeOfPrintLeafService sizeOfPrintLeafService){
        super();
        DefaultCrudFormFactory<PrintSpeedMaterialDensity> formFactory = new DefaultCrudFormFactory<>(PrintSpeedMaterialDensity.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("printMashine", "sizeOfPrintLeaf", "thickness", "timeOfOperation");

        GridCrud<PrintSpeedMaterialDensity> crud = new GridCrud<>(PrintSpeedMaterialDensity.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("printMashine", "sizeOfPrintLeaf", "thickness", "timeOfOperation");

        crud.getCrudFormFactory().setFieldProvider("printMashine",
                new ComboBoxProvider<>(printMashineService.findAll()));

        crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaf",
                new ComboBoxProvider<>(sizeOfPrintLeafService.findAll()));

        crud.getCrudFormFactory().setFieldProvider("thickness",
                new ComboBoxProvider<>(thicknessService.findAll()));

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                service::findAll,
                service::save,
                service::save,
                service::delete
        );

        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        getStyle().set("text-align", "center");
    }


}
