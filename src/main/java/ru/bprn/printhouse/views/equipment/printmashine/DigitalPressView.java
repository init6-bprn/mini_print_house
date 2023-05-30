package ru.bprn.printhouse.views.equipment.printmashine;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.CheckBoxGroupProvider;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.data.service.SizeOfPrintLeafService;
import ru.bprn.printhouse.data.service.TypeOfPrinterService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Цифровые печатные машины")
@Route(value = "digital_print", layout = MainLayout.class)
@AnonymousAllowed

public class DigitalPressView extends VerticalLayout {

    public DigitalPressView(PrintMashineService pmService, TypeOfPrinterService topService, QuantityColorsService qcService, SizeOfPrintLeafService soplService) {
        GridCrud<PrintMashine> crud = new GridCrud<>(PrintMashine.class);

        crud.getGrid().setColumns("name", "typeOfPrinter", "quantityColors", "madeOfClicks", "maxPrintAreaX",
                "maxPrintAreaY");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("name", "quantityColors", "typeOfPrinter");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "cost", "clicks", "madeOfClicks", "maxPrintAreaX",
                "maxPrintAreaY", "priceOfCmykClick", "priceOfBlackClick", "priceOfSpotClick",
                "quantityColors", "typeOfPrinter", "sizeOfPrintLeaves");
        crud.getCrudFormFactory().setFieldProvider("quantityColors",
                new ComboBoxProvider<>(qcService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("typeOfPrinter",
                new ComboBoxProvider<>(topService.findAll()));
        //crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaves",
          //      new CheckBoxGroupProvider<>(soplService.findAll()));
        crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaves",
                new CheckBoxGroupProvider<>("sizeOfPrintLeaves", soplService.findAll(), SizeOfPrintLeaf::getName));


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
