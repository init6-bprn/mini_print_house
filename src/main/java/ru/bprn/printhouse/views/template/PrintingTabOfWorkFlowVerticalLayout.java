package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.PrintMashineService;
import ru.bprn.printhouse.data.service.QuantityColorsService;

@AnonymousAllowed
public class PrintingTabOfWorkFlowVerticalLayout extends VerticalLayout implements HasBinder, HasMargins {

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


    private <T> void comboBoxViewFirstElement (ComboBox<T> combo) {
        if (combo.getListDataView().getItemCount()>0) combo.setValue(combo.getListDataView().getItem(0));
    }

    @Override
    public Boolean isValid() {
        return templateBinder.isValid();
    }

    @Override
    public String getBeanAsString(){
        try {
            return objectMapper.writeValueAsString(templateBinder.getBean());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void setBeanFromString(String str){
        try {
            if (!str.equals("null")) templateBinder.setBean(objectMapper.readValue(str, DigitalPrinting.class));
            else templateBinder.setBean(new DigitalPrinting());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Gap getMargins() {
        return templateBinder.getBean().getPrintMashine().getGap();
    }
}