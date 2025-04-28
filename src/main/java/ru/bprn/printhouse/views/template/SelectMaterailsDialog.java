package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.Material;

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
/*
    private void addMaterialSection() {

        var typeOfMaterialCombo = new ComboBox<TypeOfMaterial>();
        var thicknessCombo = new ComboBox<Thickness>();
        var sizeOfPrintLeafCombo = new ComboBox<SizeOfPrintLeaf>();
        var grid = new Grid<>(Material.class, false);

        typeOfMaterialCombo.setItems(typeOfMaterialService.findAll());
        typeOfMaterialCombo.setAllowCustomValue(false);
        typeOfMaterialCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                thicknessCombo.setItems(materialService.findAllThicknessByTypeOfMaterial(e.getValue()));
                comboBoxViewFirstElement(thicknessCombo);
                sizeOfPrintLeafCombo.setItems(materialService.findAllSizeOfPrintLeafByTypeOfMaterial(e.getValue()));
                comboBoxViewFirstElement(sizeOfPrintLeafCombo);
            }
            grid.setItems(materialService.findByFilters(e.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue()));
        });

        var listThickness = materialService.findAllThicknessByTypeOfMaterial(typeOfMaterialCombo.getValue());
        if (listThickness!=null) thicknessCombo.setItems(listThickness);
        thicknessCombo.setAllowCustomValue(false);
        thicknessCombo.addValueChangeListener(e->{
            grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), e.getValue()));
        });

        var listSizeOfPrintLeaf = materialService.findAllSizeOfPrintLeafByTypeOfMaterial(typeOfMaterialCombo.getValue());
        if (listSizeOfPrintLeaf!=null) sizeOfPrintLeafCombo.setItems(listSizeOfPrintLeaf);
        sizeOfPrintLeafCombo.setAllowCustomValue(false);
        sizeOfPrintLeafCombo.addValueChangeListener(e->{
            grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), e.getValue(), thicknessCombo.getValue()));
        });

        grid.addColumn(Material::getName).setHeader("Название");
        Grid.Column<Material> typeColumn = grid.addColumn(Material::getTypeOfMaterial).setHeader("Тип материала");
        Grid.Column<Material> sizeColumn = grid.addColumn(Material::getSizeOfPrintLeaf).setHeader("Размер печатного листа");
        Grid.Column<Material> thicknessColumn = grid.addColumn(Material::getThickness).setHeader("Плотность");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("270px");
        grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue()));

        //templateBinder.forField(grid.).bind(WorkFlow::getMaterial, WorkFlow::setMaterial);
        templateBinder.forField(grid.asSingleSelect()).
                withValidator(Objects::nonNull, "Выделите строку!")
                .bind(WorkFlow::getMaterial, WorkFlow::setMaterial);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(typeColumn).setComponent(typeOfMaterialCombo);
        headerRow.getCell(sizeColumn).setComponent(sizeOfPrintLeafCombo);
        headerRow.getCell(thicknessColumn).setComponent(thicknessCombo);

        grid.addSelectionListener(selectionEvent -> {
            if (autoNamed.getValue()) {
                templateBinder.getBean().setName(setDefaultName());
                templateBinder.refreshFields();
            }
        });

        this.add(grid);
    }

 */
}
