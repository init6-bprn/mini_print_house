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
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.MainLayout;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Шаблоны работ")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplateView extends VerticalLayout {

   @Autowired
   private PrintMashineService printerService;

   @Autowired
   private MaterialService materialService;

   @Autowired
   private StandartSizeService standartSizeService;

   private final QuantityColorsService quantityColorsService;

   private final ComboBox<StandartSize> standartSizeCombo = new ComboBox<StandartSize>();

   private SizeDialog dialog;
   private NumberField length;
   private NumberField width;
   final List<StandartSize> itemsForCombo = new ArrayList<>();

    public TemplateView(PrintMashineService printerService, MaterialService materialService, StandartSizeService standartSizeService, QuantityColorsService quantityColorsService){
        super();
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

        var materialCombo = new ComboBox<Material>();
        materialCombo.setItems(materialService.findAll());
        materialCombo.setValue(materialService.findAll().get(0));

        hLayout.add(materialCombo);
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

        standartSizeCombo.setItems(itemsForCombo);
        standartSizeCombo.setLabel("Размер изделия");
        standartSizeCombo.addValueChangeListener(e -> {
            length.setValue((double) e.getValue().getLength());
            width.setValue((double) e.getValue().getWidth());
        }) ;
        standartSizeCombo.setValue(standartSizeService.findAll().get(0));

        dialog = new SizeDialog(standartSizeService);

        dialog.addOpenedChangeListener(openedChangeEvent -> {
           if (!openedChangeEvent.isOpened()) {
               if (dialog.getStandartSize()!= null) {
                   itemsForCombo.clear();
                   itemsForCombo.addAll(standartSizeService.findAll());
                   standartSizeCombo.getDataProvider().refreshAll();
                   standartSizeCombo.setValue(dialog.getStandartSize());

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

        hLayout.add(quantityField, standartSizeCombo, length, width, addSizeButton, dialog);
        this.add(hLayout);
    }

    private void addPrinterSection() {
        var hLayout = new HorizontalLayout();

        var printerCombo = new ComboBox<PrintMashine>();
        List<PrintMashine> listPrintMachine = printerService.findAll();
        if (!listPrintMachine.isEmpty()) {
            printerCombo.setItems(listPrintMachine);
            printerCombo.setValue(listPrintMachine.get(0));
        }

        var quantityOfColor = new ComboBox<QuantityColors>();
        quantityOfColor.setItems(quantityColorsService.findAll());


        hLayout.add(printerCombo, quantityOfColor);
        this.add(hLayout);
    }

}
