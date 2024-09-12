package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;

@UIScope
@AnonymousAllowed
public class StartTabOfWorkFlowVerticalLayout extends VerticalLayout implements HasBinder{

    private final StandartSizeService standartSizeService;
    private final TypeOfMaterialService typeOfMaterialService;
    private final MaterialService materialService;
    private final GapService gapService;
    private final ImposeCaseService imposeCaseService;
    private final ObjectMapper objectMapper;

    @Getter
    private final BeanValidationBinder<WorkFlow> templateBinder;

    private final TextField nameOfTemplate = new TextField("Название шаблона: ");

    @Autowired
    public StartTabOfWorkFlowVerticalLayout(StandartSizeService standartSizeService, TypeOfMaterialService TypeOfMaterialService,
                                            MaterialService materialService, GapService gapService,
                                            ImposeCaseService imposeCaseService){
        this.standartSizeService = standartSizeService;
        this.typeOfMaterialService = TypeOfMaterialService;
        this.materialService = materialService;
        this.gapService = gapService;
        this.imposeCaseService = imposeCaseService;
        objectMapper = new ObjectMapper();

        templateBinder = new BeanValidationBinder<>(WorkFlow.class);

        setSizeUndefined();
        nameOfTemplate.setSizeFull();
        templateBinder.bind(nameOfTemplate, WorkFlow::getName, WorkFlow::setName);
        this.add(nameOfTemplate);

        addSizeOfProductSection();
        addSetOfSheetsSection();
        addMaterialSection();
        addQuantityAndOrientation();
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
        grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue()));

        templateBinder.forField(grid.asSingleSelect()).asRequired().bind(WorkFlow::getMaterial, WorkFlow::setMaterial);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(typeColumn).setComponent(typeOfMaterialCombo);
        headerRow.getCell(sizeColumn).setComponent(sizeOfPrintLeafCombo);
        headerRow.getCell(thicknessColumn).setComponent(thicknessCombo);

        grid.addSelectionListener(selectionEvent -> {
            nameOfTemplate.setValue(setDefaultName());
        });

        this.add(grid);
    }

    private void addSetOfSheetsSection(){
        var ha = new HorizontalLayout();

        var sheetsQuantity = new IntegerField("Страниц/листов");
        sheetsQuantity.setValue(1);
        templateBinder.bind(sheetsQuantity, WorkFlow::getQuantityOfLeaves, WorkFlow::setQuantityOfLeaves);

        var imposeCaseCombo = new ComboBox<ImposeCase>("Вариант спуска полос:");
        imposeCaseCombo.setItems(imposeCaseService.findAll());
        templateBinder.forField(imposeCaseCombo).asRequired().bind(WorkFlow::getImposeCase, WorkFlow::setImposeCase);

        imposeCaseCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                if (e.getValue().getName().equals("Однолистовое")) {
                    sheetsQuantity.setValue(1);
                    sheetsQuantity.setEnabled(false);
                } else sheetsQuantity.setEnabled(true);
            }
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
        templateBinder.bind(length, WorkFlow::getSizeX, WorkFlow::setSizeX);

        var width = new NumberField();
        width.setLabel("Ширина");
        templateBinder.bind(width, WorkFlow::getSizeY, WorkFlow::setSizeY);

        var sizeOfPaperCombo = new ComboBox<StandartSize>();
        sizeOfPaperCombo.setItems(standartSizeService.findAll());

        templateBinder.forField(sizeOfPaperCombo).asRequired().bind(WorkFlow::getStandartSize, WorkFlow::setStandartSize);

        sizeOfPaperCombo.setLabel("Размер изделия");
        sizeOfPaperCombo.setAllowCustomValue(false);
        sizeOfPaperCombo.addValueChangeListener(e -> {
            if (e.getValue()!=null) {
                length.setValue(e.getValue().getLength());
                width.setValue(e.getValue().getWidth());
                nameOfTemplate.setValue(setDefaultName());
            }
        }) ;

        var dialog = new SizeDialog(standartSizeService);

        dialog.addOpenedChangeListener(openedChangeEvent -> {
            if (!openedChangeEvent.isOpened()) {
                if (dialog.getStandartSize()!= null) {
                    sizeOfPaperCombo.setItems(standartSizeService.findAll());
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
            if ((length.getValue()!=null)&&(width.getValue()!=null)) {
                dialog.setX(length.getValue());
                dialog.setY(width.getValue());
                dialog.setModal(true);
                dialog.open();
            }
        });

        var bleedCombo = new ComboBox<Gap>("Припуск");
        bleedCombo.setItems(gapService.findAllBleeds("Bleed"));

        templateBinder.forField(bleedCombo).asRequired().bind(WorkFlow::getBleed, WorkFlow::setBleed);
        bleedCombo.addValueChangeListener(e->{
            nameOfTemplate.setValue(setDefaultName());
        });

        hLayout.add(sizeOfPaperCombo, length, width, layout, bleedCombo, dialog);
        this.add(hLayout);
    }

    private String setDefaultName() {
        if (templateBinder.getBean()!= null) {
            templateBinder.getBean().setName("auto");
            return templateBinder.getBean().getName();
        }
        return  "null";
    }

    private void addQuantityAndOrientation() {
        var hLayout = new HorizontalLayout();
        var radioGroup = new RadioButtonGroup<>("Ориентация");
        var rowsOnLeaf = new IntegerField("Колонок:");
        var columnsOnLeaf = new IntegerField("Столбцов:");
        var quantityOfPrintLeaves = new IntegerField("Изделий на листе:");
        var quantityOfProduction = new IntegerField("Тираж:");
        var quantityProductionsOnLeaf = new IntegerField("Листаж:");
        var gapComboBox = new ComboBox<Gap>("Отступы от краев материала:");

        gapComboBox.setItems(gapService.findAllByNameNotContaining("Bleed"));
        gapComboBox.setValue(gapService.findByName("Zero"));

        templateBinder.bind(gapComboBox, WorkFlow::getGap, WorkFlow::setGap);

        radioGroup.setItems("Автоматически", "Вертикальная", "Горизонтальная");
        radioGroup.setValue("Автоматически");

        templateBinder.bind(rowsOnLeaf, WorkFlow::getListRows, WorkFlow::setListRows);
        templateBinder.bind(columnsOnLeaf, WorkFlow::getListColumns, WorkFlow::setListColumns);
        templateBinder.bind(quantityOfPrintLeaves, WorkFlow::getQuantityOfPrintLeaves, WorkFlow::setQuantityOfPrintLeaves);
        templateBinder.bind(quantityOfProduction, WorkFlow::getQuantityOfProduct, WorkFlow::setQuantityOfProduct);
        templateBinder.bind(quantityProductionsOnLeaf, WorkFlow::getQuantityProductionsOnLeaf, WorkFlow::setQuantityProductionsOnLeaf);
        //templateBinder.bind(radioGroup, WorkFlow::getOrientation, WorkFlow::setOrientation);

        radioGroup.addValueChangeListener(e-> {
            var mass = calculateAndSetQuantity(e.getValue().toString());
            rowsOnLeaf.setValue(mass[0]);
            columnsOnLeaf.setValue(mass[1]);
            quantityOfPrintLeaves.setValue(mass[2]);
        });
        add(radioGroup);

        quantityOfPrintLeaves.setReadOnly(true);
        rowsOnLeaf.setReadOnly(true);
        columnsOnLeaf.setReadOnly(true);
        hLayout.add(radioGroup);
        var hl = new HorizontalLayout();
        hl.add(rowsOnLeaf,columnsOnLeaf,quantityOfPrintLeaves, quantityOfProduction, quantityProductionsOnLeaf, gapComboBox);
        this.add(hLayout,hl);

    }


    private int[] calculateAndSetQuantity(String str){
        var bean = templateBinder.getBean();
        Gap margins = this.getMargins();
        int[] mass = {1,1,1};

        if (bean!=null){
            if ((bean.getMaterial().getSizeOfPrintLeaf()!=null)
                    & (bean.getSizeX()!=null)
                    & (bean.getSizeY()!=null)
                    & (bean.getGap()!=null)) {
                int printSizeX = bean.getMaterial().getSizeOfPrintLeaf().getLength() - margins.getGapLeft() - margins.getGapRight();
                int printSizeY = bean.getMaterial().getSizeOfPrintLeaf().getWidth() - margins.getGapTop() - margins.getGapBottom();
                Double fullSizeX = bean.getSizeY() + bean.getBleed().getGapTop() + bean.getBleed().getGapBottom();
                Double fullSizeY = bean.getSizeX() + bean.getBleed().getGapLeft() + bean.getBleed().getGapRight();

                var mass1 = getQuantity(printSizeX, printSizeY, fullSizeX, fullSizeY);
                var mass2 = getQuantity(printSizeX, printSizeY, fullSizeY, fullSizeX);

                switch (str) {
                    case "Автоматически":
                        if (mass1[2] >= mass2[2]) mass = mass1;
                            else mass = mass2;
                        break;
                    case "Вертикальная":
                        mass = mass1;
                        break;
                    case "Горизонтальная":
                        mass = mass2;
                        break;
                }

            }
        }
        return mass;
    }

    private Gap getMargins() {
        var margins = new Gap(0,0,0,0);

        this.getChildren().filter(HasMargins.class::isInstance).forEach(component -> {
            if (margins.getGapTop() < ((HasMargins) component).getMargins().getGapTop()) margins.setGapTop(((HasMargins) component).getMargins().getGapTop());
            if (margins.getGapBottom() < ((HasMargins) component).getMargins().getGapBottom()) margins.setGapBottom(((HasMargins) component).getMargins().getGapBottom());
            if (margins.getGapLeft() < ((HasMargins) component).getMargins().getGapLeft()) margins.setGapLeft(((HasMargins) component).getMargins().getGapLeft());
            if (margins.getGapRight() < ((HasMargins) component).getMargins().getGapRight()) margins.setGapRight(((HasMargins) component).getMargins().getGapRight());
        });
        return margins;
    }


    private int[] getQuantity(int sizeLeafX, int sizeLeafY, Double sizeElementX, Double sizeElementY) {
        int[] mass = new int[3];
        mass[0] = (int) (sizeLeafX/sizeElementX);
        mass[1] = (int) (sizeLeafY/sizeElementY);
        mass[2] = mass[1]*mass[0];
        return mass;
    }

    @Override
    public Boolean isValid() {
        return templateBinder.isValid();
    }

    @Override
    public String getVolumeAsString(){
        try {
            return this.getClass().getSimpleName() + objectMapper.writeValueAsString(templateBinder.getBean());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void setVolumeAsString(String str){
        try {
            templateBinder.setBean(objectMapper.readValue(str, WorkFlow.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
