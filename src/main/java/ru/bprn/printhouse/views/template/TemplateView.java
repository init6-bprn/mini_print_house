package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Шаблоны работ")
@Route(value = "templates", layout = MainLayout.class)

@AnonymousAllowed
public class TemplateView extends VerticalLayout {


   private final PrintMashineService printerService;
   private final MaterialService materialService;
   private final StandartSizeService standartSizeService;
   private final TypeOfMaterialService typeOfMaterialService;
   private final GapService gapService;

   @PropertyId("printMashine")
   private final ComboBox<PrintMashine> printerCombo = new ComboBox<>();

   @PropertyId("name")
   private final TextField textField = new TextField();

   @PropertyId("coverQuantityColors")
   private final ComboBox<QuantityColors> coverQuantityOfColor = new ComboBox<>();

   @PropertyId("backQuantityColors")
   private final ComboBox<QuantityColors> backQuantityOfColor = new ComboBox<>();

   @PropertyId("gap")
   private final ComboBox<Gap> bleedCombo = new ComboBox<>("Припуск:");

   @PropertyId("quantity")
   private final IntegerField quantityField = new IntegerField("Количество:");

   @PropertyId("rowsOnLeaf")
   private final IntegerField rowsOnLeaf = new IntegerField("Колонок:");

   @PropertyId("columnsOnLeaf")
   private final IntegerField columnsOnLeaf = new IntegerField("Столбцов:");

   @PropertyId("quantityOfPrintLeaves")
   private final IntegerField quantityOfPrintLeaves = new IntegerField("Изделий на листе:");

   private final ComboBox<StandartSize> sizeOfPaperCombo = new ComboBox<>();
   private final ComboBox<SizeOfPrintLeaf> sizeOfPrintLeafCombo = new ComboBox<>();
   private final Grid<Material> grid = new Grid<>(Material.class, false);
   private final ComboBox<TypeOfMaterial> typeOfMaterialCombo = new ComboBox<>();
   private final ComboBox<Thickness> thicknessCombo = new ComboBox<>();
   private SizeDialog dialog;
   private NumberField length;
   private NumberField width;
   private List<Thickness> listThickness = new ArrayList<>();
   private List<SizeOfPrintLeaf> listSizeOfPrintLeaf = new ArrayList<>();
   private DigitalPrintTemplate digitalPrintTemplate = new DigitalPrintTemplate();


    @Autowired
    public TemplateView(PrintMashineService printerService, MaterialService materialService,
                        StandartSizeService standartSizeService, TypeOfMaterialService typeOfMaterialService,
                        GapService gapService){
        this.printerService = printerService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.typeOfMaterialService = typeOfMaterialService;
        this.gapService = gapService;
        Binder<DigitalPrintTemplate> binder = new BeanValidationBinder<>(DigitalPrintTemplate.class,true);
        binder.bindInstanceFields(this);

        textField.setLabel("Название шаблона");
        textField.setValue(digitalPrintTemplate.getName());
        add(textField);
        addSizeOfProduct();
        addPrinterSection();
        addMaterialSection();
        addQuantityAndOrientation();
    }

    private void addMaterialSection() {

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

        listThickness = materialService.findAllThicknessByTypeOfMaterial(typeOfMaterialCombo.getValue());
        if (listThickness!=null) thicknessCombo.setItems(listThickness);
        thicknessCombo.setAllowCustomValue(false);
        thicknessCombo.addValueChangeListener(e->{
            grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), e.getValue()));
        });

        listSizeOfPrintLeaf = materialService.findAllSizeOfPrintLeafByTypeOfMaterial(typeOfMaterialCombo.getValue());
        if (listSizeOfPrintLeaf!=null) sizeOfPrintLeafCombo.setItems(listSizeOfPrintLeaf);
        sizeOfPrintLeafCombo.setAllowCustomValue(false);
        sizeOfPrintLeafCombo.addValueChangeListener(e->{
            grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), e.getValue(), thicknessCombo.getValue()));
        });

        grid.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue()));
        grid.addColumn(Material::getName).setHeader("Название");
        Grid.Column<Material> typeColumn = grid.addColumn(Material::getTypeOfMaterial).setHeader("Тип материала");
        Grid.Column<Material> sizeColumn = grid.addColumn(Material::getSizeOfPrintLeaf).setHeader("Размер печатного листа");
        Grid.Column<Material> thicknessColumn = grid.addColumn(Material::getThickness).setHeader("Плотность");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("270px");

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(typeColumn).setComponent(typeOfMaterialCombo);
        headerRow.getCell(sizeColumn).setComponent(sizeOfPrintLeafCombo);
        headerRow.getCell(thicknessColumn).setComponent(thicknessCombo);

        this.add(grid);
    }

    private void addSizeOfProduct() {
        var hLayout = new HorizontalLayout();

        length = new NumberField();
        length.setLabel("Длина");

        width = new NumberField();
        width.setLabel("Ширина");

        sizeOfPaperCombo.setItems(standartSizeService.findAll());
        sizeOfPaperCombo.setLabel("Размер изделия");
        sizeOfPaperCombo.setAllowCustomValue(false);
        sizeOfPaperCombo.addValueChangeListener(e -> {
            length.setValue(e.getValue().getLength());
            width.setValue(e.getValue().getWidth());
        }) ;

        dialog = new SizeDialog(standartSizeService);

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


        bleedCombo.setItems(gapService.findAllBleeds("Bleed"));

        hLayout.add(sizeOfPaperCombo, length, width, bleedCombo, layout, dialog);
        this.add(hLayout);
    }

    private void addPrinterSection() {
        var hLayout = new HorizontalLayout();
        // Принтеры
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        printerCombo.setItems(printerService.findAll());
        comboBoxViewFirstElement(printerCombo);

        printerCombo.addValueChangeListener(e -> {
            coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
            comboBoxViewFirstElement(coverQuantityOfColor);
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
            comboBoxViewFirstElement(backQuantityOfColor);
        });

        // Цветность лица
        coverQuantityOfColor.setLabel("Лицо");
        if (printerCombo.getValue()!=null) {
            coverQuantityOfColor.setItems(printerCombo.getValue().getQuantityColors());
            comboBoxViewFirstElement(coverQuantityOfColor);
        }

        // Цветность оборота
        backQuantityOfColor.setLabel("Оборот");
        if (printerCombo.getValue()!=null) {
            backQuantityOfColor.setItems(printerCombo.getValue().getQuantityColors());
            comboBoxViewFirstElement(backQuantityOfColor);
        }


        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);
    }

    private void addQuantityAndOrientation() {
        var hLayout = new HorizontalLayout();
        quantityField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        quantityField.setValue(1);
        Div quantityPrefix = new Div();
        quantityPrefix.setText("шт");
        quantityField.setPrefixComponent(quantityPrefix);

        var radioGroup = new RadioButtonGroup<String>();
        radioGroup.setLabel("Ориентация");
        radioGroup.setItems("Автоматически", "Вертикальная", "Горизонтальная");
        radioGroup.setValue("Горизонтальная");
        add(radioGroup);

        radioGroup.addValueChangeListener(e->{
            if ((sizeOfPrintLeafCombo.getValue()!=null)&(length.getValue()!=null)&(width.getValue()!=null)&(printerCombo.getValue()!=null)&(bleedCombo.getValue()!=null)) {
                int[] mass1 = getQuantity(sizeOfPrintLeafCombo.getValue().getLength()-printerCombo.getValue().getGap().getGapLeft()-printerCombo.getValue().getGap().getGapRight(),
                                          sizeOfPrintLeafCombo.getValue().getWidth()-printerCombo.getValue().getGap().getGapTop()-printerCombo.getValue().getGap().getGapBottom(),
                                       length.getValue()+bleedCombo.getValue().getGapLeft()+bleedCombo.getValue().getGapRight(),
                                       width.getValue()+bleedCombo.getValue().getGapTop()+bleedCombo.getValue().getGapBottom());

                int[] mass2 = getQuantity(sizeOfPrintLeafCombo.getValue().getLength()-printerCombo.getValue().getGap().getGapLeft()-printerCombo.getValue().getGap().getGapRight(),
                                          sizeOfPrintLeafCombo.getValue().getWidth()-printerCombo.getValue().getGap().getGapTop()-printerCombo.getValue().getGap().getGapBottom(),
                                       width.getValue()+bleedCombo.getValue().getGapTop()+bleedCombo.getValue().getGapBottom(),
                                       length.getValue()+bleedCombo.getValue().getGapLeft()+bleedCombo.getValue().getGapRight());

                switch (e.getValue()) {
                    case "Автоматически": {
                        if (mass1[2] >= mass2[2]) setVolumeOnComponents(mass1[0], mass1[1], mass1[2]);
                        else setVolumeOnComponents(mass2[0], mass2[1], mass2[2]);
                    }
                    case "Вертикальная":
                        setVolumeOnComponents(mass1[0], mass1[1], mass1[2]);
                    case "Горизонтальная":
                        setVolumeOnComponents(mass2[0], mass2[1], mass2[2]);
                }
            }
        });

        quantityOfPrintLeaves.setReadOnly(true);
        rowsOnLeaf.setReadOnly(true);
        columnsOnLeaf.setReadOnly(true);
        hLayout.add(radioGroup, quantityField);
        var hl = new HorizontalLayout();
        hl.add(rowsOnLeaf,columnsOnLeaf,quantityOfPrintLeaves);
        this.add(hLayout,hl);

    }

    private int[] getQuantity(int sizeLeafX, int sizeLeafY, Double sizeElementX, Double sizeElementY) {
        int[] mass = new int[3];
        mass[0] = (int) (sizeLeafX/sizeElementX);
        mass[1] = (int) (sizeLeafY/sizeElementY);
        mass[2] = mass[1]*mass[0];
        return mass;
    }

    private void comboBoxViewFirstElement (ComboBox combo) {
        if (combo.getListDataView().getItemCount()>0) combo.setValue(combo.getListDataView().getItem(0));
    }

    private void setVolumeOnComponents (int col, int row, int quan) {
        columnsOnLeaf.setValue(col);
        rowsOnLeaf.setValue(row);
        quantityOfPrintLeaves.setValue(quan);
    }

}