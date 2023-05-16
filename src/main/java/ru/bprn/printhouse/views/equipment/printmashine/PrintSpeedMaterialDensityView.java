package ru.bprn.printhouse.views.equipment.printmashine;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.PrintSpeedMaterialDensityService;
import ru.bprn.printhouse.data.service.TypeOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Скорость печати в зависимости от плотности")
@Route(value = "print_speed_material_density", layout = MainLayout.class)
@AnonymousAllowed

public class PrintSpeedMaterialDensityView extends VerticalLayout {

    public PrintSpeedMaterialDensityView(PrintSpeedMaterialDensityService psmdService, PrintMashineService pmService, TypeOfMaterialService tomService){
        var filter = new ComboBox<PrintMashine>("Выберите принтер:");
        filter.setItems(pmService.findAll());
        this.add(filter);

        GridCrud<PrintSpeedMaterialDensity> crud = new GridCrud<>(PrintSpeedMaterialDensity.class);
        crud.getCrudLayout().addFilterComponent(filter);

        crud.getGrid().setColumns("typeOfMaterial", "speed", "densityNoMore");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("typeOfMaterial", "speed", "densityNoMore");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties( CrudOperation.ADD,"printMachine", "typeOfMaterial", "speed", "densityNoMore");
        crud.getCrudFormFactory().setVisibleProperties("typeOfMaterial", "speed", "densityNoMore");
        crud.getCrudFormFactory().setFieldProvider("typeOfMaterial",
                new ComboBoxProvider<>(tomService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("printMashine",
                new ComboBoxProvider<>(pmService.findAll()));

        this.add(crud);

        crud.setOperations(
                () -> psmdService.findPrintSpeedMaterialDensitiesByPrintMashine((PrintMashine) filter.getValue()),
                user -> psmdService.save(user),
                user -> psmdService.save(user),
                user -> psmdService.delete(user)
        );

        filter.addValueChangeListener(e -> crud.refreshGrid());

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        getStyle().set("text-align", "center");

    }
}
