package ru.bprn.printhouse.views.material;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.machine.service.DigitalPrintingMachineService;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;
import ru.bprn.printhouse.views.material.service.PrintingMaterialService;

@PageTitle("Материал для печати")
@Route(value = "printing_materials", layout = MainLayout.class)
@AnonymousAllowed
public class PrintingMaterialView extends VerticalLayout {

    public  PrintingMaterialView(PrintingMaterialService service, DigitalPrintingMachineService mashineService) {
        GridCrud<PrintingMaterials> crud = new GridCrud<>(PrintingMaterials.class);

        crud.getGrid().setColumns("name", "id", "unitsOfMeasurement", "price");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("name");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "unitsOfMeasurement", "price", "wideOfOneClick", "abstractMachines");

        crud.getCrudFormFactory().setFieldProvider("abstractMachines",
                new ComboBoxProvider<>(mashineService.findAll()));

        this.add(crud);
        crud.setOperations(service::findAll, service::save, service::save, service::delete);

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");
    }

}
