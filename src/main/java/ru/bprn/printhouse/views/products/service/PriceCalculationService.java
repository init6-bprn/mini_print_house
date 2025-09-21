package ru.bprn.printhouse.views.products.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceCalculationService {

    /**
     * Рассчитывает техническую калькуляцию для шаблона на основе пользовательской конфигурации.
     *
     * @param template      Шаблон продукта.
     * @param configuration Карта с пользовательскими настройками (тираж, выбранные материалы, значения переменных).
     * @return Список результатов калькуляции для каждого компонента продукта.
     */
    public List<CalculationResult> calculate(Templates template, Map<String, Object> configuration) {
        List<CalculationResult> results = new ArrayList<>();
        // Получаем тираж из конфигурации или из переменной шаблона по умолчанию
        int quantity = (int) configuration.getOrDefault("quantity",
                getVariableValueAsDouble(template.getVariables(), "quantity", 1.0).intValue());

        // Итерируем по каждому компоненту продукта (например, "Обложка", "Внутренний блок")
        for (AbstractProductType productType : template.getProductTypes()) {
            results.add(calculateForProductType(template, productType, quantity, configuration));
        }

        return results;
    }

    private CalculationResult calculateForProductType(Templates template, AbstractProductType productType, int quantity, Map<String, Object> configuration) {
        // Логика расчета зависит от конкретного типа продукта
        if (productType instanceof OneSheetDigitalPrintingProductType osdpt) {
            return calculateForOneSheet(template, osdpt, quantity, configuration);
        }
        // Здесь можно будет добавить 'else if' для других типов продуктов (например, многостраничной продукции)
        return new CalculationResult(); // Возвращаем пустой результат для неподдерживаемых типов
    }

    /**
     * Реализация пунктов 1-9 из плана для OneSheetDigitalPrintingProductType.
     */
    private CalculationResult calculateForOneSheet(Templates template, OneSheetDigitalPrintingProductType product, int quantity, Map<String, Object> configuration) {
        CalculationResult result = new CalculationResult();
        Map<String, Object> context = new HashMap<>();

        // === Часть 1: Подготовка и расчет листажа ===

        // 1. Сбор глобального контекста
        addVariablesToContext(context, template.getVariables());
        addVariablesToContext(context, product.getVariables());
        context.put("quantity", quantity);

        // TODO: В будущем брать материал из `configuration`
        PrintSheetsMaterial selectedMaterial = product.getDefaultMaterial();
        if (selectedMaterial == null) {
            result.addError("Не выбран основной материал для компонента: " + product.getName());
            return result;
        }
        context.put("mainMaterialWidth", selectedMaterial.getSizeX());
        context.put("mainMaterialLength", selectedMaterial.getSizeY());
        context.put("thickness", selectedMaterial.getThickness());

        // 2. Проверка совместимости оборудования
        List<AbstractMachine> machines = product.getProductOperations().stream()
                .map(op -> op.getOperation().getAbstractMachine())
                .filter(Objects::nonNull)
                .toList();

        double maxMachineWidth = machines.stream().mapToDouble(m -> getVariableValueAsDouble(m.getVariables(), "max_width", Double.MAX_VALUE)).min().orElse(Double.MAX_VALUE);
        double maxMachineLength = machines.stream().mapToDouble(m -> getVariableValueAsDouble(m.getVariables(), "max_length", Double.MAX_VALUE)).min().orElse(Double.MAX_VALUE);

        if (selectedMaterial.getSizeX() > maxMachineWidth || selectedMaterial.getSizeY() > maxMachineLength) {
            result.addError("Размер материала " + selectedMaterial.getSizeX() + "x" + selectedMaterial.getSizeY() + " превышает возможности оборудования.");
            return result;
        }

        // 3. Расчет рабочей области
        double maxGapTop = machines.stream().mapToDouble(m -> getVariableValueAsDouble(m.getVariables(), "gap_top", 0.0)).max().orElse(0.0);
        double maxGapBottom = machines.stream().mapToDouble(m -> getVariableValueAsDouble(m.getVariables(), "gap_bottom", 0.0)).max().orElse(0.0);
        double maxGapLeft = machines.stream().mapToDouble(m -> getVariableValueAsDouble(m.getVariables(), "gap_left", 0.0)).max().orElse(0.0);
        double maxGapRight = machines.stream().mapToDouble(m -> getVariableValueAsDouble(m.getVariables(), "gap_right", 0.0)).max().orElse(0.0);

        double workableAreaWidth = selectedMaterial.getSizeX() - maxGapLeft - maxGapRight;
        double workableAreaLength = selectedMaterial.getSizeY() - maxGapTop - maxGapRight;
        context.put("mainMaterialWorkAreaWidth", workableAreaWidth);
        context.put("mainMaterialWorkAreaLength", workableAreaLength);

        // 4. Расчет раскладки
        double productWidth = (double) context.get("productWidth");
        double productLength = (double) context.get("productLength");
        double bleed = (double) context.get("bleed");
        boolean multiplication = (boolean) context.get("multiplication");

        double productWidthBeforeCut = productWidth + bleed * 2;
        double productLengthBeforeCut = productLength + bleed * 2;
        context.put("productWidthBeforeCut", productWidthBeforeCut);
        context.put("productLengthBeforeCut", productLengthBeforeCut);

        int quantityProductsOnMainMaterial = 1;
        if (multiplication && productWidthBeforeCut > 0 && productLengthBeforeCut > 0) {
            int itemsV1 = (int) (workableAreaWidth / productWidthBeforeCut) * (int) (workableAreaLength / productLengthBeforeCut);
            int itemsV2 = (int) (workableAreaWidth / productLengthBeforeCut) * (int) (workableAreaLength / productWidthBeforeCut);
            quantityProductsOnMainMaterial = Math.max(itemsV1, itemsV2);
            if (quantityProductsOnMainMaterial == 0) quantityProductsOnMainMaterial = 1; // Защита от деления на ноль
        }
        context.put("quantityProductsOnMainMaterial", quantityProductsOnMainMaterial);

        // 5. Расчет начального листажа
        int requiredSheets = (int) Math.ceil((double) quantity / quantityProductsOnMainMaterial);
        context.put("requiredSheets", requiredSheets);

        // 6. Расчет брака и приладки
        // Инициализируем переменные для брака и приладки в контексте.
        // Формулы будут напрямую изменять эти значения.
        context.put("finalSheets", (double) requiredSheets);
        context.put("finalQuantity", (double) quantity);
        context.put("maxSetupWasteEquivalent", 0.0); // Макс. приладка в эквиваленте изделий

        for (ProductOperation operation : product.getProductOperations()) {
            Map<String, Object> operationContext = buildOperationContext(context, operation, configuration);
            String operationWasteFormula = getVariableValueAsString(operation.getCustomVariables(), "operationWasteFormula", "0");
            String setupWasteFormula = getVariableValueAsString(operation.getCustomVariables(), "setupWasteFormula", "0");

            // Брак (operationWaste) напрямую модифицирует переменные `finalSheets` и `finalQuantity` в контексте.
            executeFormula(operationWasteFormula, operationContext);

            // Приладка (setupWaste) также напрямую модифицирует контекст, обновляя `maxSetupWasteEquivalent`.
            executeFormula(setupWasteFormula, operationContext);
        }

        // После цикла по операциям, значения брака уже находятся внутри контекста.
        // Извлекаем суммарный брак по тиражу, который был добавлен формулами.
        double totalOperationWasteQuantity = ((Number) context.getOrDefault("finalQuantity", (double) quantity)).doubleValue() - quantity;
        // Извлекаем максимальную приладку, рассчитанную формулами.
        double maxSetupWasteInEquivalentProducts = ((Number) context.getOrDefault("maxSetupWasteEquivalent", 0.0)).doubleValue();

        // 7. Расчет итогового тиража и листажа
        // К исходному тиражу добавляем суммарный брак по тиражу и максимальную приладку (уже в изделиях).
        int finalQuantity = (int) Math.ceil(quantity + totalOperationWasteQuantity + maxSetupWasteInEquivalentProducts);
        // Листаж на брак уже был добавлен в контекст, извлекаем его.
        int finalSheets = ((Number) context.get("finalSheets")).intValue();
        context.put("finalQuantity", finalQuantity);
        context.put("finalSheets", finalSheets);

        // 8. Финальная корректировка листажа
        if (finalQuantity > finalSheets * quantityProductsOnMainMaterial) {
            finalSheets = (int) Math.ceil((double) finalQuantity / quantityProductsOnMainMaterial);
            context.put("finalSheets", finalSheets);
        }

        result.setFinalSheets(finalSheets);
        result.setMainMaterial(selectedMaterial);

        // === Часть 2: Техническая калькуляция операций ===

        // 9. Расчет физических величин для каждой операции
        for (ProductOperation operation : product.getProductOperations()) {
            Map<String, Object> operationContext = buildOperationContext(context, operation, configuration);
            String machineTimeFormula = getVariableValueAsString(operation.getCustomVariables(), "machineTimeFormula", "");
            String actionFormula = getVariableValueAsString(operation.getCustomVariables(), "actionFormula", "");
            String materialFormula = getVariableValueAsString(operation.getCustomVariables(), "materialFormula", "");

            double machineTime = ((Number) evaluateFormula(machineTimeFormula, operationContext)).doubleValue();
            double actionTime = ((Number) evaluateFormula(actionFormula, operationContext)).doubleValue();
            double materialAmount = ((Number) evaluateFormula(materialFormula, operationContext)).doubleValue();

            result.addOperationResult(operation.getId(), machineTime, actionTime, materialAmount, operation.getSelectedMaterial());
        }

        return result;
    }

    private Map<String, Object> buildOperationContext(Map<String, Object> globalContext, ProductOperation operation, Map<String, Object> configuration) {
        Map<String, Object> context = new HashMap<>(globalContext);

        // Переменные из машины
        AbstractMachine machine = operation.getOperation().getAbstractMachine();
        if (machine != null) {
            addVariablesToContext(context, machine.getVariables());
        }

        // Переменные из шаблона операции
        addVariablesToContext(context, operation.getOperation().getVariables());

        // Переменные из самой операции (переопределяют все предыдущие)
        addVariablesToContext(context, operation.getCustomVariables());

        // TODO: Добавить переопределение переменных из `configuration` от пользователя

        return context;
    }

    private Object evaluateFormula(String formula, Map<String, Object> context) {
        if (formula == null || formula.isBlank()) {
            return 0.0; // Возвращаем 0.0, чтобы избежать NPE при вызове .doubleValue()
        }
        try {
            Binding binding = new Binding(context);
            GroovyShell shell = new GroovyShell(binding);
            return shell.evaluate(formula);
        } catch (Exception e) {
            System.err.println("Ошибка при вычислении Groovy формулы: " + formula);
            e.printStackTrace();
        }
        return 0.0; // Возвращаем 0.0 в случае ошибки
    }

    /**
     * Выполняет Groovy-скрипт, который может изменять переданный контекст.
     * Ничего не возвращает.
     */
    private void executeFormula(String formula, Map<String, Object> context) {
        if (formula == null || formula.isBlank()) {
            return;
        }
        try {
            Binding binding = new Binding(context);
            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(formula);
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении Groovy-скрипта: " + formula);
            e.printStackTrace();
        }
    }

    private void addVariablesToContext(Map<String, Object> context, List<Variable> variables) {
        if (variables == null) {
            return;
        }
        variables.forEach(v -> context.put(v.getKey(), v.getValueAsObject()));
    }

    private Double getVariableValueAsDouble(List<Variable> variables, String key, double defaultValue) {
        return getVariable(variables, key)
                .map(v -> (Double) v.getValueAsObject())
                .orElse(defaultValue);
    }

    private String getVariableValueAsString(List<Variable> variables, String key, String defaultValue) {
        return getVariable(variables, key)
                .map(Variable::getValue)
                .orElse(defaultValue);
    }

    private Optional<Variable> getVariable(List<Variable> variables, String key) {
        if (variables == null) return Optional.empty();
        return variables.stream()
                .filter(v -> key.equals(v.getKey()))
                .findFirst();
    }
}