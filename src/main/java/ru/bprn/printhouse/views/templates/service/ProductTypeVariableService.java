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
        variables.add(new Variable("productWidth", 210.0, "Ширина изделия", Variable.VariableType.DOUBLE, "35", "1000", "0.5", null));
        variables.add(new Variable("productLength", 148.5, "Длина изделия", Variable.VariableType.DOUBLE, "35", "1000", "0.5", null));
        variables.add(new Variable("bleed", 2, "Поле на подрезку", Variable.VariableType.DOUBLE, "-7", "10", "0.5", null));
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
        variables.add(new Variable("multiplication", "true", "Замостить", Variable.VariableType.BOOLEAN, null,null, null, null));
        variables.add(new Variable("requiredSheets", 0, "Расчетный листаж (без брака)", Variable.VariableType.INTEGER, "0", "1000000", "1", null));
        variables.add(new Variable("finalQuantity", 0, "Итоговый тираж (с браком)", Variable.VariableType.INTEGER, "0", "1000000", "1", null)); // Эта переменная уже была, оставляем
        variables.add(new Variable("finalSheets", 0, "Итоговый листаж (с браком и приладкой)", Variable.VariableType.INTEGER, "0", "1000000", "1", null)); // Эта переменная уже была, оставляем
        variables.add(new Variable("maxSetupWasteEquivalent", 0.0, "Максимальная приладка в эквиваленте изделий", Variable.VariableType.DOUBLE, "0", "1000000", "1", null));
        variables.add(new Variable("componentPrimeCost", 0.0, "Себестоимость компонента", Variable.VariableType.DOUBLE, "0", "10000000", "0.01", null));
        
        variables.add(new Variable("setupFormula", """
            // --- Скрипт на Groovy, расчитываем листаж и заполняем значениями динамические переменные ---
            // --- Шаг 1: Рассчитываем полный размер изделия с учетом вылетов ---
            // Вылеты добавляются с двух сторон, поэтому умножаем на 2.
            // Обновляем переменные размера изделия до обрезки.
            productWidthBeforeCut = productWidth + (bleed * 2)
            productLengthBeforeCut = productLength + (bleed * 2)
            
            // --- Шаг 2: Рассчитываем количество для книжной ориентации (без поворота) ---
            // (int) - это целочисленное деление, отбрасывающее остаток
            def cols_v = (int)(mainMaterialWorkAreaWidth / productWidthBeforeCut)
            def rows_v = (int)(mainMaterialWorkAreaLength / productLengthBeforeCut)
            def total_v = cols_v * rows_v
            
            // --- Шаг 3: Рассчитываем количество для альбомной ориентации (с поворотом на 90 градусов) ---
            // Меняем местами ширину и длину изделия
            def cols_h = (int)(mainMaterialWorkAreaWidth / productLengthBeforeCut)
            def rows_h = (int)(mainMaterialWorkAreaLength / productWidthBeforeCut)
            def total_h = cols_h * rows_h
            
            // --- Шаг 4: Выбираем лучший вариант и сохраняем результаты ---
            if (total_v >= total_h) {
                // Книжная ориентация лучше или равна альбомной
                quantityProductsOnMainMaterial = total_v
                columns = cols_v
                rows = rows_v
            } else {
                // Альбомная ориентация лучше
                quantityProductsOnMainMaterial = total_h
                columns = cols_h
                rows = rows_h
            }
            
            // --- Шаг 5: Рассчитываем базовый листаж (без учета брака) ---
            // Используем Math.ceil для округления в большую сторону.
            if (quantityProductsOnMainMaterial > 0) {
                requiredSheets = Math.ceil(quantity / quantityProductsOnMainMaterial)
            } else {
                requiredSheets = Double.POSITIVE_INFINITY // Индикатор ошибки: изделие не помещается на лист
            }
            
        """, "Формула расчета материала", Variable.VariableType.STRING, null, null, null, null));
        variables.add(new Variable("materialFormula", "", "Формула расчета материала", Variable.VariableType.STRING, null, null, null, null));
        variables.add(new Variable("finalAdjustmentFormula", """
            // Шаг 4.1: Учет приладки
            // Фактический тираж увеличивается на максимальную требуемую приладку.
            finalQuantity += maxSetupWasteEquivalent
            
            // Шаг 4.2: Финальная проверка и корректировка листажа.
            // Проверяется, достаточно ли рассчитанных листов для производства фактического тиража.
            if (quantityProductsOnMainMaterial > 0 && finalSheets * quantityProductsOnMainMaterial < finalQuantity) {
                // Если не хватает, листаж пересчитывается в большую сторону.
                finalSheets = Math.ceil(finalQuantity / quantityProductsOnMainMaterial)
            }
            
            // Шаг 4.3: Финальный пересчет тиража на основе итогового листажа
            // Это гарантирует, что тираж точно соответствует количеству изделий на листах.
            finalQuantity = finalSheets * quantityProductsOnMainMaterial
        """, "Формула финальной корректировки", Variable.VariableType.STRING, null, null, null, null));
        return variables;
    }
}
