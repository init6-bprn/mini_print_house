package ru.bprn.printhouse.views.material;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.data.service.SizeOfPrintLeafService;
import ru.bprn.printhouse.data.service.ThicknessService;
import ru.bprn.printhouse.data.service.TypeOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Материал для печати")
@Route(value = "materials", layout = MainLayout.class)
@AnonymousAllowed

public class MaterialView extends VerticalLayout {

    public MaterialView(MaterialService maService, TypeOfMaterialService tomService,
                        SizeOfPrintLeafService soplService, ThicknessService thService) {

        GridCrud<Material> crud = new GridCrud<>(Material.class);

        crud.getGrid().setColumns("typeOfMaterial", "name", "sizeOfPrintLeaf", "thickness");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().setSortableColumns("name");

        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("typeOfMaterial", "name", "sizeOfPrintLeaf", "thickness");

        crud.getCrudFormFactory().setFieldProvider("typeOfMaterial",
                new ComboBoxProvider<>(tomService.findAll()));

        crud.getCrudFormFactory().setFieldProvider("sizeOfPrintLeaf",
                new ComboBoxProvider<>(soplService.findAll()));

        crud.getCrudFormFactory().setFieldProvider("thickness",
                new ComboBoxProvider<>(thService.findAll()));

        this.add(crud);
        crud.setOperations(
                () -> maService.findAll(),
                material -> maService.save(material),
                material -> maService.save(material),
                material -> maService.delete(material)
        );

        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.START);
        getStyle().set("text-align", "center");

    }
}
