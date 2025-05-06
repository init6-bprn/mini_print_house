package ru.bprn.printhouse.data.calculate;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class OneSheetDigitalPrintingCalculateWorkView extends VerticalLayout {
    private DigitalPrinting digitalPrinting;

    public OneSheetDigitalPrintingCalculateWorkView(List<Object> digitalPrinting) {
        super();
        this.digitalPrinting = JSONToObjectsHelper.setBeanFromJSONStr(digitalPrinting, DigitalPrinting.class);
        if (this.digitalPrinting != null) {
            add(addComponentView());
            var quantity = new IntegerField("Тираж:", t -> {
                this.digitalPrinting.setQuantityOfProduct(t.getValue());
                this.digitalPrinting.calc();
                calculate();
            });
            add(quantity);
        } else {
            Notification.show("Что-то пошло не так...");
        }


    }

    private VerticalLayout addComponentView() {
        var vl = new VerticalLayout();

        var colorFrontSelector = new Select<QuantityColors>("Цветность лицо:", selectColor -> {
            digitalPrinting.setQuantityColorsCover(selectColor.getValue());
            calculate();
        });
        colorFrontSelector.setItems(digitalPrinting.getPrintMashine().getQuantityColors());
        colorFrontSelector.setValue(digitalPrinting.getQuantityColorsCover());

        var colorBackSelector = new Select<QuantityColors>("Цветность оборот:", selectColor -> {
            digitalPrinting.setQuantityColorsBack(selectColor.getValue());
            calculate();
        });
        colorBackSelector.setItems(digitalPrinting.getPrintMashine().getQuantityColors());
        colorBackSelector.setValue(digitalPrinting.getQuantityColorsBack());

        var selectMaterial = new Select<Material>("Материал:", select ->{
            digitalPrinting.setDefaultMaterial(select.getValue());
            calculate();
        });
        selectMaterial.setItems(digitalPrinting.getSelectedMaterials());
        selectMaterial.setValue(digitalPrinting.getDefaultMaterial());

        vl.add(colorFrontSelector,colorBackSelector,selectMaterial);

        return vl;
    }

    private void calculate(){
        var map = digitalPrinting.getVariables();
        var materialFormula = digitalPrinting.getMaterialFormula();
        map.put("priceOfWork", 5);
        map.put("priceOfMaterial", digitalPrinting.getMaterial().getPriceOfLeaf());
        String sb = map.entrySet().toString();
        sb = sb.replace(",", ";");
        sb = sb.substring(1, sb.length()-1);
        var totalWork = computeFormula(sb, digitalPrinting.getFormula().getFormula());
        var totalMaterial = computeFormula(sb, digitalPrinting.getMaterialFormula().getFormula());
        Double oneProductCoast = roundPrice (digitalPrinting.getQuantityOfProduct(), totalWork+totalMaterial);
        Double total = oneProductCoast * digitalPrinting.getQuantityOfProduct();
    }

    private Double computeFormula(String mapStr, String formula){
        double total = 0;

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

            try {
                total += (Double) engine.eval(mapStr + ";" + formula+";");
            } catch (ScriptException e) {
                Notification.show("Некорректный расчет!");
                throw new RuntimeException(e);
            }
        return total;

    }

    private Double roundPrice(Integer quantity, Double total) {
        DecimalFormat df = new DecimalFormat();
        df.setRoundingMode(RoundingMode.HALF_UP);

        if (quantity < 50) df.applyPattern("#");
        if (50 <= quantity & quantity < 100) df.applyPattern("#.#");
        if (quantity >= 100) df.applyPattern("#.##");

        return Double.valueOf(df.format(total/quantity));
    }
}
