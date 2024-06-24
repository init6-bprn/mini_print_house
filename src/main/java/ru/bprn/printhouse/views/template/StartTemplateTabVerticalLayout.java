package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;

import java.util.List;

public class StartTemplateTabVerticalLayout extends VerticalLayout{

    private final StandartSizeService standartSizeService;
    private final TypeOfMaterialService typeOfMaterialService;
    private final MaterialService materialService;
    private final GapService gapService;
    private final ImposeCaseService imposeCaseService;

    private final Template template;

    private final TextField nameOfTemplate = new TextField("Название шаблона: ");

    public StartTemplateTabVerticalLayout(Template template, StandartSizeService standartSizeService, TypeOfMaterialService TypeOfMaterialService,
                                          MaterialService materialService, GapService gapService, ImposeCaseService imposeCaseService){
        this.standartSizeService = standartSizeService;
        this.typeOfMaterialService = TypeOfMaterialService;
        this.materialService = materialService;
        this.gapService = gapService;
        this.imposeCaseService = imposeCaseService;
        this.template = template;

        setSizeFull();
        nameOfTemplate.setSizeFull();
        this.add(nameOfTemplate);

        addSizeOfProductSection();
        addSetOfSheetsSection();
        addMaterialSection();

        nameOfTemplate.setValue(setDefaultName());
    }

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
        List<Material> list = materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue());
        grid.setItems(list);
        list.stream().findFirst().ifPresent(template::setMaterial);
        grid.select(template.getMaterial());

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(typeColumn).setComponent(typeOfMaterialCombo);
        headerRow.getCell(sizeColumn).setComponent(sizeOfPrintLeafCombo);
        headerRow.getCell(thicknessColumn).setComponent(thicknessCombo);

        grid.addSelectionListener(selectionEvent -> {
            selectionEvent.getFirstSelectedItem().ifPresent(template::setMaterial);
            nameOfTemplate.setValue(setDefaultName());
        });

        this.add(grid);
    }

    private void addSetOfSheetsSection(){
        var ha = new HorizontalLayout();

        var sheetsQuantity = new IntegerField("Страниц в тетради");
        sheetsQuantity.setValue(1);
        sheetsQuantity.addValueChangeListener(e->template.setQuantityOfLeaves(e.getValue()));

        var imposeCaseCombo = new ComboBox<ImposeCase>("Вариант спуска полос:");
        imposeCaseCombo.setItems(imposeCaseService.findAll());
        imposeCaseCombo.addValueChangeListener(e->{
            template.setImposeCase(e.getValue());
            if (template.getImposeCase().getName().equals("Однолистовое")) {
                sheetsQuantity.setValue(1);
                sheetsQuantity.setEnabled(false);
            }
            else sheetsQuantity.setEnabled(true);
        });

        ha.add(imposeCaseCombo, sheetsQuantity);
        this.add(ha);
    }

    private <T> void comboBoxViewFirstElement(ComboBox<T> combo) {
        if (combo!=null)
            if (combo.getListDataView().getItemCount()>0)
                combo.setValue(combo.getListDataView().getItem(0));
    }

    private void addSizeOfProductSection() {
        var hLayout = new HorizontalLayout();

        var length = new NumberField();
        length.setLabel("Длина");
        length.addValueChangeListener(e->template.setSizeX(e.getValue()));

        var width = new NumberField();
        width.setLabel("Ширина");
        width.addValueChangeListener(e->template.setSizeY(e.getValue()));

        var sizeOfPaperCombo = new ComboBox<StandartSize>();
        sizeOfPaperCombo.setItems(standartSizeService.findAll());
        comboBoxViewFirstElement(sizeOfPaperCombo);
        template.setStandartSize(sizeOfPaperCombo.getValue());
        sizeOfPaperCombo.setLabel("Размер изделия");
        sizeOfPaperCombo.setAllowCustomValue(false);
        sizeOfPaperCombo.addValueChangeListener(e -> {
            template.setStandartSize(e.getValue());
            template.setSizeX(e.getValue().getLength());
            template.setSizeY(e.getValue().getWidth());
            length.setValue(template.getSizeX());
            width.setValue(template.getSizeY());
            nameOfTemplate.setValue(setDefaultName());
        }) ;

        var dialog = new SizeDialog(standartSizeService);

        dialog.addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened()) {
                if (dialog.getStandartSize()!= null) {
                    sizeOfPaperCombo.getDataProvider().refreshAll();
                    sizeOfPaperCombo.setValue(dialog.getStandartSize());

                }
            }
        });

        var label = new NativeLabel("Добавить");
        label.getStyle().set("padding-top", "var(--lumo-space-s)")
                .set("font-size", "var(--lumo-font-size-xs)");
        var addSizeButton = new Button("Add");
        addSizeButton.setAriaLabel("Add");
        var layout = new VerticalLayout(label, addSizeButton);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");
        addSizeButton.addClickListener(e-> {
            dialog.setX(length.getValue());
            dialog.setY(width.getValue());
            dialog.setModal(true);
            dialog.open();
        });

        var bleedCombo = new ComboBox<Gap>("Припуск");
        bleedCombo.setItems(gapService.findAllBleeds("Bleed"));
        comboBoxViewFirstElement(bleedCombo);
        template.setGap(bleedCombo.getValue());
        bleedCombo.addValueChangeListener(e->{
            template.setGap(e.getValue());
            nameOfTemplate.setValue(setDefaultName());
        });

        hLayout.add(sizeOfPaperCombo, length, width, bleedCombo, layout, dialog);
        this.add(hLayout);
    }

    private String setDefaultName() {
        if ((template.getStandartSize()!=null)&(template.getMaterial()!=null)&(template.getGap()!=null)) {
            template.setName(template.getStandartSize().getName()+" - "+template.getMaterial().getName()+" - "+
                    template.getMaterial().getThickness().toString()+"г. - "+template.getMaterial().getSizeOfPrintLeaf()+
                    " - "+template.getGap().getName());
        }
        else template.setName("Заполните все поля!");
        return template.getName();
    }

}
