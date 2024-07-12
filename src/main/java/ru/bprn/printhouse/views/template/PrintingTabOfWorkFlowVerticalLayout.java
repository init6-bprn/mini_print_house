package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;

@AnonymousAllowed
public class PrintingTabOfWorkFlowVerticalLayout extends VerticalLayout implements HasBinder {

    private final PrintMashineService printerService;
    private final QuantityColorsService quantityColorsService;
    private final ObjectMapper objectMapper;
    @Getter
    private final BeanValidationBinder<DigitalPrinting> templateBinder;

    public PrintingTabOfWorkFlowVerticalLayout(PrintMashineService printerService, QuantityColorsService quantityColorsService){

        this.printerService = printerService;
        this.quantityColorsService = quantityColorsService;
        objectMapper = new ObjectMapper();
        templateBinder = new BeanValidationBinder<>(DigitalPrinting.class);

        addPrinterSection();
        addQuantityAndOrientation();
    }

    private void addPrinterSection() {

        var hLayout = new HorizontalLayout();

        var coverQuantityOfColor = new ComboBox<QuantityColors>();
        templateBinder.forField(coverQuantityOfColor).asRequired().bind(DigitalPrinting::getQuantityColorsCover, DigitalPrinting::setQuantityColorsCover);
        coverQuantityOfColor.setItems(quantityColorsService.findAll());

        var backQuantityOfColor = new ComboBox<QuantityColors>();
        templateBinder.forField(backQuantityOfColor).asRequired().bind(DigitalPrinting::getQuantityColorsBack, DigitalPrinting::setQuantityColorsBack);
        backQuantityOfColor.setItems(quantityColorsService.findAll());

        // Принтеры
        var printerCombo = new ComboBox<PrintMashine>();
        printerCombo.setLabel("Принтер:");
        printerCombo.setAllowCustomValue(false);
        printerCombo.setItems(printerService.findAll());
        templateBinder.forField(printerCombo).asRequired().bind(DigitalPrinting::getPrintMashine, DigitalPrinting::setPrintMashine);

        // Цветность лица
        coverQuantityOfColor.setLabel("Лицо");

        // Цветность оборота
        backQuantityOfColor.setLabel("Оборот");

        printerCombo.addValueChangeListener(e -> {
            var oldValue = coverQuantityOfColor.getValue();
            coverQuantityOfColor.setItems(e.getValue().getQuantityColors());
            coverQuantityOfColor.setValue(oldValue);
            var oldValue2 = backQuantityOfColor.getValue();
            backQuantityOfColor.setItems(e.getValue().getQuantityColors());
            backQuantityOfColor.setValue(oldValue2);
        });

        hLayout.add(printerCombo, coverQuantityOfColor, backQuantityOfColor);
        this.add(hLayout);
    }

    private void addQuantityAndOrientation() {
        var hLayout = new HorizontalLayout();
        var radioGroup = new RadioButtonGroup<>("Ориентация");
        var rowsOnLeaf = new IntegerField("Колонок:");
        var columnsOnLeaf = new IntegerField("Столбцов:");
        var quantityOfPrintLeaves = new IntegerField("Изделий на листе:");

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
/*
    private void setVolumeOnComponents (int col, int row, int quan) {
        columnsOnLeaf.setValue(col);
        rowsOnLeaf.setValue(row);
        quantityOfPrintLeaves.setValue(quan);
    }
*/
    @Override
    public Boolean isValid() {
        return templateBinder.isValid();
    }

    @Override
    public String getVolumeAsString(){
        try {
            return objectMapper.writeValueAsString(templateBinder.getBean());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void setVolumeAsString(String str){
        try {
            if (!str.equals("null")) templateBinder.setBean(objectMapper.readValue(str, DigitalPrinting.class));
            else templateBinder.setBean(new DigitalPrinting());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}