package ru.bprn.printhouse.views.machine.printers;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Цифровые печатные машины")
@Route(value = "digital_print", layout = MainLayout.class)
@AnonymousAllowed

public class PrintersView extends VerticalLayout {

    @Autowired
    public PrintersView(MaterialService maService, PrintMashineService pmService,TypeOfPrinterService topService,
                        QuantityColorsService qcService, SizeOfPrintLeafService soplService, GapService gapService) {
        GridCrud<PrintMashine> crud = new GridCrud<>(PrintMashine.class);

        crud.getGrid().setColumns("name", "typeOfPrinter", "quantityColors", "madeOfClicks", "maxPrintAreaX",
                "maxPrintAreaY");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("name", "quantityColors", "typeOfPrinter");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "cost", "clicks", "madeOfClicks", "maxPrintAreaX",
                "maxPrintAreaY", "priceOfCmykClick", "priceOfBlackClick", "priceOfSpotClick",
                "quantityColors", "typeOfPrinter", "sizeOfPrintLeaves", "materials", "gap", "hasDuplex");

        crud.getCrudFormFactory().setFieldProvider("quantityColors",q -> {
            MultiSelectComboBox<QuantityColors> mCombo = new MultiSelectComboBox<>();
            mCombo.setItems(qcService.findAll());
            mCombo.setItemLabelGenerator(QuantityColors::getName);
            return mCombo;
        });

        crud.getCrudFormFactory().setFieldProvider("typeOfPrinter",
                new ComboBoxProvider<>(topService.findAll()));

        crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaves", q -> {
                    MultiSelectComboBox<SizeOfPrintLeaf> mCombo = new MultiSelectComboBox<>();
                    mCombo.setItems(soplService.findAll());
                    mCombo.setItemLabelGenerator(SizeOfPrintLeaf::getName);
                    return mCombo;
                });

        crud.getCrudFormFactory().setFieldProvider("materials", q -> {
            MultiSelectComboBox<Material> mCombo = new MultiSelectComboBox<>();
            mCombo.setItems(maService.findAll());
            mCombo.setItemLabelGenerator(Material::getName);
            mCombo.setReadOnly(true);
            return mCombo;
        });

        crud.getCrudFormFactory().setFieldProvider("gap",
                new ComboBoxProvider<>(gapService.findAll()));

        this.add(crud);
        crud.setOperations(
                () -> pmService.findAll(),
                printer -> pmService.save(printer),
                printer -> pmService.save(printer),
                printer -> pmService.delete(printer)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");

    }
}
