package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.SizeOfPrintLeafService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь размера печатных листов")
@Route(value = "size_of_print_leaf_dictionary", layout = MainLayout.class)
@AnonymousAllowed

public class SizeOfPrintLeafDictionary extends VerticalLayout {

    public SizeOfPrintLeafDictionary (SizeOfPrintLeafService sizeOfPrintLeafService, PrintMashineService pmService) {

        DefaultCrudFormFactory<SizeOfPrintLeaf> formFactory = new DefaultCrudFormFactory<SizeOfPrintLeaf>(SizeOfPrintLeaf.class) {
            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Component nameField = (Component) fields.get(0);
                formLayout.setColspan(nameField, 2);
            }
        };
        formFactory.setUseBeanValidation(true);
        formFactory.setVisibleProperties("name", "length", "width", "printMashineSet");

        GridCrud<SizeOfPrintLeaf> crud = new GridCrud<>(SizeOfPrintLeaf.class, new HorizontalSplitCrudLayout(), formFactory);
        crud.setClickRowToUpdate(true);
        crud.setUpdateOperationVisible(false);
        crud.getGrid().setColumns("name", "length", "width");

        crud.getCrudFormFactory().setFieldProvider("printMashineSet", q -> {
            MultiSelectComboBox<PrintMashine> mCombo = new MultiSelectComboBox<>();
            mCombo.setItems(pmService.findAll());
            mCombo.setItemLabelGenerator(PrintMashine::getName);
            return mCombo;
        });

                //new CheckBoxGroupProvider<>("Print_Mashine", pmService.findAll(), PrintMashine::getName));

        setSizeFull();
        this.add(crud);

        crud.setOperations(
                () -> sizeOfPrintLeafService.findAll(),
               leaf ->  //{leaf.getPrintMashineSet().
                    sizeOfPrintLeafService.save(leaf),
                leaf -> sizeOfPrintLeafService.save(leaf),
                leaf -> sizeOfPrintLeafService.delete(leaf)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");
    }


}
