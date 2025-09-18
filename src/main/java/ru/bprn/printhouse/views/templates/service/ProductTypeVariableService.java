package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProductTypeVariableService {

    /**
     * Возвращает список предопределенных переменных для указанного класса продукта.
     * В будущем эту информацию можно будет загружать из базы данных или конфигурационных файлов.
     */
    public List<Variable> getVariablesFor(Class<? extends AbstractProductType> productTypeClass) {
        if (productTypeClass.equals(OneSheetDigitalPrintingProductType.class)) {
            return getOneSheetDigitalPrintingVariables();
        }
        // Здесь можно будет добавить 'else if' для других типов продуктов
        return Collections.emptyList();
    }

    private List<Variable> getOneSheetDigitalPrintingVariables() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("quantity", 1, "Количество изделий", Variable.VariableType.INTEGER, "1", "100000", "1", null));
        variables.add(new Variable("quantityOfMainMaterial", 1, "Количество единиц основного материала", Variable.VariableType.INTEGER, "1", "100000", "1", null));
        variables.add(new Variable("productWidth", 210.0, "Ширина изделия", Variable.VariableType.DOUBLE, "35", "1000", "0.5", null));
        variables.add(new Variable("productLength", 148.5, "Длина изделия", Variable.VariableType.DOUBLE, "35", "1000", "0.5", null));
        variables.add(new Variable("productWidthBeforeCut", 210.0, "Ширина изделия до обрезки", Variable.VariableType.DOUBLE, "35", "1000", "0.5", null));
        variables.add(new Variable("productLengthBeforeCut", 148.5, "Длина изделия до обрезки", Variable.VariableType.DOUBLE, "35", "1000", "0.5", null));
        variables.add(new Variable("mainMaterialWidth", 488, "Ширина основного материала", Variable.VariableType.INTEGER, "100", "488", "1", null));
        variables.add(new Variable("mainMaterialLength", 330, "Длина основного материала", Variable.VariableType.INTEGER, "100", "330", "1", null));
        variables.add(new Variable("mainMaterialWorkAreaWidth", 488, "Ширина рабочей области основного материала", Variable.VariableType.INTEGER, "100", "488", "1", null));
        variables.add(new Variable("mainMaterialWorkAreaLength", 330, "Длина рабочей области основного материала", Variable.VariableType.INTEGER, "100", "330", "1", null));
        variables.add(new Variable("quantityProductsOnMainMaterial", 1, "Количество изделий на основном материале", Variable.VariableType.INTEGER, "1", "100000", "1", null));
        variables.add(new Variable("columns", 1, "Количество колонок изделий на печатном листе", Variable.VariableType.INTEGER, "1", "100", "1", null));
        variables.add(new Variable("rows", 1, "Количество строк изделий на печатном листе", Variable.VariableType.INTEGER, "1", "60", "1", null));
        variables.add(new Variable("thickness", 1, "Толщина основного материала", Variable.VariableType.DOUBLE, "0.01", "1000", null, null));
        return variables;
    }
}
