package ru.bprn.printhouse.data.calculate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.CostOfPrintSizeLeafAndColorService;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class OneSheetDigitalPrintingCalculateWorkView extends Dialog {
    private final DigitalPrinting digitalPrinting;
    private final CostOfPrintSizeLeafAndColorService costService;

    public OneSheetDigitalPrintingCalculateWorkView(List<Object> digitalPrinting, CostOfPrintSizeLeafAndColorService costService, String str) {
        super(str);
        this.digitalPrinting = JSONToObjectsHelper.setBeanFromJSONStr(digitalPrinting, DigitalPrinting.class);
        this.costService = costService;
        if (this.digitalPrinting != null) {
            add(addComponentView());
            addFooterComponents();
        }
        else {
            Notification.show("Что-то пошло не так...");
        }
    }

    private FormLayout addComponentView() {
        var vl = new FormLayout();
        var text = new TextArea("Итоги:");
        vl.setColspan(text, 2);


        var colorFrontSelector = new Select<QuantityColors>("Цветность лицо:", selectColor -> {
            digitalPrinting.setQuantityColorsCover(selectColor.getValue());
            text.setValue(calculate());
        });
        colorFrontSelector.setItems(digitalPrinting.getPrintMashine().getQuantityColors());
        colorFrontSelector.setValue(digitalPrinting.getQuantityColorsCover());

        var colorBackSelector = new Select<QuantityColors>("Цветность оборот:", selectColor -> {
            digitalPrinting.setQuantityColorsBack(selectColor.getValue());
            text.setValue(calculate());
        });
        colorBackSelector.setItems(digitalPrinting.getPrintMashine().getQuantityColors());
        colorBackSelector.setValue(digitalPrinting.getQuantityColorsBack());

        var selectMaterial = new Select<Material>("Материал:", select ->{
            digitalPrinting.setDefaultMaterial(select.getValue());
            text.setValue(calculate());
        });
        selectMaterial.setItems(digitalPrinting.getSelectedMaterials());
        selectMaterial.setValue(digitalPrinting.getDefaultMaterial());



        var quantity = new IntegerField("Тираж:", t -> {
            this.digitalPrinting.setQuantityOfProduct(t.getValue());
            digitalPrinting.getVariables().put("quantityOfProduct", t.getValue());
            digitalPrinting.calc();
            text.setValue(calculate());

        });
        quantity.setMin(1);
        quantity.setMax(100000);
        quantity.setValue(1);

        vl.add(colorFrontSelector,colorBackSelector,selectMaterial, quantity, text);

        return vl;
    }

    private void addFooterComponents() {
        var close = new Button("Close", buttonClickEvent -> close());
        var confirm = new Button("Добавить", buttonClickEvent -> {

        });
        this.getFooter().add(confirm, close);
    }

    private String calculate(){

        var map = digitalPrinting.getVariables();
        var materialFormula = digitalPrinting.getMaterialFormula();
        setPrices();
        String sb = map.entrySet().toString();

        sb = sb.replace(",", ";");
        sb = sb.substring(1, sb.length()-1);
        sb = sb + "; ";

        var totalWork = computeFormula(sb, digitalPrinting.getFormula().getFormula());
        var totalMaterial = computeFormula(sb, digitalPrinting.getMaterialFormula().getFormula());
        Double oneProductCoast = roundPrice (digitalPrinting.getQuantityOfProduct(), totalWork+totalMaterial);
        Double total = oneProductCoast * digitalPrinting.getQuantityOfProduct();

        return "СЕБЕСТОИМОСТЬ тиража: "+total.toString();
    }

    private void setPrices() {
        digitalPrinting.getVariables().put("OSDP_MaterialPrice", digitalPrinting.getMaterial().getPriceOfLeaf());

        Double d = costService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf(digitalPrinting.getPrintMashine(),
                digitalPrinting.getQuantityColorsCover(),
                digitalPrinting.getDefaultMaterial().getSizeOfPrintLeaf()).getCoast();
        digitalPrinting.getVariables().put("OSDP_FrontPrice", d);

        d = costService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf(digitalPrinting.getPrintMashine(),
                digitalPrinting.getQuantityColorsBack(),
                digitalPrinting.getDefaultMaterial().getSizeOfPrintLeaf()).getCoast();
        digitalPrinting.getVariables().put("OSDP_BackPrice", d);
    }

    private Double computeFormula(String variableStr, String formulaStr){
        double total = 0;

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

            try {
                total += (Double) engine.eval(variableStr+"; "+formulaStr+";");
            } catch (ScriptException e) {
                Notification.show("Некорректный расчет!");
                //throw new RuntimeException(e);
            }
        return total;

    }

    private Double roundPrice(Integer quantity, Double total) {
        DecimalFormat df = new DecimalFormat();
        df.setRoundingMode(RoundingMode.HALF_UP);

        if (quantity < 50) df.applyPattern("#");
        if (50 <= quantity & quantity < 100) df.applyPattern("#.#");
        if (quantity >= 100) df.applyPattern("#.##");

        String str = df.format(total/quantity);
        str = str.replace("," , ".");

        if (quantity!=0) return Double.parseDouble(str);
        else return 0d;
    }
}
