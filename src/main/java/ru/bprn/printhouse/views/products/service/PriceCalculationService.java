package ru.bprn.printhouse.views.products.service;

import org.springframework.stereotype.Service;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class PriceCalculationService {

    /**
     * Рассчитывает итоговую стоимость для шаблона на основе пользовательской конфигурации.
     *
     * @param template      Шаблон продукта.
     * @param configuration Карта с пользовательскими настройками (тираж, выбранные материалы, значения переменных).
     * @return Итоговая стоимость в виде BigDecimal.
     */
    public int calculatePrice(Templates template, Map<String, Object> configuration) {
        int totalPrice = 0;
        int quantity = (int) configuration.getOrDefault("quantity", template.getQuantity());

        for (AbstractProductType productType : template.getProductTypes()) {
            totalPrice += calculateProductTypePrice(productType, quantity, configuration);
        }

        return totalPrice;
    }

    private int calculateProductTypePrice(AbstractProductType productType, int quantity, Map<String, Object> configuration) {
        // Подготовка общего контекста для скриптового движка
        Map<String, Object> context = new HashMap<>();

        double sizeX = getVariableValue(productType.getVariables(), "sizeX", 0.0);
        double sizeY = getVariableValue(productType.getVariables(), "sizeY", 0.0);
        double bleed = getVariableValue(productType.getVariables(), "bleed", 0.0);

        context.put("quantity", quantity);
        context.put("sizeX", sizeX);
        context.put("sizeY", sizeY);
        context.put("bleed", bleed);

        // Этап 1: Выполнение специфической логики для конкретного типа продукта
        if (productType instanceof OneSheetDigitalPrintingProductType osdpt) {
            calculateForOneSheetDigitalPrinting(osdpt, quantity, configuration, context);
        }
        // Здесь можно будет добавить 'else if' для других типов продуктов

        // Этап 2: Расчет стоимости операций на основе подготовленного контекста
        int productTypePrice = 0;
        for (ProductOperation operation : productType.getProductOperations()) {
            // Проверяем, включена ли операция в конфигурации
            boolean isEnabled = (boolean) configuration.getOrDefault("op_" + operation.getId() + "_enabled", !operation.isSwitchOff());
            if (!isEnabled) {
                continue;
            }
            productTypePrice += calculateOperationPrice(operation, context, configuration);
        }

        return productTypePrice;
    }

    /**
     * Реализация пунктов 1-4 из плана для OneSheetDigitalPrintingProductType.
     * Рассчитывает раскладку и подготавливает переменные для последующих расчетов.
     */
    private void calculateForOneSheetDigitalPrinting(OneSheetDigitalPrintingProductType product, int quantity, Map<String, Object> configuration, Map<String, Object> context) {
        // 1. Подготовка исходных данных
        double productSizeX = (double) context.getOrDefault("sizeX", 0.0);
        double productSizeY = (double) context.getOrDefault("sizeY", 0.0);
        double bleed = (double) context.getOrDefault("bleed", 0.0);

        // Получаем выбранный материал из конфигурации или материал по умолчанию
        Object materialFromConfig = configuration.get("apt_" + product.getId() + "_material");
        PrintSheetsMaterial selectedMaterial = (materialFromConfig instanceof PrintSheetsMaterial)
                ? (PrintSheetsMaterial) materialFromConfig
                : product.getDefaultMat();

        if (selectedMaterial == null) return; // Не можем считать без материала

        // 2. Расчет дообрезного формата изделия
        double preCutSizeX = productSizeX + bleed * 2;
        double preCutSizeY = productSizeY + bleed * 2;

        // 3. Расчет рабочей области печатного листа
        double materialSizeX = selectedMaterial.getSizeX();
        double materialSizeY = selectedMaterial.getSizeY();

        int maxGapTop = 0, maxGapBottom = 0, maxGapLeft = 0, maxGapRight = 0;
        for (ProductOperation operation : product.getProductOperations()) {
            AbstractMachine machine = operation.getOperation().getMachine();
            if (machine != null && machine.getGap() != null) {
                Gap gap = machine.getGap();
                maxGapTop = Math.max(maxGapTop, gap.getGapTop());
                maxGapBottom = Math.max(maxGapBottom, gap.getGapBottom());
                maxGapLeft = Math.max(maxGapLeft, gap.getGapLeft());
                maxGapRight = Math.max(maxGapRight, gap.getGapRight());
            }
        }

        double workableAreaX = materialSizeX - maxGapLeft - maxGapRight;
        double workableAreaY = materialSizeY - maxGapTop - maxGapBottom;

        // 4. Расчет раскладки (листаж)
        int itemsPerSheet = 0;
        if (preCutSizeX > 0 && preCutSizeY > 0) {
            // Вариант 1: без поворота изделия
            int itemsV1 = (int) (workableAreaX / preCutSizeX) * (int) (workableAreaY / preCutSizeY);
            // Вариант 2: с поворотом изделия
            int itemsV2 = (int) (workableAreaX / preCutSizeY) * (int) (workableAreaY / preCutSizeX);
            itemsPerSheet = Math.max(itemsV1, itemsV2);
        }

        int requiredSheets = 0;
        if (itemsPerSheet > 0) {
            requiredSheets = (int) Math.ceil((double) quantity / itemsPerSheet);
        }

        // Помещаем все рассчитанные переменные в контекст для использования в формулах
        context.put("preCutSizeX", preCutSizeX);
        context.put("preCutSizeY", preCutSizeY);
        context.put("materialSizeX", materialSizeX);
        context.put("materialSizeY", materialSizeY);
        context.put("workableAreaX", workableAreaX);
        context.put("workableAreaY", workableAreaY);
        context.put("itemsPerSheet", itemsPerSheet);
        context.put("requiredSheets", requiredSheets);

        // TODO: Пункт 5. Расчет общего количества печатных листов (totalPrintSheets)
        // Пока что totalPrintSheets = requiredSheets, но в будущем добавится приладка и брак
        context.put("totalPrintSheets", requiredSheets);
    }

    private int calculateOperationPrice(ProductOperation operation, Map<String, Object> parentContext, Map<String, Object> configuration) {
        Map<String, Object> operationContext = new HashMap<>(parentContext);

        // Добавляем переменные операции, переопределяя их пользовательскими значениями
        for (Variable var : operation.getCustomVariables()) {
            Object value = configuration.getOrDefault("var_" + var.getId(), var.getValueAsObject());
            operationContext.put(var.getKey(), value);
        }

        int materialCost = 0;
        if (operation.getCustomMaterialFormula() != null && operation.getCustomMaterialFormula().getFormula() != null) {
            AbstractMaterials selectedMaterial = (AbstractMaterials) configuration.getOrDefault("op_" + operation.getId() + "_material", operation.getSelectedMaterial());
            if (selectedMaterial != null && selectedMaterial.getPrice() != null) {
                int materialPrice = selectedMaterial.getPrice(); // Цена в копейках
                double materialAmount = evaluateFormula(operation.getCustomMaterialFormula().getFormula(), operationContext);
                materialCost = (int) (materialPrice * materialAmount);
            }
        }

        int machineCost = 0;
        if (operation.getCustomMachineTimeFormula() != null && operation.getCustomMachineTimeFormula().getFormula() != null) {
            AbstractMachine machine = operation.getOperation().getMachine();
            if (machine != null && machine.getCostPerHour() != null) {
                double costPerSecond = (double) machine.getCostPerHour() / 3600.0; // Цена в копейках за секунду
                double timeInSeconds = evaluateFormula(operation.getCustomMachineTimeFormula().getFormula(), operationContext);
                machineCost = (int) (costPerSecond * timeInSeconds);
            }
        }

        int actionCost = 0;
        if (operation.getCustomActionFormula() != null && operation.getCustomActionFormula().getFormula() != null) {
            double actionValue = evaluateFormula(operation.getCustomActionFormula().getFormula(), operationContext);
            actionCost = (int) actionValue; // Предполагаем, что формула возвращает копейки
        }

        return materialCost + machineCost + actionCost;
    }

    private double evaluateFormula(String formula, Map<String, Object> context) {
        if (formula == null || formula.isBlank()) {
            return 0.0;
        }
        try {
            Binding binding = new Binding(context);
            GroovyShell shell = new GroovyShell(binding);

            Object result = shell.evaluate(formula);
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (Exception e) { // Ловим более широкий спектр исключений от Groovy
            System.err.println("Ошибка при вычислении Groovy формулы: " + formula);
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getVariableValue(Set<Variable> variables, String key, double defaultValue) {
        if (variables == null) {
            return defaultValue;
        }
        return variables.stream()
                .filter(v -> key.equals(v.getKey()))
                .findFirst()
                .map(Variable::getValueAsObject)
                .filter(Number.class::isInstance)
                .map(v -> ((Number) v).doubleValue()).orElse(defaultValue);
    }
}