package ru.bprn.printhouse.views.machine.pappercutters;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import ru.bprn.printhouse.data.entity.PaperCutter;
import ru.bprn.printhouse.data.service.PaperCutterService;
import ru.bprn.printhouse.data.service.SizeOfPrintLeafService;
import ru.bprn.printhouse.data.service.TypeOfPrinterService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Резаки")
@Route(value = "paper_cutters", layout = MainLayout.class)
@AnonymousAllowed

public class PaperCuttersView extends VerticalLayout {

    public PaperCuttersView(PaperCutterService pcService, TypeOfPrinterService topService, SizeOfPrintLeafService soplService) {
        GridCrud<PaperCutter> crud = new GridCrud<>(PaperCutter.class);

        crud.getGrid().setColumns("name", "maxSizeX", "maxSizeY", "finalCostOfCut");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("name");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "maxSizeX", "maxSizeY", "maxSizeZ",
                "gap", "costOfCutter", "cuts", "madeOfCuts", "costOfKnifeSharpening", "cutsToSharpening",
                "madeOfCutsBeforeSharpening", "finalCostOfCut");
        /*
        crud.getCrudFormFactory().setFieldProvider("typeOfPrinter",
                new ComboBoxProvider<>(topService.findAll()));

        crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaves", q -> {
                    MultiSelectComboBox<SizeOfPrintLeaf> mCombo = new MultiSelectComboBox<>();
                    mCombo.setItems(soplService.findAll());
                    mCombo.setItemLabelGenerator(SizeOfPrintLeaf::getName);
                    return mCombo;
                });
                //new CheckBoxGroupProvider<>("sizeOfPrintLeaves", soplService.findAll(), SizeOfPrintLeaf::getName));
*/

        this.add(crud);
        crud.setOperations(
                () -> pcService.findAll(),
                printer -> pcService.save(printer),
                printer -> pcService.save(printer),
                printer -> pcService.delete(printer)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");

    }
}
