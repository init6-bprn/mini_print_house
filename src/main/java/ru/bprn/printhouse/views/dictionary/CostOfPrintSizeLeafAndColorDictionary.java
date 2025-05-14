package ru.bprn.printhouse.views.dictionary;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;
import ru.bprn.printhouse.data.entity.CostOfPrintSizeLeafAndColor;
import ru.bprn.printhouse.data.service.CostOfPrintSizeLeafAndColorService;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.data.service.SizeOfPrintLeafService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.List;

@PageTitle("Словарь стоимости отпечатка")
@Route(value = "Cost_Of_Print_Size_Leaf_And_Color_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class CostOfPrintSizeLeafAndColorDictionary extends VerticalLayout{

        public CostOfPrintSizeLeafAndColorDictionary(CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService,
                                                     PrintMashineService printMashineService, SizeOfPrintLeafService sizeOfPrintLeafService,
                                                     QuantityColorsService quantityColorsService) {

            DefaultCrudFormFactory<CostOfPrintSizeLeafAndColor> formFactory = new DefaultCrudFormFactory<>(CostOfPrintSizeLeafAndColor.class) {
                @Override
                protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                    Component nameField = (Component) fields.get(0);
                    formLayout.setColspan(nameField, 2);
                }
            };
            formFactory.setUseBeanValidation(true);
            formFactory.setVisibleProperties("printMashine", "sizeOfPrintLeaf", "quantityColors", "cost");

            GridCrud<CostOfPrintSizeLeafAndColor> crud = new GridCrud<>(CostOfPrintSizeLeafAndColor.class, new HorizontalSplitCrudLayout(), formFactory);
            crud.setClickRowToUpdate(true);
            crud.setUpdateOperationVisible(false);
            crud.getGrid().setColumns("printMashine", "sizeOfPrintLeaf", "quantityColors", "cost");

            crud.getCrudFormFactory().setFieldProvider("printMashine",
                    new ComboBoxProvider<>(printMashineService.findAll()));

            crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaf",
                    new ComboBoxProvider<>(sizeOfPrintLeafService.findAll()));

            crud.getCrudFormFactory().setFieldProvider("quantityColors",
                    new ComboBoxProvider<>(quantityColorsService.findAll()));

            setSizeFull();
            this.add(crud);

            crud.setOperations(
                    () -> costOfPrintSizeLeafAndColorService.findAll(),
                    user -> costOfPrintSizeLeafAndColorService.save(user),
                    user -> costOfPrintSizeLeafAndColorService.save(user),
                    user -> costOfPrintSizeLeafAndColorService.delete(user)
            );

            setJustifyContentMode(JustifyContentMode.CENTER);
            setDefaultHorizontalComponentAlignment(Alignment.START);
            getStyle().set("text-align", "center");
        }


}
