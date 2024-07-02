package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.views.MainLayout;


@AnonymousAllowed
public class TemplateView extends VerticalLayout {

    private final PrintMashineService printerService;

    @PropertyId("printMashine")
    private final ComboBox<PrintMashine> printerCombo = new ComboBox<>();

    @PropertyId("rowsOnLeaf")
    private final IntegerField rowsOnLeaf = new IntegerField("Колонок:");

    @PropertyId("columnsOnLeaf")
    private final IntegerField columnsOnLeaf = new IntegerField("Столбцов:");

    @PropertyId("quantityOfPrintLeaves")
    private final IntegerField quantityOfPrintLeaves = new IntegerField("Изделий на листе:");

    private final RadioButtonGroup<String> radioGroup = new RadioButtonGroup<>("Ориентация");

    public TemplateView(PrintMashineService printerService){

        this.printerService = printerService;

        //Binder<DigitalPrintTemplate> binder = new BeanValidationBinder<>(DigitalPrintTemplate.class,true);
        //binder.bindInstanceFields(this);

        addPrinterSection();
        addQuantityAndOrientation();
    }

    private void addPrinterSection() {
        var hLayout = new HorizontalLayout();
        var coverQuantityOfColor = new ComboBox<QuantityColors>();
        var backQuantityOfColor = new ComboBox<QuantityColors>();
        // Принтеры
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        printerCombo.setItems(printerService.findAll());
        comboBoxViewFirstElement(printerCombo);

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

        printerCombo.addValueChangeListener(e -> {
            coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
            comboBoxViewFirstElement(coverQuantityOfColor);
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
            comboBoxViewFirstElement(backQuantityOfColor);
        });

        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);
    }

    private void addQuantityAndOrientation() {
        var hLayout = new HorizontalLayout();

        radioGroup.setItems("Автоматически", "Вертикальная", "Горизонтальная");
        radioGroup.setValue("Автоматически");
       // radioGroup.addValueChangeListener(e-> calculateAndSetQuantity());
        add(radioGroup);

        quantityOfPrintLeaves.setReadOnly(true);
        rowsOnLeaf.setReadOnly(true);
        columnsOnLeaf.setReadOnly(true);
        hLayout.add(radioGroup);
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
/*
    private void calculateAndSetQuantity(){
        if ((sizeOfPrintLeaf!=null)&(size!=null)&(printerCombo.getValue()!=null)&(bleed!=null)) {
            int[] mass1 = getQuantity(sizeOfPrintLeaf.getLength()-printerCombo.getValue().getGap().getGapLeft()-printerCombo.getValue().getGap().getGapRight(),
                    sizeOfPrintLeaf.getWidth()-printerCombo.getValue().getGap().getGapTop()-printerCombo.getValue().getGap().getGapBottom(),
                    size.getLength()+bleed.getGapLeft()+bleed.getGapRight(),
                    size.getWidth()+bleed.getGapTop()+bleed.getGapBottom());

            int[] mass2 = getQuantity(sizeOfPrintLeaf.getLength()-printerCombo.getValue().getGap().getGapLeft()-printerCombo.getValue().getGap().getGapRight(),
                    sizeOfPrintLeaf.getWidth()-printerCombo.getValue().getGap().getGapTop()-printerCombo.getValue().getGap().getGapBottom(),
                    size.getWidth()+bleed.getGapTop()+bleed.getGapBottom(),
                    size.getLength()+bleed.getGapLeft()+bleed.getGapRight());

            switch (radioGroup.getValue()) {
                case "Автоматически":
                    if (mass1[2] >= mass2[2]) setVolumeOnComponents(mass1[0], mass1[1], mass1[2]);
                    else setVolumeOnComponents(mass2[0], mass2[1], mass2[2]);
                    break;
                case "Вертикальная":
                    setVolumeOnComponents(mass1[0], mass1[1], mass1[2]);
                    break;
                case "Горизонтальная":
                    setVolumeOnComponents(mass2[0], mass2[1], mass2[2]);
                    break;
            }
        }
    }
*/
    private <T> void comboBoxViewFirstElement (ComboBox<T> combo) {
        if (combo.getListDataView().getItemCount()>0) combo.setValue(combo.getListDataView().getItem(0));
    }

    private void setVolumeOnComponents (int col, int row, int quan) {
        columnsOnLeaf.setValue(col);
        rowsOnLeaf.setValue(row);
        quantityOfPrintLeaves.setValue(quan);
    }

}