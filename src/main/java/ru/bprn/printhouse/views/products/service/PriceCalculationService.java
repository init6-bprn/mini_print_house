package ru.bprn.printhouse.views.products.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.products.entity.ComponentCalculation;
import ru.bprn.printhouse.views.products.entity.FinalCalculation;
import ru.bprn.printhouse.views.products.entity.OperationCalculation;
import ru.bprn.printhouse.views.products.entity.PriceOfMachine;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.products.repository.PriceOfMachineRepository;
import ru.bprn.printhouse.views.products.repository.PriceOfMaterialRepository;
import ru.bprn.printhouse.views.templates.entity.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class PriceCalculationService {

    private static final Logger log = LoggerFactory.getLogger(PriceCalculationService.class);

    private final PriceOfMaterialRepository priceOfMaterialRepository;
    private final PriceOfMachineRepository priceOfMachineRepository;
    private final SecureGroovyService secureGroovyService;

    /**
     * Главный метод для расчета итоговой стоимости продукта по шаблону.
     *
     * @param template   Шаблон продукта.
     * @param userInputs Карта с пользовательскими настройками (как минимум, "quantity").
     * @return Итоговая стоимость в копейках.
     */
    public FinalCalculation calculate(Templates template, Map<String, Object> userInputs) {
        Map<String, Object> globalContext = buildInitialContext(template, userInputs);
        List<ComponentCalculation> componentCalculations = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Этап 2: Расчет по каждому компоненту
        for (AbstractProductType productType : template.getProductTypes()) {
            try {
                ComponentCalculation componentCalc = calculateComponent(productType, globalContext);
                componentCalculations.add(componentCalc);
            } catch (Exception e) {
                log.error("Ошибка при расчете компонента '{}': {}", productType.getName(), e.getMessage(), e);
                errors.add("Ошибка в компоненте '" + productType.getName() + "': " + e.getMessage());
            }
        }

        // Если были ошибки на уровне компонентов, дальнейший расчет не имеет смысла
        if (!errors.isEmpty()) {
            return new FinalCalculation(0L, componentCalculations, errors);
        }

        long totalPrimeCost = componentCalculations.stream().mapToLong(ComponentCalculation::getPrimeCost).sum();

        // Этап 3: Финальный расчет отпускной цены
        double margin = ((Number) globalContext.getOrDefault("margin", 0.0)).doubleValue();
        double tax = ((Number) globalContext.getOrDefault("tax", 0.0)).doubleValue();
        double banking = ((Number) globalContext.getOrDefault("banking", 0.0)).doubleValue();

        double sellingPrice = totalPrimeCost;
        sellingPrice *= (1 + margin / 100);
        sellingPrice *= (1 + tax / 100);
        sellingPrice *= (1 + banking / 100);

        // Округление
        int quantity = ((Number) globalContext.get("quantity")).intValue();
        double pricePerOneKopecks = sellingPrice / quantity;

        String roundMaskFormula = (String) globalContext.getOrDefault("roundMask", "'#.##'");
        String actualRoundMask = (String) evaluate(globalContext, roundMaskFormula, "#.##");

        double roundedPricePerOneKopecks = roundUpByMask(pricePerOneKopecks, actualRoundMask);

        long finalPrice = (long) (roundedPricePerOneKopecks * quantity);

        return new FinalCalculation(finalPrice, componentCalculations, errors);
    }

    /**
     * Этап 1: Инициализация глобального контекста расчета.
     */
    private Map<String, Object> buildInitialContext(Templates template, Map<String, Object> userInputs) {
        Map<String, Object> context = new HashMap<>();
        addVariablesToContext(context, template.getVariables());
        context.putAll(userInputs); // Пользовательский ввод переопределяет значения по умолчанию
        // Инициализируем аккумуляторы
        context.put("totalWeight", 0.0);
        context.put("totalManufacturingTime", 0.0);
        return context;
    }

    /**
     * Рассчитывает все параметры для одного компонента продукта.
     */
    private ComponentCalculation calculateComponent(AbstractProductType productType, Map<String, Object> globalContext) {
        // 2.1. Создание локального контекста компонента
        Map<String, Object> componentContext = new HashMap<>(globalContext);
        addVariablesToContext(componentContext, productType.getVariables());
        addMaterialVariablesToContext(componentContext, productType.getDefaultMaterial());

        // 2.2. Определение рабочей области листа
        defineWorkableArea(componentContext, productType);

        // 2.3. Вызов специфичной логики продукта для расчета раскладки
        productType.calculateLayoutSpecifics(componentContext);

        // 2.4. Предварительный расчет `realQuantity` (брак и приладка)
        calculateWasteAndSetup(componentContext, productType);

        // 2.5. Финальная корректировка листажа
        finalizeQuantities(componentContext, productType);

        // 2.6. Расчет физических величин и себестоимости операций
        List<OperationCalculation> operationCalculations = calculateOperations(componentContext, productType);

        // 2.7. Расчет стоимости и веса основного материала
        AbstractMaterials mainMaterial = productType.getDefaultMaterial();
        double finalSheets = ((Number) componentContext.getOrDefault("finalSheets", 0.0)).doubleValue();
        long mainMaterialCost = (long) (finalSheets * getMaterialPriceInKopecks(mainMaterial));
        double componentWeight = finalSheets * ((Number) componentContext.getOrDefault("mass", 0.0)).doubleValue();

        return new ComponentCalculation(
                productType.getId(),
                productType.getName(),
                componentWeight,
                mainMaterialCost,
                operationCalculations,
                mainMaterial
        );
    }

    private void defineWorkableArea(Map<String, Object> context, AbstractProductType productType) {
        double maxGapTop = 0.0, maxGapBottom = 0.0, maxGapLeft = 0.0, maxGapRight = 0.0;

        for (ProductOperation po : productType.getProductOperations()) {
            if (po.isSwitchOff()) continue;
            AbstractMachine machine = po.getOperation().getAbstractMachine();
            if (machine == null) continue;

            for (Variable var : machine.getVariables()) {
                Object value = var.getValueAsObject();
                if (value instanceof Number num) {
                    switch (var.getKey()) {
                        case "gap_top" -> maxGapTop = Math.max(maxGapTop, num.doubleValue());
                        case "gap_bottom" -> maxGapBottom = Math.max(maxGapBottom, num.doubleValue());
                        case "gap_left" -> maxGapLeft = Math.max(maxGapLeft, num.doubleValue());
                        case "gap_right" -> maxGapRight = Math.max(maxGapRight, num.doubleValue());
                    }
                }
            }
        }

        double sheetWidth = ((Number) context.getOrDefault("sheetWidth", 0.0)).doubleValue();
        double sheetLength = ((Number) context.getOrDefault("sheetLength", 0.0)).doubleValue();

        context.put("workableSheetWidth", sheetWidth - maxGapLeft - maxGapRight);
        context.put("workableSheetLength", sheetLength - maxGapTop - maxGapBottom);
    }

    private void calculateWasteAndSetup(Map<String, Object> context, AbstractProductType productType) {
        context.put("finalQuantity", context.get("quantity"));
        context.put("maxSetupWasteEquivalent", 0.0);

        for (ProductOperation po : productType.getProductOperations()) {
            if (po.isSwitchOff()) continue;
            Map<String, Object> opContext = buildOperationContext(context, po);
            evaluate(opContext, po.getOperation().getWasteExpression(), null);
            evaluate(opContext, po.getOperation().getSetupExpression(), null);
            // Возвращаем обновленные значения в основной контекст
            context.put("finalQuantity", opContext.get("finalQuantity"));
            context.put("maxSetupWasteEquivalent", opContext.get("maxSetupWasteEquivalent"));
        }
    }

    private void finalizeQuantities(Map<String, Object> context, AbstractProductType productType) {
        // Инициализируем finalSheets базовым значением
        context.put("finalSheets", context.get("baseSheets"));

        getVariable(productType, "finalAdjustmentFormula")
                .map(Variable::getValue)
                .ifPresent(formula -> evaluate(context, formula, null));
    }

    private List<OperationCalculation> calculateOperations(Map<String, Object> context, AbstractProductType productType) {
        List<OperationCalculation> calculations = new ArrayList<>();

        for (ProductOperation productOperation : productType.getProductOperations()) {
            if (productOperation.isSwitchOff()) continue;

            Operation opTemplate = productOperation.getOperation();
            Map<String, Object> opContext = buildOperationContext(context, productOperation);

            // Расчет физических величин
            double machineTime = (double) evaluate(opContext, opTemplate.getMachineTimeExpression(), 0.0);
            double workerTime = (double) evaluate(opContext, opTemplate.getActionTimeExpression(), 0.0);
            double materialAmount = (double) evaluate(opContext, opTemplate.getMaterialAmountExpression(), 0.0);

            // Расчет стоимостей
            long machinePricePerHour = getMachinePriceInKopecks(opTemplate.getAbstractMachine());
            long machineCost = (long) ((machineTime / 3600) * machinePricePerHour);

            double workerRateRub = ((Number) context.getOrDefault("worker_rate", 0.0)).doubleValue();
            long workerRateKopecks = (long) (workerRateRub * 100);
            long workerCost = (long) ((workerTime / 3600) * workerRateKopecks);

            AbstractMaterials opMaterial = productOperation.getSelectedMaterial();
            long opMaterialPrice = getMaterialPriceInKopecks(opMaterial);
            long materialCost = (long) (materialAmount * opMaterialPrice);

            calculations.add(new OperationCalculation(
                    productOperation.getId(),
                    opTemplate.getName(),
                    machineTime,
                    workerTime,
                    materialAmount,
                    machineCost,
                    workerCost,
                    materialCost,
                    opMaterial
            ));
        }
        return calculations;
    }

    // --- Вспомогательные методы ---

    private Object evaluate(Map<String, Object> context, String formula, Object defaultValue) {
        if (!isNotBlank(formula)) return null;
        try {
            Object result = secureGroovyService.evaluate(context, formula);
            if (result == null) return defaultValue;
            if (result instanceof Number) return ((Number) result).doubleValue();
            return result;
        } catch (Exception e) {
            log.error("Ошибка выполнения формулы: '{}'", formula.trim(), e);
            throw new IllegalStateException("Ошибка в формуле: " + e.getMessage(), e);
        }
    }

    private void addVariablesToContext(Map<String, Object> context, List<Variable> variables) {
        if (variables == null) {
            return;
        }
        variables.forEach(v -> context.put(v.getKey(), v.getValueAsObject()));
    }

    private void addMaterialVariablesToContext(Map<String, Object> context, AbstractMaterials material) {
        if (material instanceof PrintSheetsMaterial psm) {
            context.put("sheetWidth", (double) psm.getSizeX());
            context.put("sheetLength", (double) psm.getSizeY());
            context.put("density", (double) psm.getThickness());
        }
        // Здесь можно будет добавить 'else if' для других типов материалов (рулонные и т.д.)
    }


    private Map<String, Object> buildOperationContext(Map<String, Object> parentContext, ProductOperation po) {
        Map<String, Object> opContext = new HashMap<>(parentContext);
        addVariablesToContext(opContext, po.getOperation().getAbstractMachine().getVariables());
        addVariablesToContext(opContext, po.getOperation().getVariables());
        addVariablesToContext(opContext, po.getCustomVariables());
        return opContext;
    }

    private long getMaterialPriceInKopecks(AbstractMaterials material) {
        if (material == null) return 0L;
        return priceOfMaterialRepository
                .findFirstByMaterialAndEffectiveDateBeforeOrderByEffectiveDateDesc(material, LocalDateTime.now())
                .map(PriceOfMaterial::getPrice)
                .map(price -> (long) (price * 100))
                .orElse(0L);
    }

    private long getMachinePriceInKopecks(AbstractMachine machine) {
        if (machine == null) return 0L;
        return priceOfMachineRepository
                .findFirstByMachineAndEffectiveDateBeforeOrderByEffectiveDateDesc(machine, LocalDateTime.now())
                .map(PriceOfMachine::getPrice)
                .map(price -> (long) (price * 100))
                .orElse(0L);
    }

    private Optional<Variable> getVariable(AbstractProductType productType, String key) {
        return productType.getVariables().stream().filter(v -> key.equals(v.getKey())).findFirst();
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Округляет значение в копейках ВВЕРХ (ceil) в соответствии с маской.
     * @param kopecks Сумма в копейках.
     * @param mask Маска округления ('#', '#.#', '#.##').
     * @return Округленная сумма в копейках.
     */
    private double roundUpByMask(double kopecks, String mask) {
        return switch (mask) {
            // Округление до целых рублей (до 100 копеек)
            case "#" -> Math.ceil(kopecks / 100.0) * 100.0;

            // Округление до десятков копеек (до 10 копеек)
            case "#.#" -> Math.ceil(kopecks / 10.0) * 10.0;

            // Округление до целых копеек
            case "#.##" -> Math.ceil(kopecks);

            // По умолчанию - округление до копеек
            default -> Math.ceil(kopecks);
        };
    }
}