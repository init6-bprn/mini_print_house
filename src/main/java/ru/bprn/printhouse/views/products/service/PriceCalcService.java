package ru.bprn.printhouse.views.products.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.products.entity.PriceOfMachine;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.products.repository.PriceOfMachineRepository;
import ru.bprn.printhouse.views.products.repository.PriceOfMaterialRepository;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import java.util.Objects;
import java.util.Optional;

import ru.bprn.printhouse.views.templates.entity.Variable;

@Service
@AllArgsConstructor
public class PriceCalcService {
    private final PriceOfMaterialRepository priceOfMaterialRepository;
    private final PriceOfMachineRepository priceOfMachineRepository;


    public void calculate(Templates template){
        Map<String, Object> globalContext = addContext(template);
        calculateProductType(template.getProductTypes(), globalContext);
    }

    public void calculateProductType(Set<AbstractProductType> productTypes, Map<String, Object> globalContext){
        
        for (AbstractProductType productType : productTypes) {
            // У каждого продукта - свой контекст, заполняем динамическими переменными
            Map<String, Object> productTypeContext = new HashMap<>();
            productTypeContext.putAll(globalContext);
            productTypeContext.putAll(addContext(productType));
            switch (productType) {
                case OneSheetDigitalPrintingProductType one -> {
                    // Первоначальный расчет переменных
                    setupContext(one);
                    // С применением формулы
                    getVariable(one, "setupFormula")
                            .map(Variable::getValue)
                            .ifPresent(formula -> calculateOperation(productTypeContext, formula));
                }
                case null, default -> {}
            }
            for (ProductOperation operation : productType.getProductOperations()) {
                // У каждой операции - свой контекст)
                Map<String, Object> operationContext = new HashMap<>();
                operationContext.putAll(productTypeContext);
                operationContext.putAll(addContext(operation));

                // 3.2 Выполнение формулы брака, взятой напрямую из Operation
                String operationWasteFormula = operation.getOperation().getWasteExpression();
                if (operationWasteFormula != null && !operationWasteFormula.isBlank()) {
                    calculateOperation(operationContext, operationWasteFormula);
                }

                // 3.3 Выполнение формулы приладки, взятой напрямую из Operation
                String setupWasteFormula = operation.getOperation().getSetupExpression();
                if (setupWasteFormula != null && !setupWasteFormula.isBlank()) {
                    calculateOperation(operationContext, setupWasteFormula);
                }

                // Обновляем значения в productTypeContext из operationContext, чтобы они были доступны следующей операции
                productTypeContext.put("finalQuantity", operationContext.get("finalQuantity"));
                productTypeContext.put("maxSetupWasteEquivalent", operationContext.get("maxSetupWasteEquivalent"));
                productTypeContext.put("finalSheets", operationContext.get("finalSheets"));
            }
            // Шаг 4. Финальная корректировка листажа с помощью формулы
            switch (productType) {
                case OneSheetDigitalPrintingProductType one -> getVariable(one, "finalAdjustmentFormula")
                        .map(Variable::getValue)
                        .ifPresent(formula -> calculateOperation(productTypeContext, formula));
                case null, default -> {}
            }

            // Шаг 5.1 Расчет стоимости основного материала
            AbstractMaterials mainMaterial = productType.getDefaultMaterial();
            if (mainMaterial != null) {
                double finalSheets = ((Number) productTypeContext.getOrDefault("finalSheets", 0.0)).doubleValue();
                Optional<PriceOfMaterial> priceOpt = priceOfMaterialRepository
                        .findFirstByMaterialAndEffectiveDateBeforeOrderByEffectiveDateDesc(mainMaterial, LocalDateTime.now());

                if (priceOpt.isPresent()) {
                    double materialCost = finalSheets * priceOpt.get().getPrice();
                    productTypeContext.put("componentPrimeCost", materialCost);
                }
            }

            // Шаг 5.2 Расчет стоимости операций
            for (ProductOperation productOperation : productType.getProductOperations()) {
                if (productOperation.isSwitchOff()) continue; // Пропускаем отключенные

                Operation operationTemplate = productOperation.getOperation();
                double componentPrimeCost = ((Number) productTypeContext.get("componentPrimeCost")).doubleValue();

                // Создаем временный контекст для выполнения формул
                Map<String, Object> operationCostContext = new HashMap<>(productTypeContext);
                operationCostContext.putAll(addContext(operationTemplate.getAbstractMachine()));
                operationCostContext.putAll(addContext(operationTemplate));
                operationCostContext.putAll(addContext(productOperation));

                // Стоимость работы оборудования
                if (operationTemplate.getMachineTimeExpression() != null && !operationTemplate.getMachineTimeExpression().isBlank()) {
                    double machineTime = (double) calculateOperation(operationCostContext, operationTemplate.getMachineTimeExpression());
                    Optional<PriceOfMachine> machinePriceOpt = priceOfMachineRepository.findFirstByMachineAndEffectiveDateBeforeOrderByEffectiveDateDesc(operationTemplate.getAbstractMachine(), LocalDateTime.now());
                    if (machinePriceOpt.isPresent()) {
                        double machineCostPerHour = machinePriceOpt.get().getPrice();
                        componentPrimeCost += (machineTime / 3600) * machineCostPerHour;
                    }
                }

                // Стоимость ручной работы
                if (operationTemplate.getActionTimeExpression() != null && !operationTemplate.getActionTimeExpression().isBlank()) {
                    double actionTime = (double) calculateOperation(operationCostContext, operationTemplate.getActionTimeExpression());
                    double workerRate = ((Number) productTypeContext.getOrDefault("worker_rate", 0.0)).doubleValue();
                    componentPrimeCost += (actionTime / 3600) * workerRate;
                }

                // Стоимость расходного материала операции
                if (operationTemplate.getMaterialAmountExpression() != null && !operationTemplate.getMaterialAmountExpression().isBlank()) {
                    double materialAmount = (double) calculateOperation(operationCostContext, operationTemplate.getMaterialAmountExpression());
                    AbstractMaterials operationMaterial = productOperation.getSelectedMaterial();
                    if (operationMaterial != null && materialAmount > 0) {
                        Optional<PriceOfMaterial> opMaterialPriceOpt = priceOfMaterialRepository.findFirstByMaterialAndEffectiveDateBeforeOrderByEffectiveDateDesc(operationMaterial, LocalDateTime.now());
                        if (opMaterialPriceOpt.isPresent()) {
                            componentPrimeCost += materialAmount * opMaterialPriceOpt.get().getPrice();
                        }
                    }
                }
                productTypeContext.put("componentPrimeCost", componentPrimeCost); // Обновляем себестоимость в основном контексте
            }
        }
    }

    private Object calculateOperation(Map<String, Object> context, String formula){
        GroovyShell shell = new GroovyShell(new Binding(context));
        Object result = shell.evaluate(formula);
        if (result instanceof Number) return ((Number) result).doubleValue();
        return result;
    }

    private Map<String, Object> addContext(Object object) {
        // Используем switch как выражение, которое возвращает результат
        return switch (object) {
            case Templates t -> addVariablesToContext(t.getVariables());
            case AbstractProductType p -> addVariablesToContext(p.getVariables());
            case ProductOperation po -> addVariablesToContext(po.getCustomVariables());
            case AbstractMachine m -> addVariablesToContext(m.getVariables());
            case Operation o -> addVariablesToContext(o.getVariables());
            case null, default -> new HashMap<>();
        };
    }

    private Map<String, Object> addVariablesToContext(List<Variable> variables) {
        Map<String, Object> context = new HashMap<>();
        if (variables == null) {
            return null;
        }
        variables.forEach(v -> context.put(v.getKey(), v.getValueAsObject()));
        return context;
         
    }

    private void setupContext(OneSheetDigitalPrintingProductType one) {
        // --- Шаг 0: Получаем размеры основного материала и записываем их в переменные ---
        PrintSheetsMaterial material = one.getDefaultMaterial();
        if (material == null) {
            System.out.println("ОШИБКА: Для продукта '" + one.getName() + "' не задан основной материал.");
            // TODO: Добавить обработку ошибки, например, прервать расчет для этого компонента
            return;
        }
        int materialWidth = material.getSizeX();
        int materialLength = material.getSizeY();

        // Обновляем динамические переменные в самом объекте продукта
        getVariable(one, "mainMaterialWidth").ifPresent(v -> v.setValue(materialWidth));
        getVariable(one, "mainMaterialLength").ifPresent(v -> v.setValue(materialLength));

        // --- Шаг 1: Собираем все уникальные машины из операций ---
        List<AbstractMachine> machines = one.getProductOperations().stream()
                .map(ProductOperation::getOperation) // Для каждой операции продукта берем ее шаблон (Operation)
                .filter(Objects::nonNull) // Отбрасываем те, у которых нет шаблона
                .map(Operation::getAbstractMachine) // Для каждого шаблона операции берем связанное оборудование
                .filter(Objects::nonNull) // Отбрасываем те, у которых нет оборудования
                .distinct() // Оставляем только уникальные машины, чтобы не проверять одну и ту же несколько раз
                .toList();

        // --- Шаг 2: Инициализируем переменные для хранения результатов ---
        double maxGapTop = 0.0;
        double maxGapBottom = 0.0;
        double maxGapLeft = 0.0;
        double maxGapRight = 0.0;
        
        // Используем Double.MAX_VALUE для поиска минимума
        double minMachineLength = Double.MAX_VALUE;
        double minMachineWidth = Double.MAX_VALUE;

        // --- Шаг 3: Проходим по всем переменным всех машин ОДИН РАЗ ---
        for (AbstractMachine machine : machines) {
            for (Variable var : machine.getVariables()) {
                Object value = var.getValueAsObject();
                if (value instanceof Number num) {
                    // Обновляем максимальные отступы
                    switch (var.getKey()) {
                        case "gap_top" -> maxGapTop = Math.max(maxGapTop, num.doubleValue());
                        case "gap_bottom" -> maxGapBottom = Math.max(maxGapBottom, num.doubleValue());
                        case "gap_left" -> maxGapLeft = Math.max(maxGapLeft, num.doubleValue());
                        case "gap_right" -> maxGapRight = Math.max(maxGapRight, num.doubleValue());
                    }                    
                    // Обновляем минимальные размеры
                    if ("max_length".equals(var.getKey())) minMachineLength = Math.min(minMachineLength, num.doubleValue());
                    if ("max_width".equals(var.getKey())) minMachineWidth = Math.min(minMachineWidth, num.doubleValue());
                }
            }
        }

        // --- Шаг 4: Проверяем совместимость материала с оборудованием ---
        if (materialWidth > minMachineWidth || materialLength > minMachineLength) {
            // TODO: Добавить логику обработки несовместимости
            return;
        }

        // --- Шаг 5: Вычисляем и сохраняем рабочую область ---
        double workableWidth = materialWidth - maxGapLeft - maxGapRight;
        double workableLength = materialLength - maxGapTop - maxGapBottom;

        getVariable(one, "mainMaterialWorkAreaWidth").ifPresent(v -> v.setValue(workableWidth));
        getVariable(one, "mainMaterialWorkAreaLength").ifPresent(v -> v.setValue(workableLength));
    }

    private Optional<Variable> getVariable(AbstractProductType productType, String key) {
        return productType.getVariables().stream().filter(v -> key.equals(v.getKey())).findFirst();
    }
}
