package ru.bprn.printhouse.views.machine.printers;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;
import ru.bprn.printhouse.data.service.DigitalPrintingMachineService;
import ru.bprn.printhouse.data.service.PrintSpeedMaterialDensityService;
import ru.bprn.printhouse.data.service.ThicknessService;
import ru.bprn.printhouse.data.service.TypeOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Скорость печати в зависимости от плотности")
@Route(value = "print_speed_material_density", layout = MainLayout.class)
@AnonymousAllowed

public class PrintSpeedMaterialDensityView extends VerticalLayout {

    public PrintSpeedMaterialDensityView(PrintSpeedMaterialDensityService psmdService, DigitalPrintingMachineService pmService,
                                         TypeOfMaterialService tomService, ThicknessService thService){
        var filter = new ComboBox<PrintMashine>();
        filter.setItems(pmService.findAll());
        filter.setItemLabelGenerator(PrintMashine::getName);

        GridCrud<PrintSpeedMaterialDensity> crud = new GridCrud<>(PrintSpeedMaterialDensity.class);
        crud.getCrudLayout().addFilterComponent(filter);

        crud.getGrid().setColumns("typeOfMaterial", "density", "speed");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("typeOfMaterial", "density", "speed");

        crud.getCrudFormFactory().setUseBeanValidation(true);

        crud.getCrudFormFactory().setVisibleProperties("printMashine", "typeOfMaterial", "density", "speed");
        crud.getCrudFormFactory().setFieldProvider("typeOfMaterial",
                new ComboBoxProvider<>(tomService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("printMashine",
                new ComboBoxProvider<>(pmService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("density",
                new ComboBoxProvider<>(thService.findAll()));

        this.add(crud);

        crud.setOperations(
                () -> psmdService.findPrintSpeedMaterialDensitiesByPrintMashine(filter.getValue()),
                psmd -> psmdService.save(psmd),
                psmd -> psmdService.save(psmd),
                psmd -> psmdService.delete(psmd)
        );

        filter.addValueChangeListener(e -> crud.refreshGrid());

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        getStyle().set("text-align", "center");

    }
}
