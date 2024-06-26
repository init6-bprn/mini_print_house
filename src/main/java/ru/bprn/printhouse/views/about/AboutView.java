package ru.bprn.printhouse.views.about;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
//@RouteAlias(value = "", layout = MainLayout.class)

@AnonymousAllowed
public class AboutView extends VerticalLayout {


    public AboutView(MaterialService typeOfMaterialService) {

        Grid<Material> grid = new Grid<>(Material.class, false);
        grid.setItems(typeOfMaterialService.findAll());
        grid.addColumn(Material::getName).setHeader("Name");

        add(grid);

    }

}
