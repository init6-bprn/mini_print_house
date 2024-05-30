package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
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


   final private PrintMashineService printerService;
   final private MaterialService materialService;
   final private StandartSizeService standartSizeService;
   private final QuantityColorsService quantityColorsService;
   private final TypeOfMaterialService typeOfMaterialService;
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
                        SizeOfPrintLeafService sizeOfPrintLeafService){
        //super();
        this.printerService = printerService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.quantityColorsService = quantityColorsService;
        this.costOfPrintSizeLeafAndColorService = costOfPrintSizeLeafAndColorService;
        this.typeOfMaterialService = typeOfMaterialService;
        this.thicknessService = thicknessService;
        this.sizeOfPrintLeafService = sizeOfPrintLeafService;
        addPrinterSection();
        addMaterialSection();
        addUserEntering();
    }

    private void addMaterialSection() {
        var hLayout = new HorizontalLayout();
        typeOfMaterialCombo.setLabel("Тип материала");
        typeOfMaterialCombo.setItems(typeOfMaterialService.findAll());
        typeOfMaterialCombo.setAllowCustomValue(false);
        typeOfMaterialCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                thicknessCombo.setItems(materialService.findAllThicknessByTypeOfMaterial(e.getValue()));
                if (thicknessCombo.getListDataView().getItemCount()>0) thicknessCombo.setValue(thicknessCombo.getListDataView().getItem(0));
                sizeOfPrintLeafCombo.setItems(materialService.findAllSizeOfPrintLeafByTypeOfMaterial(e.getValue()));
                if (sizeOfPrintLeafCombo.getListDataView().getItemCount()>0) sizeOfPrintLeafCombo.setValue(sizeOfPrintLeafCombo.getListDataView().getItem(0));
                materialCombo.setItems(materialService.findByFilters(e.getValue(), sizeOfPrintLeafCombo.getValue(), thicknessCombo.getValue()));
                if (materialCombo.getListDataView().getItemCount()>0) materialCombo.setValue(materialCombo.getListDataView().getItem(0));
            }
        });

        listThickness = materialService.findAllThicknessByTypeOfMaterial(typeOfMaterialCombo.getValue());
        thicknessCombo.setLabel("Плотность");
        if (listThickness!=null) thicknessCombo.setItems(listThickness);
        thicknessCombo.setAllowCustomValue(false);
        thicknessCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                materialCombo.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), sizeOfPrintLeafCombo.getValue(), e.getValue()));
                if (materialCombo.getListDataView().getItemCount()>0) materialCombo.setValue(materialCombo.getListDataView().getItem(0));
            }
        });

        listSizeOfPrintLeaf = materialService.findAllSizeOfPrintLeafByTypeOfMaterial(typeOfMaterialCombo.getValue());
        if (listSizeOfPrintLeaf!=null) sizeOfPrintLeafCombo.setItems(listSizeOfPrintLeaf);
        sizeOfPrintLeafCombo.setAllowCustomValue(false);
        sizeOfPrintLeafCombo.setLabel("Размер печатного листа:");
        sizeOfPrintLeafCombo.addValueChangeListener(e->{
            if (e.getValue()!=null) {
                materialCombo.setItems(materialService.findByFilters(typeOfMaterialCombo.getValue(), e.getValue(), thicknessCombo.getValue()));
                if (materialCombo.getListDataView().getItemCount()>0) materialCombo.setValue(materialCombo.getListDataView().getItem(0));
            }
        });


        materialCombo.setLabel("Материал для печати:");
        materialCombo.setAllowCustomValue(false);
        materialCombo.addValueChangeListener(e->{
            /*if ( e.getValue()!= null) {
                sizeOfPrintLeafCombo.setItems(e.getValue().getSizeOfPrintLeaf());
                //sizeOfPrintLeafCombo.setValue(e.getValue().getSizeOfPrintLeaf());
            } else sizeOfPrintLeafCombo.setItems();*/
        });


        hLayout.add(typeOfMaterialCombo, thicknessCombo, sizeOfPrintLeafCombo, materialCombo);
        this.add(hLayout);
    }

    private void addUserEntering() {
        var hLayout = new HorizontalLayout();
        var quantityField = new IntegerField();
        quantityField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        quantityField.setLabel("Количество:");
        quantityField.setValue(1);
        Div quantityPrefix = new Div();
        quantityPrefix.setText("шт");
        quantityField.setPrefixComponent(quantityPrefix);

        length = new NumberField();
        length.setLabel("Длина");

        width = new NumberField();
        width.setLabel("Ширина");

        itemsForCombo.addAll(standartSizeService.findAll());

        sizeOfPaperCombo.setItems(standartSizeService.findAll());
        sizeOfPaperCombo.setLabel("Размер изделия");
        sizeOfPaperCombo.setAllowCustomValue(false);
        sizeOfPaperCombo.addValueChangeListener(e -> {
            length.setValue(e.getValue().getLength());
            width.setValue(e.getValue().getWidth());
        }) ;
        //sizeOfPaperCombo.setValue(standartSizeService.findAll().get(0));

        dialog = new SizeDialog(standartSizeService);

        dialog.addOpenedChangeListener(openedChangeEvent -> {
           if (!openedChangeEvent.isOpened()) {
               if (dialog.getStandartSize()!= null) {
                   itemsForCombo.clear();
                   itemsForCombo.addAll(standartSizeService.findAll());
                   sizeOfPaperCombo.getDataProvider().refreshAll();
                   sizeOfPaperCombo.setValue(dialog.getStandartSize());

               }
           }
        });

        var addSizeButton = new Button("Add");
        addSizeButton.addClickListener(e-> {
            dialog.setX(length.getValue());
            dialog.setY(width.getValue());
            dialog.setModal(true);
            dialog.open();
        });

        hLayout.add(quantityField, sizeOfPaperCombo, length, width, addSizeButton, dialog);
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
            if (coverQuantityOfColor.getListDataView().getItemCount()>0) coverQuantityOfColor.setValue(coverQuantityOfColor.getListDataView().getItem(0));
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
            if (backQuantityOfColor.getListDataView().getItemCount()>0) backQuantityOfColor.setValue(backQuantityOfColor.getListDataView().getItem(0));
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

}
