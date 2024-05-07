package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.MainLayout;

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

    public TemplateView(PrintMashineService printerService, MaterialService materialService, StandartSizeService standartSizeService){
        super();
        this.printerService = printerService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        addPrinterBox();
        addUserEntering();
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

        var length = new NumberField();
        length.setLabel("Длина");

        var width = new NumberField();
        width.setLabel("Ширина");

        var standartSizeCombo = new ComboBox<StandartSize>();
        standartSizeCombo.setItems(standartSizeService.findAll());
        standartSizeCombo.setLabel("Размер изделия");
        standartSizeCombo.addValueChangeListener(e -> {
            length.setValue((double) e.getValue().getLength());
            width.setValue((double) e.getValue().getWidth());
        }) ;
        standartSizeCombo.setValue(standartSizeService.findAll().get(0));

        var dialog = new SizeDialog(standartSizeService);
        dialog.addDialogCloseActionListener(e->{
            if (dialog.getStandartSize()!= null)
                {
                    standartSizeCombo.setItems(standartSizeService.findAll());
                    standartSizeCombo.setValue(dialog.getStandartSize());
                }
            e.getSource().close();
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

    private void addPrinterBox() {
        var hLayout = new HorizontalLayout();
        var printerCombo = new ComboBox<PrintMashine>();
        var materialCombo = new ComboBox<Material>();
        materialCombo.setItems(materialService.findAll());
        materialCombo.setValue(materialService.findAll().get(0));
        List<PrintMashine> listPrintMachine = printerService.findAll();
        if (!listPrintMachine.isEmpty()) {
            printerCombo.setItems(listPrintMachine);
            printerCombo.setValue(listPrintMachine.get(0));
        }
        hLayout.add(printerCombo, materialCombo);
        this.add(hLayout);
    }

}
