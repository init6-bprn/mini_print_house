package ru.bprn.printhouse.views.products.service;

import com.vaadin.flow.component.notification.Notification;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.products.entity.PriceOfMachine;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.products.repository.PriceOfMachineRepository;
import ru.bprn.printhouse.views.products.repository.PriceOfMaterialRepository;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.text.DecimalFormat;
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
    public CalculationReport calculateTotalPrice(Templates template, Map<String, Object> userInputs) {
        StringBuilder reportBuilder = new StringBuilder();
        Map<String, Object> globalContext = buildInitialContext(template, userInputs);
        reportBuilder.append("--- Начало расчета ---\n");
        reportBuilder.append("Глобальный контекст: ").append(globalContext).append("\n");

        long totalPrimeCost = 0L; // Общая себестоимость в копейках

        // Шаг 2: Расчет каждого компонента
        for (AbstractProductType productType : template.getProductTypes()) {
            totalPrimeCost += calculateComponentPrimeCost(productType, globalContext, reportBuilder);
        }
        reportBuilder.append("\nОбщая себестоимость всех компонентов: ").append(totalPrimeCost / 100.0).append(" руб.\n");

        // Шаг 6: Расчет итоговой отпускной цены
        double margin = ((Number) globalContext.getOrDefault("margin", 0.0)).doubleValue();
        double tax = ((Number) globalContext.getOrDefault("tax", 0.0)).doubleValue();
        double banking = ((Number) globalContext.getOrDefault("banking", 0.0)).doubleValue();

        double sellingPrice = totalPrimeCost;
        sellingPrice *= (1 + margin / 100);
        reportBuilder.append("После наценки (").append(margin).append("%): ").append(String.format("%.2f", sellingPrice / 100.0)).append(" руб.\n");
        sellingPrice *= (1 + tax / 100);
        reportBuilder.append("После налога (").append(tax).append("%): ").append(String.format("%.2f", sellingPrice / 100.0)).append(" руб.\n");
        sellingPrice *= (1 + banking / 100);
        reportBuilder.append("После банковской комиссии (").append(banking).append("%): ").append(String.format("%.2f", sellingPrice / 100.0)).append(" руб.\n");

        // Шаг 7: Округление
        int quantity = ((Number) globalContext.get("quantity")).intValue();
        double pricePerOne = sellingPrice / quantity;

        // Сначала получаем и ВЫПОЛНЯЕМ формулу, чтобы получить маску
        String roundMaskFormula = (String) globalContext.getOrDefault("roundMask", "'#.##'"); // По умолчанию - строка '#.##'
        String actualRoundMask = (String) evaluate(globalContext, roundMaskFormula);
        if (actualRoundMask == null || actualRoundMask.isBlank()) actualRoundMask = "#.##"; // Защита от пустой маски
        reportBuilder.append("Маска округления: '").append(actualRoundMask).append("'\n");

        // Теперь используем полученную маску для форматирования
        DecimalFormat df = new DecimalFormat(actualRoundMask);
        double roundedPricePerOne = Double.parseDouble(df.format(pricePerOne).replace(',', '.'));
        reportBuilder.append("Цена за единицу до округления: ").append(String.format("%.4f", pricePerOne / 100.0)).append(" руб.\n");
        reportBuilder.append("Цена за единицу после округления: ").append(roundedPricePerOne / 100.0).append(" руб.\n");

        long finalPrice = (long) (roundedPricePerOne * quantity);
        reportBuilder.append("Итоговая отпускная цена (за весь тираж): ").append(finalPrice / 100.0).append(" руб.\n");
        reportBuilder.append("--- Конец расчета ---\n");

        return new CalculationReport(finalPrice, reportBuilder.toString());
    }

    /**
     * Шаг 1: Инициализация глобального контекста расчета.
     */
    private Map<String, Object> buildInitialContext(Templates template, Map<String, Object> userInputs) {
        Map<String, Object> context = new HashMap<>();
        addVariablesToContext(context, template.getVariables());
        context.putAll(userInputs); // Пользовательский ввод переопределяет значения по умолчанию
        return context;
    }

    /**
     * Рассчитывает себестоимость одного компонента продукта.
     */
    private long calculateComponentPrimeCost(AbstractProductType productType, Map<String, Object> globalContext, StringBuilder reportBuilder) {
        reportBuilder.append("\n--- Расчет компонента: ").append(productType.getName()).append(" ---\n");
        Map<String, Object> context = new HashMap<>(globalContext);
        addVariablesToContext(context, productType.getVariables());
        reportBuilder.append("  Контекст компонента: ").append(context).append("\n");

        // Шаг 2.4: Выполнение формулы настройки (раскладка и т.д.)
        if (productType instanceof OneSheetDigitalPrintingProductType one) {
            setupContext(one, context);
            getVariable(one, "setupFormula")
                    .map(Variable::getValue)
                    .ifPresent(formula -> {
                        reportBuilder.append("  Выполнение setupFormula...\n");
                        evaluate(context, formula);
                    });
        }
        reportBuilder.append("  После setupFormula: quantityProductsOnMainMaterial=").append(context.get("quantityProductsOnMainMaterial"))
                .append(", requiredSheets=").append(context.get("requiredSheets")).append("\n");

        // Инициализируем finalSheets базовым значением перед расчетом брака
        context.put("finalSheets", context.get("requiredSheets"));
        reportBuilder.append("  Инициализация finalSheets: ").append(context.get("finalSheets")).append("\n");

        // Инициализируем finalQuantity базовым тиражом перед расчетом брака
        context.put("finalQuantity", context.get("quantity"));
        reportBuilder.append("  Инициализация finalQuantity: ").append(context.get("finalQuantity")).append("\n");


        // Шаг 3: Расчет брака и приладки
        for (ProductOperation operation : productType.getProductOperations()) {
            if (operation.isSwitchOff()) continue;
            reportBuilder.append("  Операция '").append(operation.getOperation().getName()).append("':\n");
            reportBuilder.append("    Выполнение wasteExpression...\n");
            evaluate(context, operation.getOperation().getWasteExpression());
            reportBuilder.append("    Выполнение setupExpression...\n");
            evaluate(context, operation.getOperation().getSetupExpression());
        }
        reportBuilder.append("  После расчета брака и приладки: finalQuantity=").append(context.get("finalQuantity"))
                .append(", maxSetupWasteEquivalent=").append(context.get("maxSetupWasteEquivalent")).append("\n");

        // Шаг 4: Финальная корректировка листажа
        getVariable(productType, "finalAdjustmentFormula")
                .map(Variable::getValue)
                .ifPresent(formula -> {
                    reportBuilder.append("  Выполнение finalAdjustmentFormula...\n");
                    evaluate(context, formula);
                });
        reportBuilder.append("  После финальной корректировки: finalQuantity=").append(context.get("finalQuantity"))
                .append(", finalSheets=").append(context.get("finalSheets")).append("\n");

        // Шаг 5: Расчет себестоимости компонента
        long componentPrimeCost = 0L;

        // 5.1 Стоимость основного материала
        AbstractMaterials mainMaterial = productType.getDefaultMaterial();
        if (mainMaterial != null) {
            double finalSheets = ((Number) context.getOrDefault("finalSheets", 0.0)).doubleValue();
            reportBuilder.append("  Стоимость основного материала (").append(mainMaterial.getName()).append("): ").append(finalSheets).append(" листов\n");
            long materialPriceInKopecks = getMaterialPriceInKopecks(mainMaterial);
            componentPrimeCost += (long) (finalSheets * materialPriceInKopecks);
        }

        // 5.2 Стоимость операций
        for (ProductOperation productOperation : productType.getProductOperations()) {
            if (productOperation.isSwitchOff()) continue;

            Operation opTemplate = productOperation.getOperation();
            reportBuilder.append("  Стоимость операции '").append(opTemplate.getName()).append("':\n");
            Map<String, Object> opContext = buildOperationContext(context, productOperation);

            // Стоимость работы оборудования
            if (isNotBlank(opTemplate.getMachineTimeExpression())) {
                double machineTime = (double) evaluate(opContext, opTemplate.getMachineTimeExpression());
                reportBuilder.append("    Время машины: ").append(String.format("%.2f", machineTime)).append(" сек.\n");
                long machinePricePerHour = getMachinePriceInKopecks(opTemplate.getAbstractMachine());
                componentPrimeCost += (long) ((machineTime / 3600) * machinePricePerHour);
            }

            // Стоимость ручной работы
            if (isNotBlank(opTemplate.getActionTimeExpression())) {
                double actionTime = (double) evaluate(opContext, opTemplate.getActionTimeExpression());
                reportBuilder.append("    Время ручной работы: ").append(String.format("%.2f", actionTime)).append(" сек.\n");
                double workerRateRub = ((Number) context.getOrDefault("worker_rate", 0.0)).doubleValue();
                long workerRateKopecks = (long) (workerRateRub * 100);
                componentPrimeCost += (long) ((actionTime / 3600) * workerRateKopecks);
            }

            // Стоимость расходного материала операции
            if (isNotBlank(opTemplate.getMaterialAmountExpression())) {
                double materialAmount = (double) evaluate(opContext, opTemplate.getMaterialAmountExpression());
                reportBuilder.append("    Расход материала операции: ").append(String.format("%.2f", materialAmount)).append("\n");
                AbstractMaterials opMaterial = productOperation.getSelectedMaterial();
                if (opMaterial != null && materialAmount > 0) {
                    long opMaterialPrice = getMaterialPriceInKopecks(opMaterial);
                    componentPrimeCost += (long) (materialAmount * opMaterialPrice);
                }
            }
        }
        reportBuilder.append("  Итоговая себестоимость компонента: ").append(componentPrimeCost / 100.0).append(" руб.\n");
        return componentPrimeCost;
    }

    // --- Вспомогательные методы ---

    private Object evaluate(Map<String, Object> context, String formula) {
        if (!isNotBlank(formula)) return null;
        try {
            Object result = secureGroovyService.evaluate(context, formula);
            // Если формула ничего не возвращает (например, просто изменяет контекст),
            // то возвращаем null, чтобы избежать ошибок приведения типов.
            if (result == null) return null;
            if (result instanceof Number) return ((Number) result).doubleValue();
            return result;
        } catch (Exception e) {
            // Ловим любую ошибку при выполнении скрипта, чтобы приложение не падало.
            log.error("Ошибка выполнения формулы: '{}'", formula.trim(), e);
            Notification.show("Ошибка в формуле: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            // Возвращаем 0.0, чтобы избежать NullPointerException в коде, который ожидает число.
            return 0.0;
        }
    }

    private void addVariablesToContext(Map<String, Object> context, List<Variable> variables) {
        if (variables == null) {
            return;
        }
        variables.forEach(v -> context.put(v.getKey(), v.getValueAsObject()));
    }

    private Map<String, Object> buildOperationContext(Map<String, Object> parentContext, ProductOperation po) {
        Map<String, Object> opContext = new HashMap<>(parentContext);
        addVariablesToContext(opContext, po.getOperation().getAbstractMachine().getVariables());
        addVariablesToContext(opContext, po.getOperation().getVariables());
        addVariablesToContext(opContext, po.getCustomVariables());
        return opContext;
    }

    private long getMaterialPriceInKopecks(AbstractMaterials material) {
        return priceOfMaterialRepository
                .findFirstByMaterialAndEffectiveDateBeforeOrderByEffectiveDateDesc(material, LocalDateTime.now())
                .map(PriceOfMaterial::getPrice)
                .map(price -> (long) (price * 100))
                .orElse(0L);
    }

    private long getMachinePriceInKopecks(AbstractMachine machine) {
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

    // Этот метод остается как временное решение для подготовки контекста
    private void setupContext(OneSheetDigitalPrintingProductType one, Map<String, Object> context) {
        PrintSheetsMaterial material = one.getDefaultMaterial();
        if (material == null) return;

        int materialWidth = material.getSizeX();
        int materialLength = material.getSizeY();

        context.put("mainMaterialWidth", (double) materialWidth);
        context.put("mainMaterialLength", (double) materialLength);

        List<AbstractMachine> machines = one.getProductOperations().stream()
                .map(ProductOperation::getOperation)
                .filter(Objects::nonNull)
                .map(Operation::getAbstractMachine)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        double maxGapTop = 0.0, maxGapBottom = 0.0, maxGapLeft = 0.0, maxGapRight = 0.0;
        
        for (AbstractMachine machine : machines) {
            for (Variable var : machine.getVariables()) {
                Object value = var.getValueAsObject();
                if (value instanceof Number num) {
                    switch (var.getKey()) {
                        case "gap_top"    -> maxGapTop = Math.max(maxGapTop, num.doubleValue());
                        case "gap_bottom" -> maxGapBottom = Math.max(maxGapBottom, num.doubleValue());
                        case "gap_left"   -> maxGapLeft = Math.max(maxGapLeft, num.doubleValue());
                        case "gap_right"  -> maxGapRight = Math.max(maxGapRight, num.doubleValue());
                    }
                }
            }
        }

        double workableWidth = materialWidth - maxGapLeft - maxGapRight;
        double workableLength = materialLength - maxGapTop - maxGapBottom;

        context.put("mainMaterialWorkAreaWidth", workableWidth);
        context.put("mainMaterialWorkAreaLength", workableLength);
    }
}