package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.views.material.entity.Material;

import java.util.Set;

@Getter
@Setter
public class SelectMaterailsDialog extends Dialog {

    private Grid<Material> grid = new Grid<>();

    public SelectMaterailsDialog(String title){
        super(title);
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(false);
        this.setModal(true);
        this.setHeight("75%");
        this.setWidth("75%");

        var saveButton = new Button("Ok", buttonClickEvent -> this.close());
        this.getFooter().add(saveButton);

        var layout = new VerticalLayout();
        layout.setSizeFull();



        grid.addColumn(Material::getName).setHeader("Название");
        grid.addColumn(Material::getTypeOfMaterial).setHeader("Тип материала");
        grid.addColumn(Material::getSizeOfPrintLeaf).setHeader("Размер печатного листа");
        grid.addColumn(Material::getThickness).setHeader("Плотность");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        grid.getHeaderRows().clear();
        grid.getListDataView();


        layout.add(grid);
        this.add(layout);
    }

    public void setSelectedMaterial(Set<Material> materials) {
        if (!materials.isEmpty()) for (Material m : materials) grid.select(m);
    }

}
