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
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@PageTitle("Шаблоны работ")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplateView extends VerticalLayout {


   final private PrintMashineService printerService;
   final private MaterialService materialService;
   final private StandartSizeService standartSizeService;
   private final QuantityColorsService quantityColorsService;
   private final ComboBox<StandartSize> sizeOfPaperCombo = new ComboBox<>();
   private final ComboBox<Material> materialCombo = new ComboBox<>();
   private final ComboBox<SizeOfPrintLeaf> sizeOfPrintLeafCombo = new ComboBox<>();

   private SizeDialog dialog;
   private NumberField length;
   private NumberField width;
   final private List<StandartSize> itemsForCombo = new ArrayList<>();
   final private List<Material> listOfMaterial = new ArrayList<>();
   final private List<SizeOfPrintLeaf> listSizeOfPrintLeaf = new ArrayList<>();

    @Autowired
    public TemplateView(PrintMashineService printerService, MaterialService materialService, StandartSizeService standartSizeService, QuantityColorsService quantityColorsService){
        //super();
        this.printerService = printerService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.quantityColorsService = quantityColorsService;
        addPrinterSection();
        addMaterialSection();
        addUserEntering();
    }

    private void addMaterialSection() {
        var hLayout = new HorizontalLayout();
        materialCombo.setLabel("Материал для печати:");
        materialCombo.setItems(materialService.findAll());
        materialCombo.scrollIntoView();
        materialCombo.setAllowCustomValue(false);

        materialCombo.addValueChangeListener(e->{
            if ( e.getValue()!= null) {
                sizeOfPrintLeafCombo.setItems(e.getValue().getSizeOfPrintLeaf());
                //sizeOfPrintLeafCombo.setValue(e.getValue().getSizeOfPrintLeaf());
            } else sizeOfPrintLeafCombo.setItems();
        });
        //materialCombo.setValue(materialService.findAll().get(0));
        sizeOfPrintLeafCombo.setItems(listSizeOfPrintLeaf);
        sizeOfPrintLeafCombo.setAllowCustomValue(false);
        sizeOfPrintLeafCombo.setLabel("Размер печатного листа:");


        hLayout.add(materialCombo, sizeOfPrintLeafCombo);
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
        var printerCombo = new ComboBox<PrintMashine>();
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        List<PrintMashine> listPrintMachine = printerService.findAll();
        if (!listPrintMachine.isEmpty()) {
            printerCombo.setItems(listPrintMachine);
            printerCombo.setValue(listPrintMachine.get(0));
            updateMaterialCombo(listPrintMachine.get(0).getMaterials());
        }

        printerCombo.addValueChangeListener(e -> updateMaterialCombo(e.getValue().getMaterials()));

        // Цветность лица
        var coverQuantityOfColor = new ComboBox<QuantityColors>();
        coverQuantityOfColor.setLabel("Лицо");
        coverQuantityOfColor.setItems(quantityColorsService.findAll());

        // Цветность оборота
        var backQuantityOfColor = new ComboBox<QuantityColors>();
        backQuantityOfColor.setLabel("Оборот");
        backQuantityOfColor.setItems(quantityColorsService.findAll());


        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);
    }

    private void updateMaterialCombo(Set<Material> materials) {
        listOfMaterial.clear();
        listOfMaterial.addAll(materials);
        if (!listOfMaterial.isEmpty()) {
            materialCombo.setItems(listOfMaterial);
            materialCombo.getDataProvider().refreshAll();
            materialCombo.setValue(listOfMaterial.get(0));
        }
    }

}
