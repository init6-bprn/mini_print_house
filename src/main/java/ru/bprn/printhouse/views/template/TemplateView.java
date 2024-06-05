package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

import java.util.ArrayList;
import java.util.List;
@Component
@PageTitle("Шаблоны работ")
@Route(value = "templates", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)

@AnonymousAllowed
public class TemplateView extends VerticalLayout {


   final private PrintMashineService printerService;
   final private MaterialService materialService;
   final private StandartSizeService standartSizeService;
   private final QuantityColorsService quantityColorsService;
   private final TypeOfMaterialService typeOfMaterialService;
   private final GapService gapService;
   private final ThicknessService thicknessService;
   private final SizeOfPrintLeafService sizeOfPrintLeafService;
   private final CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService;
   private final ComboBox<PrintMashine> printerCombo = new ComboBox<>();
   private final ComboBox<StandartSize> sizeOfPaperCombo = new ComboBox<>();
   private final ComboBox<Material> materialCombo = new ComboBox<>();
   private final ComboBox<SizeOfPrintLeaf> sizeOfPrintLeafCombo = new ComboBox<>();
   private final ComboBox<QuantityColors> coverQuantityOfColor = new ComboBox<>();
   private final ComboBox<QuantityColors> backQuantityOfColor = new ComboBox<>();
   private final ComboBox<TypeOfMaterial> typeOfMaterialCombo = new ComboBox<>();
   private final ComboBox<Thickness> thicknessCombo = new ComboBox<>();
   private SizeDialog dialog;
   private NumberField length;
   private NumberField width;
   final private List<StandartSize> itemsForCombo = new ArrayList<>();
   final private List<Material> listOfMaterial = new ArrayList<>();
   private List<Thickness> listThickness = new ArrayList<>();
   private List<SizeOfPrintLeaf> listSizeOfPrintLeaf = new ArrayList<>();



    @Autowired
    public TemplateView(PrintMashineService printerService, MaterialService materialService,
                        StandartSizeService standartSizeService, QuantityColorsService quantityColorsService,
                        CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService,
                        TypeOfMaterialService typeOfMaterialService, ThicknessService thicknessService,
                        SizeOfPrintLeafService sizeOfPrintLeafService, GapService gapService){
        //super();
        this.printerService = printerService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.quantityColorsService = quantityColorsService;
        this.costOfPrintSizeLeafAndColorService = costOfPrintSizeLeafAndColorService;
        this.typeOfMaterialService = typeOfMaterialService;
        this.thicknessService = thicknessService;
        this.sizeOfPrintLeafService = sizeOfPrintLeafService;
        this.gapService = gapService;
        addPrinterSection();
        addMaterialSection();
        addSizeOfProduct();
        addQuantityAndOrientation();
    }

    private void addMaterialSection() {

        typeOfMaterialCombo.setLabel("Тип материала");
        typeOfMaterialCombo.setItems(typeOfMaterialService.findAll());
        typeOfMaterialCombo.setAllowCustomValue(false);
        typeOfMaterialCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                thicknessCombo.setItems(materialService.findAllThicknessByTypeOfMaterial(e.getValue()));
                comboBoxViewFirstElement(thicknessCombo);
                sizeOfPrintLeafCombo.setItems(materialService.findAllSizeOfPrintLeafByTypeOfMaterial(e.getValue()));
                comboBoxViewFirstElement(sizeOfPrintLeafCombo);
                materialCombo.setItems(materialService.findByFilters(e.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue()));
                comboBoxViewFirstElement(materialCombo);
            }
        });

        listThickness = materialService.findAllThicknessByTypeOfMaterial(typeOfMaterialCombo.getValue());
        thicknessCombo.setLabel("Плотность");
        if (listThickness!=null) thicknessCombo.setItems(listThickness);
        thicknessCombo.setAllowCustomValue(false);
        thicknessCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                materialCombo.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), e.getValue()));
                comboBoxViewFirstElement(materialCombo);
            }
        });

        listSizeOfPrintLeaf = materialService.findAllSizeOfPrintLeafByTypeOfMaterial(typeOfMaterialCombo.getValue());
        if (listSizeOfPrintLeaf!=null) sizeOfPrintLeafCombo.setItems(listSizeOfPrintLeaf);
        sizeOfPrintLeafCombo.setAllowCustomValue(false);
        sizeOfPrintLeafCombo.setLabel("Размер печатного листа:");
        sizeOfPrintLeafCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                materialCombo.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), e.getValue(), thicknessCombo.getValue()));
                comboBoxViewFirstElement(materialCombo);
            }
        });


        materialCombo.setLabel("Материал для печати:");
        materialCombo.setAllowCustomValue(false);
        materialCombo.setItems(materialService.findAll());
        materialCombo.addValueChangeListener(e->{
            /*if ( e.getValue()!= null) {
                sizeOfPrintLeafCombo.setItems(e.getValue().getSizeOfPrintLeaf());
                //sizeOfPrintLeafCombo.setValue(e.getValue().getSizeOfPrintLeaf());
            } else sizeOfPrintLeafCombo.setItems();*/
        });

        Grid<Material> grid = new Grid<>(Material.class, false);
        grid.setItems(materialService.findAll());
        grid.addColumn(Material::getName).setHeader("Название");
        Grid.Column<Material> typeColumn = grid.addColumn(Material::getTypeOfMaterial).setHeader("Тип материала");
        Grid.Column<Material> sizeColumn = grid.addColumn(Material::getSizeOfPrintLeaf).setHeader("Размер печатного листа");
        Grid.Column<Material> thicknessColumn = grid.addColumn(Material::getThickness).setHeader("Плотность");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

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

        var bleedCombo = new ComboBox<Gap>("Припуск");
        bleedCombo.setItems(gapService.findAllBleeds("Bleed"));

        hLayout.add(sizeOfPaperCombo, length, width, bleedCombo, layout, dialog);
        this.add(hLayout);
    }

    private void addPrinterSection() {
        var hLayout = new HorizontalLayout();
        // Принтеры
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        List<PrintMashine> listPrintMachine = printerService.findAll();
        if (!listPrintMachine.isEmpty()) {
            printerCombo.setItems(listPrintMachine);
            printerCombo.setValue(listPrintMachine.get(0));
        }

        printerCombo.addValueChangeListener(e -> {
            coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
            comboBoxViewFirstElement(coverQuantityOfColor);
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
            comboBoxViewFirstElement(backQuantityOfColor);
        });

        // Цветность лица
        coverQuantityOfColor.setLabel("Лицо");
        if (!printerCombo.isEmpty()) coverQuantityOfColor.setItems(printerCombo.getValue().getQuantityColors());

        // Цветность оборота
        backQuantityOfColor.setLabel("Оборот");
        if (!printerCombo.isEmpty()) backQuantityOfColor.setItems(printerCombo.getValue().getQuantityColors());


        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);
    }

    private void addQuantityAndOrientation() {
        var hLayout = new HorizontalLayout();

        var quantityField = new IntegerField();
        quantityField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        quantityField.setLabel("Количество:");
        quantityField.setValue(1);
        Div quantityPrefix = new Div();
        quantityPrefix.setText("шт");
        quantityField.setPrefixComponent(quantityPrefix);

        var radioGroup = new RadioButtonGroup<String>();
        radioGroup.setLabel("Ориентация");
        radioGroup.setItems("Автоматически", "Вертикальная", "Горизонтальная");
        radioGroup.setValue("Автоматически");
        add(radioGroup);

        hLayout.add(radioGroup, quantityField);
        this.add(hLayout);

    }

    private void comboBoxViewFirstElement (ComboBox combo) {
        if (combo.getListDataView().getItemCount()>0) combo.setValue(combo.getListDataView().getItem(0));
    }

}
