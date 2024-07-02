package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.PrintMashineService;

@AnonymousAllowed
public class printingTabOfWorkFlowVerticalLayout extends VerticalLayout {
    private final PrintMashineService printMashineService;

    public printingTabOfWorkFlowVerticalLayout(PrintMashineService printMashineService){

        this.printMashineService = printMashineService;
        addPrinterSection();
    }

    private void addPrinterSection() {
        var hLayout = new HorizontalLayout();
        var printerCombo = new ComboBox<PrintMashine>();
        var coverQuantityOfColor = new ComboBox<QuantityColors>();
        var backQuantityOfColor = new ComboBox<QuantityColors>();

        // Принтеры
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        printerCombo.setItems(printMashineService.findAll());

        // Цветность лица
        coverQuantityOfColor.setLabel("Лицо");
        if (printerCombo.getValue()!=null) {
            coverQuantityOfColor.setItems(printerCombo.getValue().getQuantityColors());
        }

        // Цветность оборота
        backQuantityOfColor.setLabel("Оборот");
        if (printerCombo.getValue()!=null) {
            backQuantityOfColor.setItems(printerCombo.getValue().getQuantityColors());
        }

        printerCombo.addValueChangeListener(e -> {
            coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
        });

        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);
    }
}
