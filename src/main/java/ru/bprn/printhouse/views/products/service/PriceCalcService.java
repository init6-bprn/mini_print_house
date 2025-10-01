package ru.bprn.printhouse.views.products.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import java.util.Objects;
import java.util.Optional;

import ru.bprn.printhouse.views.templates.entity.Variable;

public class PriceCalcService {
private Templates template;
private Map<String, Object> globalContext;
private Map<String, Object> context;


    public PriceCalcService(Templates template){
        this.template = template;
        this.globalContext = addContext(template);
        calculateProductType(template.getProductTypes());
    }

    public void calculateProductType(Set<AbstractProductType> productTypes){
        Map<String, Object> productTypeContext = new HashMap<>();
        for (AbstractProductType productType : productTypes) {
            switch (productType) {
                case OneSheetDigitalPrintingProductType one -> setUpContext(one);
           
                case null, default -> {}
            }

            productTypeContext.putAll(globalContext);
            productTypeContext.putAll(addContext(productType));
            
            calculateOperation(productTypeContext);
           
        }
    }

    private void calculateOperation(Map<String, Object> context){
        GroovyShell shell = new GroovyShell(new Binding(context));

    }

    private Map<String, Object> addContext(Object object) {
        // Используем switch как выражение, которое возвращает результат
        return switch (object) {
            case Templates t -> addVariablesToContext(t.getVariables());
            case AbstractProductType p -> addVariablesToContext(p.getVariables());
            case ProductOperation po -> addVariablesToContext(po.getCustomVariables());
            case AbstractMachine m -> addVariablesToContext(m.getVariables());
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

    private void setUpContext(OneSheetDigitalPrintingProductType one) {
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

        System.out.println("Материал '" + material.getName() + "' имеет размеры: " + materialWidth + "x" + materialLength);

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

        System.out.println("Ограничения оборудования: мин. размеры листа (ширина x длина): " + minMachineWidth + " x " + minMachineLength);
        System.out.println("Максимальные непечатные поля (T/B/L/R): " + maxGapTop + "/" + maxGapBottom + "/" + maxGapLeft + "/" + maxGapRight);


        // --- Шаг 4: Проверяем совместимость материала с оборудованием ---
        if (materialWidth > minMachineWidth || materialLength > minMachineLength) {
            System.out.println("ПРЕДУПРЕЖДЕНИЕ: Размер материала (" + materialWidth + "x" + materialLength
                    + ") больше, чем минимально поддерживаемый машинами (" + minMachineWidth + "x" + minMachineLength + ").");
            // TODO: Добавить логику обработки несовместимости
        }

        // --- Шаг 5: Вычисляем и сохраняем рабочую область ---
        double workableWidth = materialWidth - maxGapLeft - maxGapRight;
        double workableLength = materialLength - maxGapTop - maxGapBottom;

        getVariable(one, "mainMaterialWorkAreaWidth").ifPresent(v -> v.setValue(workableWidth));
        getVariable(one, "mainMaterialWorkAreaLength").ifPresent(v -> v.setValue(workableLength));

        System.out.println("Расчетная рабочая область листа: " + workableWidth + "x" + workableLength);
    }

    private Optional<Variable> getVariable(AbstractProductType productType, String key) {
        return productType.getVariables().stream().filter(v -> key.equals(v.getKey())).findFirst();
    }
}
