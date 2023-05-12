package ru.bprn.printhouse.views.equipment.printmashine;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.data.service.TypeOfPrinterService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Цифровые печатные машины")
@Route(value = "digital_print", layout = MainLayout.class)
@AnonymousAllowed

public class DigitalPressView extends VerticalLayout {

    public DigitalPressView(PrintMashineService pmService, TypeOfPrinterService topService, QuantityColorsService qcService) {
        GridCrud<PrintMashine> crud = new GridCrud<>(PrintMashine.class);
        crud.getGrid().setColumns("name", "cost", "click", "madeClick", "maxPrintAreaX",
                "maxPrintAreaY", "priceOfCMYKClick", "priceOfBlackClick", "priceOfSpotClick",
                "quantityColors", "typeOfPrinter");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("name", "quantityColors", "typeOfPrinter");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "cost", "click", "madeClick", "maxPrintAreaX",
                "maxPrintAreaY", "priceOfCMYKClick", "priceOfBlackClick", "priceOfSpotClick",
                "quantityColors", "typeOfPrinter");
        crud.getCrudFormFactory().setFieldProvider("quantityColors",
                new ComboBoxProvider<>(qcService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("typeOfPrinter",
                new ComboBoxProvider<>(topService.findAll()));

        this.add(crud);
        crud.setOperations(
                () -> pmService.findAll(),
                user -> pmService.save(user),
                user -> pmService.save(user),
                user -> pmService.delete(user)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");

    }
}
