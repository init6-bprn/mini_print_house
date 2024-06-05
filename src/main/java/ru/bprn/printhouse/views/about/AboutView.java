package ru.bprn.printhouse.views.about;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.data.service.TypeOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)

@AnonymousAllowed
public class AboutView extends VerticalLayout {


    public AboutView(MaterialService typeOfMaterialService) {

        Grid<Material> grid = new Grid<>(Material.class, false);
        grid.setItems(typeOfMaterialService.findAll());
        grid.addColumn(Material::getName).setHeader("Name");

        add(grid);

    }

}
