package ru.bprn.printhouse.data.calculate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.service.CostOfPrintSizeLeafAndColorService;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;
import ru.bprn.printhouse.data.service.PriceOfMaterialService;
import ru.bprn.printhouse.data.service.PrintSpeedMaterialDensityService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class OneSheetDigitalPrintingCalculateWorkView extends Dialog {
    private final DigitalPrinting digitalPrinting;
    private final CostOfPrintSizeLeafAndColorService costService;
    private final PriceOfMaterialService materialService;
    private final PrintSpeedMaterialDensityService materialDensityService;

    public OneSheetDigitalPrintingCalculateWorkView(List<Object> digitalPrinting,
                                                    CostOfPrintSizeLeafAndColorService costService,
                                                    PriceOfMaterialService priceOfMaterialService,
                                                    PrintSpeedMaterialDensityService materialDensityService,
                                                    String str) {
        super(str);
        this.digitalPrinting = JSONToObjectsHelper.setBeanFromJSONStr(digitalPrinting, DigitalPrinting.class);
        this.costService = costService;
        this.materialService = priceOfMaterialService;
        this.materialDensityService = materialDensityService;
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
        BigDecimal b = new BigDecimal(0);



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

        double totalWork = computeFormula(sb, digitalPrinting.getFormula().getFormula());
        double totalMaterial = computeFormula(sb, digitalPrinting.getMaterialFormula().getFormula());
        double totalEmployer = computeFormula(sb,
                digitalPrinting.getVariables().get("OSDP_EmployerPrice").toString()
                + "*"+digitalPrinting.getVariables().get("quantityOfPrintSheets").toString());
        int oneProductCost = roundHalfUp((int) (totalWork + totalMaterial + totalEmployer), digitalPrinting.getQuantityOfProduct());
        int total = oneProductCost * digitalPrinting.getQuantityOfProduct();

        return "СЕБЕСТОИМОСТЬ работ: "+ totalWork/100
                +"; СЕБЕСТОИМОСТЬ материала: "+ totalMaterial/100
                +"; СЕБЕСТОИМОСТЬ времени: "+ totalEmployer/100
                +"; СЕБЕСТОИМОСТЬ тиража: "+precisionPrice(total);
    }

    private int roundHalfUp(int dividend, int divisor) {
        if (divisor>0) {
            int quotient = dividend / divisor;
            int remainder = dividend % divisor;
            if (divisor <= remainder * 2) quotient++;
            return quotient;
        }
        else return 0;
    }

    private void setPrices() {

        //Цена единицы материала
        digitalPrinting.getVariables().put("OSDP_MaterialPrice",
                (materialService.find(digitalPrinting.getMaterial()).getPrice()));

        //Цена печатного клика лица
        int d = costService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf(digitalPrinting.getPrintMashine(),
                digitalPrinting.getQuantityColorsCover(),
                digitalPrinting.getDefaultMaterial().getSizeOfPrintLeaf()).getCost();
        digitalPrinting.getVariables().put("OSDP_FrontPrice", d);

        //Цена печатного клика оборота
        d = costService.findByPrintMashineAndQuantityColorsSizeOfPrintLeaf(digitalPrinting.getPrintMashine(),
                digitalPrinting.getQuantityColorsBack(),
                digitalPrinting.getDefaultMaterial().getSizeOfPrintLeaf()).getCost();
        digitalPrinting.getVariables().put("OSDP_BackPrice", d);

        //Цена единицы работы (время работы печатника на 1 клик)
        var priceForSec = 50;

        var priceOneOperation = priceForSec * materialDensityService.findTimeOfOperation(digitalPrinting.getPrintMashine(),
                digitalPrinting.getDefaultMaterial().getThickness(), digitalPrinting.getDefaultMaterial().getSizeOfPrintLeaf());
        digitalPrinting.getVariables().put("OSDP_EmployerPrice",  (double) priceOneOperation);
        int i = 0;
        if (digitalPrinting.getVariables().get("OSDP_FrontPrice").intValue() > 0) i++;
        if (digitalPrinting.getVariables().get("OSDP_BackPrice").intValue() > 0) i++;
        digitalPrinting.getVariables().put("OSDP_EmployerPrice", digitalPrinting.getVariables().get("OSDP_EmployerPrice").doubleValue()*i);

    }

    private double computeFormula(String variableStr, String formulaStr){
        double total = 0;

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

            try {

                total += (double) engine.eval(variableStr+"; "+formulaStr+";");
            } catch (ScriptException e) {
                Notification.show("Некорректный расчет!");
                //throw new RuntimeException(e);
            }
        return total;

    }

    private String precisionPrice(int quantity) {
        DecimalFormat df = new DecimalFormat();
        df.setRoundingMode(RoundingMode.HALF_UP);

        // В рубли из копеек
        if (quantity <= 99) df.applyPattern("#");
        if (100 <= quantity & quantity < 1000) df.applyPattern("#.#");
        if (quantity >= 1000) df.applyPattern("#.##");

       return df.format(quantity/100d);
    }
}
