# План рефакторинга сервиса расчета (PriceCalculationService) 

Этот документ описывает проблему текущей архитектуры `PriceCalculationService` и предлагает пошаговый план по ее улучшению для поддержки различных типов продуктов.

## 1. Проблема: Жесткая связь и нарушение принципа открытости/закрытости

Текущая реализация `PriceCalculationService` содержит код, который напрямую работает с конкретным классом `OneSheetDigitalPrintingProductType`.

**Пример "кода с запахом" в `calculateComponentPrimeCost`:**
```java
if (productType instanceof OneSheetDigitalPrintingProductType one) {
    setupContext(one, context);
    // ... другая логика, специфичная для листовой печати
}
```

**Последствия:**
*   **Низкая расширяемость:** Чтобы добавить новый тип продукта (например, `WideFormatProductType` для широкоформатной печати или `BookBlockProductType` для книжных блоков), придется изменять сам `PriceCalculationService`, добавляя новые блоки `else if`.
*   **Нарушение принципа открытости/закрытости (Open/Closed Principle):** Класс должен быть открыт для расширения, но закрыт для модификации. Текущий сервис требует модификации при каждом расширении.
*   **Снижение читаемости:** Сервис со временем превратится в большой `switch` или цепочку `if-else`, что усложнит его понимание и поддержку.

## 2. Решение: Паттерн проектирования "Стратегия"

Идеальным решением является применение паттерна **Стратегия**. Мы вынесем уникальные алгоритмы расчета для каждого типа продукта в отдельные классы-стратегии.

*   `PriceCalculationService` станет **оркестратором**. Он не будет знать о деталях расчета, а будет лишь выбирать нужную стратегию и делегировать ей работу.
*   Каждая **стратегия** будет инкапсулировать логику, специфичную для одного типа продукта (листовая печать, рулонная печать и т.д.).

## 3. ToDo: Пошаговый план реализации

### Шаг 1: Создать интерфейс стратегии

Нужно определить общий контракт для всех алгоритмов расчета.

**Файл:** `src/.../service/strategies/ProductTypeCalculationStrategy.java`
```java
public interface ProductTypeCalculationStrategy {
    /**
     * Сообщает, поддерживает ли эта стратегия данный класс продукта.
     */
    boolean supports(Class<? extends AbstractProductType> clazz);

    /**
     * Подготавливает контекст, добавляя специфичные для продукта переменные
     * (например, размеры рабочей области листа).
     */
    void prepareContext(AbstractProductType productType, Map<String, Object> context);

    /**
     * Выполняет основную формулу расчета "физики" продукта
     * (например, раскладку на листе или расчет длины рулона).
     */
    void executeLayoutFormula(AbstractProductType productType, Map<String, Object> context, StringBuilder reportBuilder);

    /**
     * Рассчитывает стоимость и вес основного материала.
     * Возвращает стоимость материала в копейках.
     */
    long calculateMainMaterialCostAndWeight(AbstractProductType productType, Map<String, Object> globalContext, Map<String, Object> localContext, StringBuilder reportBuilder);
}
```

### Шаг 2: Создать менеджер стратегий

Это будет сервис, который автоматически находит все реализации стратегий и предоставляет нужную по запросу.

**Файл:** `src/.../service/strategies/CalculationStrategyManager.java`
```java
@Service
public class CalculationStrategyManager {
    private final List<ProductTypeCalculationStrategy> strategies;

    // Spring инжектирует сюда все бины, реализующие интерфейс
    public CalculationStrategyManager(List<ProductTypeCalculationStrategy> strategies) {
        this.strategies = strategies;
    }

    public Optional<ProductTypeCalculationStrategy> getStrategy(AbstractProductType productType) {
        return strategies.stream()
                .filter(s -> s.supports(productType.getClass()))
                .findFirst();
    }
}
```

### Шаг 3: Реализовать первую конкретную стратегию

Нужно перенести существующую логику для листовой печати в отдельный класс.

**Файл:** `src/.../service/strategies/OneSheetDigitalCalculationStrategy.java`
*   **Задача:** Реализовать интерфейс `ProductTypeCalculationStrategy`.
*   **Метод `supports`:** Должен возвращать `true` для `OneSheetDigitalPrintingProductType.class`.
*   **Перенести логику:**
    *   Весь код из `PriceCalculationService.setupContext()` переезжает в метод `prepareContext()` этой стратегии.
    *   Логика вызова `setupFormula` переезжает в `executeLayoutFormula()`.
    *   Логика расчета стоимости и веса основного листового материала переезжает в `calculateMainMaterialCostAndWeight()`.

### Шаг 4: Рефакторинг `PriceCalculationService`

После создания стратегий нужно "очистить" главный сервис.

*   **Задача:** Удалить из `PriceCalculationService` все блоки `if (productType instanceof ...)` и приватный метод `setupContext`.
*   **Интеграция:**
    1.  Инжектировать `CalculationStrategyManager` в `PriceCalculationService`.
    2.  В методе `calculateComponentPrimeCost` вызывать `strategyManager.getStrategy(productType)`.
    3.  Если стратегия найдена, последовательно вызывать ее методы: `prepareContext`, `executeLayoutFormula`, `calculateMainMaterialCostAndWeight`.
    4.  Если стратегия не найдена, логировать ошибку или выбрасывать исключение.

### Шаг 5: (Будущее) Добавление нового типа продукта

Когда понадобится добавить, например, широкоформатную печать, шаги будут очень простыми:
1.  Создать новый класс `WideFormatProductType`.
2.  Создать новый класс `WideFormatCalculationStrategy`, реализующий `ProductTypeCalculationStrategy`.
3.  Описать в нем свою логику (расчет по квадратным/погонным метрам, работа с рулонными материалами).
4.  **Все!** `PriceCalculationService` изменять не придется. Он автоматически подхватит новую стратегию.

## 4. Ожидаемые преимущества

*   **Гибкость:** Система готова к любым новым типам продуктов.
*   **Поддерживаемость:** Логика каждого продукта изолирована в своем классе.
*   **Надежность:** Уменьшается риск сломать существующий расчет при добавлении нового.
*   **Следование SOLID:** Код соответствует принципу открытости/закрытости.