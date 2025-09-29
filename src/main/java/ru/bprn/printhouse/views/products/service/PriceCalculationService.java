package ru.bprn.printhouse.views.products.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.CalculationPhase;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class PriceCalculationService {

    private final PriceOfMaterialService priceOfMaterialService;
    private final PriceOfMachineService priceOfMachineService;

    /**
     * Рассчитывает техническую калькуляцию для шаблона на основе пользовательской конфигурации.
     *
     * @param template      Шаблон продукта.
     * @param configuration Карта с пользовательскими настройками (тираж, выбранные материалы, значения переменных).
     * @return Список результатов калькуляции для каждого компонента продукта.
     */
    public CalculationResult calculate(Templates template, Map<String, Object> configuration) {
        CalculationResult finalResult = new CalculationResult();
        Map<String, Object> globalContext = buildInitialContext(template, configuration);

        // Итерируем по каждому компоненту продукта (например, "Обложка", "Внутренний блок")
        for (AbstractProductType productType : template.getProductTypes()) {
            CalculationResult componentResult = calculateForProductType(productType, globalContext);
            // TODO: Агрегировать результаты от каждого компонента в finalResult
            // Сейчас для простоты возвращаем результат первого компонента
            return componentResult;
        }

        return finalResult;
    }

    private CalculationResult calculateForProductType(AbstractProductType productType, Map<String, Object> globalContext) {
        CalculationResult result = new CalculationResult();
        Map<String, Object> context = new HashMap<>(globalContext);

        // 1. Сбор контекста и формул
        addVariablesToContext(context, productType.getVariables());
        // TODO: Добавить переменные из пользовательской конфигурации для этого productType

        List<Operation.FormulaInfo> formulas = collectAndSortFormulas(productType);

        // 2. Поэтапное выполнение формул
        executeFormulas(formulas, context, result);
        if (result.hasErrors()) return result;

        // 3. Заполнение CalculationResult из контекста
        populateResultFromContext(result, context, productType);

        // 4. Экономический расчет
        calculateCost(result, context);

        return result;
    }

    /**
     * Шаг 1: Собирает первоначальный контекст из шаблона и пользовательского ввода.
     */
    private Map<String, Object> buildInitialContext(Templates template, Map<String, Object> configuration) {
        Map<String, Object> context = new HashMap<>();
        addVariablesToContext(context, template.getVariables());
        context.putAll(configuration); // Пользовательский ввод имеет наивысший приоритет
        // Инициализация аккумуляторов
        context.put("totalCost", BigDecimal.ZERO);
        context.put("totalWeight", 0.0);
        return context;
    }

    /**
     * Шаг 2: Рекурсивно собирает и сортирует все формулы.
     */
    private List<Operation.FormulaInfo> collectAndSortFormulas(AbstractProductType productType) {
        Stream<Operation.FormulaInfo> productFormulas = productType.getFormulas().stream()
                .map(f -> new Operation.FormulaInfo(f.getExpression(), CalculationPhase.PREPARATION, 0)); //TODO: Заменить на данные из Formula

        Stream<Operation.FormulaInfo> operationFormulas = productType.getProductOperations().stream()
                .filter(po -> !po.isSwitchOff())
                .flatMap(po -> po.getOperation().getAllFormulaInfo().stream());

        return Stream.concat(productFormulas, operationFormulas)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Operation.FormulaInfo::phase).thenComparingInt(Operation.FormulaInfo::priority))
                .toList();
    }

    /**
     * Шаг 3: Выполняет отсортированный список формул, обновляя контекст.
     */
    private void executeFormulas(List<Operation.FormulaInfo> formulas, Map<String, Object> context, CalculationResult result) {
        Binding binding = new Binding(context);
        GroovyShell shell = new GroovyShell(binding);

        for (Operation.FormulaInfo formula : formulas) {
            try {
                Object formulaResult = shell.evaluate(formula.expression());
                // Если у формулы есть ключ, добавляем результат в контекст
                // TODO: Добавить ключ в FormulaInfo
                // if (formula.key() != null) {
                //     context.put(formula.key(), formulaResult);
                // }
            } catch (Exception e) {
                result.addError("Ошибка при выполнении формулы '" + formula.expression() + "': " + e.getMessage());
                // Прерываем расчет, если одна из формул неверна
                return;
            }
        }
    }

    /**
     * Шаг 4: Заполняет объект CalculationResult данными из контекста после расчетов.
     */
    private void populateResultFromContext(CalculationResult result, Map<String, Object> context, AbstractProductType productType) {
        // Основной материал
        if (productType instanceof PrintSheetsMaterial psm) { //TODO: Исправить на HasMateria
            result.setMainMaterial(psm);
        }

        result.setFinalSheets(((Number) context.getOrDefault("finalSheets", 0)).intValue());

        // Результаты по операциям
        for (ProductOperation po : productType.getProductOperations()) {
            // TODO: После добавления ключей в FormulaInfo, извлекать результаты по ключам
            // Например: double machineTime = ((Number) context.getOrDefault("machineTime_" + po.getId(), 0)).doubleValue();
            result.addOperationResult(po.getId(), 0, 0, 0, po.getSelectedMaterial());
        }
    }

    /**
     * Шаг 5: Рассчитывает итоговую стоимость на основе технологических результатов и цен.
     */
    private void calculateCost(CalculationResult result, Map<String, Object> context) {
        BigDecimal primeCost = BigDecimal.ZERO;

        // Стоимость основного материала
        if (result.getMainMaterial() != null) {
            BigDecimal materialPrice = priceOfMaterialService.getActualPriceFor(result.getMainMaterial());
            primeCost = primeCost.add(materialPrice.multiply(BigDecimal.valueOf(result.getFinalSheets())));
        }

        // Стоимость операций
        for (CalculationResult.OperationResult opResult : result.getOperationResults()) {
            // Стоимость расходного материала операции
            if (opResult.getOperationMaterial() != null) {
                BigDecimal opMaterialPrice = priceOfMaterialService.getActualPriceFor(opResult.getOperationMaterial());
                primeCost = primeCost.add(opMaterialPrice.multiply(BigDecimal.valueOf(opResult.getMaterialAmount())));
            }
            // TODO: Добавить расчет стоимости работы оборудования и сотрудника
        }

        // Применение глобальных наценок
        BigDecimal margin = BigDecimal.valueOf(((Number) context.getOrDefault("margin", 0.0)).doubleValue());
        BigDecimal tax = BigDecimal.valueOf(((Number) context.getOrDefault("tax", 0.0)).doubleValue());

        BigDecimal finalPrice = primeCost.multiply(BigDecimal.ONE.add(margin.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
        finalPrice = finalPrice.multiply(BigDecimal.ONE.add(tax.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));

        result.setTotalPrice(finalPrice);
    }

    // --- Вспомогательные методы ---

    private void addVariablesToContext(Map<String, Object> context, List<Variable> variables) {
        if (variables == null) {
            return;
        }
        variables.forEach(v -> context.put(v.getKey(), v.getValueAsObject()));
    }
}