package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.*;

import java.util.Objects;

@UIScope
@AnonymousAllowed
public class StartTabOfWorkFlowVerticalLayout extends VerticalLayout implements HasBinder, Price{

    private final StandartSizeService standartSizeService;
    private final TypeOfMaterialService typeOfMaterialService;
    private final MaterialService materialService;
    private final GapService gapService;
    private final ImposeCaseService imposeCaseService;
    private final ObjectMapper objectMapper;

    @Getter
    private final BeanValidationBinder<WorkFlow> templateBinder;

    @Getter
    private final Checkbox autoNamed = new Checkbox("Задать имя автоматически");

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

        addNameOfTemplate();
        addSizeOfProductSection();
        //addSetOfSheetsSection();
        //addMaterialSection();
        //addQuantityAndOrientation();

    }

    private  void addNameOfTemplate() {
        var nameOfTemplate = new TextField("Название шаблона: ");

        nameOfTemplate.setReadOnly(true);
        autoNamed.setValue(true);
        autoNamed.addValueChangeListener(e->{
            if (e.getValue()) {
                nameOfTemplate.setReadOnly(true);
                setDefaultName();
                templateBinder.refreshFields();
            }
            else nameOfTemplate.setReadOnly(false);
        });
        nameOfTemplate.setSizeFull();
        nameOfTemplate.setValueChangeMode(ValueChangeMode.EAGER);
        templateBinder.bind(nameOfTemplate, WorkFlow::getName, WorkFlow::setName);
        this.add(nameOfTemplate, autoNamed);
    }

    private String setDefaultName() {
        var bean = templateBinder.getBean();
        var stringName = new StringBuilder();

        if (bean.getStandartSize() != null) stringName.append(bean.getStandartSize().getName()).append(" - ");
        else if (bean.getSizeX() != null && bean.getSizeY() != null) {
                 stringName.append(bean.getSizeX()).append("x").append(bean.getSizeY()).append(" - ");
             }
            else stringName.append("Неизвестный размер - ");
/*
        if (bean.getMaterial()!=null) stringName.append(bean.getMaterial().getName()).append("-")
                .append(bean.getMaterial().getThickness()).append("-")
                .append(bean.getMaterial().getSizeOfPrintLeaf());
        else stringName.append("неизвестный материал - ");
*/
        return  stringName.toString();
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

        private void addSetOfSheetsSection(){
            var ha = new HorizontalLayout();

            var sheetsQuantity = new IntegerField("Страниц/листов");
            sheetsQuantity.setValue(1);
            templateBinder.bind(sheetsQuantity, WorkFlow::getQuantityOfLeaves, WorkFlow::setQuantityOfLeaves);

            var imposeCaseCombo = new ComboBox<ImposeCase>("Вариант спуска полос:");
            imposeCaseCombo.setItems(imposeCaseService.findAll());
            templateBinder.forField(imposeCaseCombo).asRequired().bind(WorkFlow::getImposeCase, WorkFlow::setImposeCase);
            imposeCaseCombo.setValue(imposeCaseService.getFirst());


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
    */
    private <T> void comboBoxViewFirstElement(ComboBox<T> combo) {
        if (combo!=null)
            if (combo.getListDataView().getItemCount()>0)
                combo.setValue(combo.getListDataView().getItem(0));
    }

    private void addSizeOfProductSection() {
        var hLayout = new HorizontalLayout();
        hLayout.setAlignItems(Alignment.START);

        var length = new NumberField();
        length.setLabel("Длина");
        length.setWidth(13, Unit.PERCENTAGE);
        templateBinder.forField(length).asRequired().bind(WorkFlow::getSizeX, WorkFlow::setSizeX);
        length.addValueChangeListener(e->setFullProductSize());

        var width = new NumberField();
        width.setLabel("Ширина");
        width.setWidth(13, Unit.PERCENTAGE);
        templateBinder.forField(width).asRequired().bind(WorkFlow::getSizeY, WorkFlow::setSizeY);
        length.addValueChangeListener(e->setFullProductSize());

        var sizeOfPaperCombo = new ComboBox<StandartSize>();
        sizeOfPaperCombo.setWidth(30, Unit.PERCENTAGE);
        sizeOfPaperCombo.setItems(standartSizeService.findAll());

        templateBinder.forField(sizeOfPaperCombo).asRequired().bind(WorkFlow::getStandartSize, WorkFlow::setStandartSize);

        sizeOfPaperCombo.setLabel("Размер изделия");
        sizeOfPaperCombo.setAllowCustomValue(false);
        sizeOfPaperCombo.addValueChangeListener(e -> {
            if (e.getValue()!=null) {
                length.setValue(e.getValue().getLength());
                width.setValue(e.getValue().getWidth());
                if (autoNamed.getValue()) {
                    templateBinder.getBean().setName(setDefaultName());
                    templateBinder.refreshFields();
                }
                setFullProductSize();
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
        layout.setWidth(14, Unit.PERCENTAGE);
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
        bleedCombo.setWidth(30, Unit.PERCENTAGE);
        bleedCombo.setItems(gapService.findAllBleeds("Bleed"));
        //templateBinder.forField(bleedCombo).asRequired().bind(WorkFlow::getBleed, WorkFlow::setBleed);
        templateBinder.forField(bleedCombo).withValidator(Objects::nonNull, "Обязательно заполнить!").bind(WorkFlow::getBleed, WorkFlow::setBleed);

        bleedCombo.addValueChangeListener(e->{
            if (autoNamed.getValue()) {
                templateBinder.getBean().setName(setDefaultName());
                templateBinder.refreshFields();
            }
            setFullProductSize();
        });

        hLayout.add(sizeOfPaperCombo, length, width, layout, bleedCombo, dialog);
        this.add(hLayout);
    }

    private void setFullProductSize() {
        var bean = templateBinder.getBean();
        if (bean != null) {
            if ((bean.getSizeY() != null) & (bean.getPrintSizeX() != null)) {
                if (bean.getBleed() != null) {
                    bean.setFullProductSizeX(bean.getSizeY() + bean.getBleed().getGapTop() + bean.getBleed().getGapBottom());
                    bean.setFullProductSizeY(bean.getSizeX() + bean.getBleed().getGapLeft() + bean.getBleed().getGapRight());
                } else {
                    bean.setFullProductSizeX(bean.getSizeY());
                    bean.setFullProductSizeY(bean.getSizeX());
                }
                templateBinder.refreshFields();
            }
        }
    }


    private void addQuantityAndOrientation() {
        var hLayout = new HorizontalLayout();
        var radioGroup = new RadioButtonGroup<String>("Ориентация");
        var rowsOnLeaf = new IntegerField("Колонок:");
        var columnsOnLeaf = new IntegerField("Столбцов:");
        var quantityOfPrintLeaves = new IntegerField("Листаж:");
        var quantityOfProduction = new IntegerField("Тираж:");
        var quantityProductionsOnLeaf = new IntegerField("Изделий на листе:");
        var gapLayout = new HorizontalLayout();
        var h3 = new NativeLabel("Отступы от краев материала:");
        var topGap = new IntegerField("Top");
        var bottomGsp = new IntegerField("Bottom");
        var leftGap = new IntegerField("Left");
        var rightGap = new IntegerField("Right");
        var printSizeX = new NumberField("Ширина:");
        var printSizeY = new NumberField("Длина:");
        gapLayout.add(topGap, bottomGsp, leftGap ,rightGap, printSizeX, printSizeY);

        templateBinder.bind(rowsOnLeaf, WorkFlow::getRowsOnLeaf, WorkFlow::setRowsOnLeaf);
        templateBinder.bind(columnsOnLeaf, WorkFlow::getColumnsOnLeaf, WorkFlow::setColumnsOnLeaf);
        templateBinder.bind(quantityOfPrintLeaves, WorkFlow::getQuantityOfPrintLeaves, WorkFlow::setQuantityOfPrintLeaves);
        templateBinder.bind(quantityOfProduction, WorkFlow::getQuantityOfProduct, WorkFlow::setQuantityOfProduct);
        templateBinder.bind(quantityProductionsOnLeaf, WorkFlow::getQuantityProductionsOnLeaf, WorkFlow::setQuantityProductionsOnLeaf);
        templateBinder.bind(printSizeX, WorkFlow::getPrintSizeX, WorkFlow::setPrintSizeX);
        templateBinder.bind(printSizeY, WorkFlow::getPrintSizeY, WorkFlow::setPrintSizeY);


        quantityOfPrintLeaves.setReadOnly(true);
        rowsOnLeaf.setReadOnly(true);
        columnsOnLeaf.setReadOnly(true);
        hLayout.add(radioGroup);
        var hl = new HorizontalLayout();
        hl.add(rowsOnLeaf,columnsOnLeaf,quantityProductionsOnLeaf, quantityOfProduction, quantityOfPrintLeaves);
        this.add(hLayout,hl, h3, gapLayout);
    }

    @Override
    public Boolean isValid() {
        templateBinder.validate();
        return templateBinder.isValid();
    }

    @Override
    public String[] getBeanAsString(){
        return JSONToObjectsHelper.getBeanAsJSONStr(templateBinder.getBean());
    }

    @Override
    public double getPriceOfOperation() {
        return 1d;
    }

    @Override
    public double getPriceOfWork() {
        return 0;
    }

    @Override
    public double getPriceOfAmmo() {
        return 0;
    }

    @Override
    public int getTimeOfOperationPerSec() {
        return 0;
    }

    @Override
    public String getFormula() {
        return "price*leaves";
    }
}
