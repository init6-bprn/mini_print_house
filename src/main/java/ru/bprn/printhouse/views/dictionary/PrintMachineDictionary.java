package ru.bprn.printhouse.views.dictionary;


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

@PageTitle("Словарь печатных машин")
@Route(value = "print_machine_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class PrintMachineDictionary extends VerticalLayout{

        public PrintMachineDictionary(PrintMashineService pmService, QuantityColorsService qcService, TypeOfPrinterService topService) {

            GridCrud<PrintMashine> crud = new GridCrud<>(PrintMashine.class);
            crud.getGrid().setColumns("name", "quantityColors", "typeOfPrinter");
            crud.getGrid().setColumnReorderingAllowed(true);
            crud.getGrid().setSortableColumns("name", "quantityColors", "typeOfPrinter");

            crud.getCrudFormFactory().setUseBeanValidation(true);
            crud.getCrudFormFactory().setVisibleProperties("name", "quantityColors", "typeOfPrinter");
            crud.getCrudFormFactory().setFieldProvider("quantityColors",
                    new ComboBoxProvider<>(qcService.findAll()));
            crud.getCrudFormFactory().setFieldProvider("typeOfPrinter",
                    new ComboBoxProvider<>(topService.findAll()));

            this.add(crud);

            crud.setOperations(
                    () -> pmService.findAll(),
                    printMashine -> pmService.save(printMashine),
                    printMashine -> pmService.save(printMashine),
                    printMashine -> pmService.delete(printMashine)
            );

            setSizeFull();
            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
