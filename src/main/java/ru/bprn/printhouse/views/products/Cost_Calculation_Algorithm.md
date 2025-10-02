# Алгоритм расчета стоимости изделия

Этот документ описывает пошаговый процесс расчета итоговой стоимости изделия, созданного на основе сущности `Templates`. Алгоритм служит основой для проектирования новой сущности `Formula` и рефакторинга сервисов расчета.

## Входные данные для расчета

1.  `template`: Экземпляр сущности `Templates`, на основе которого производится расчет.
2.  `userInputs`: `Map<String, Object>`, содержащая значения, введенные пользователем. Как минимум, это `quantity` (тираж).

## Сущность Formula (Предварительное описание)

*   **Назначение:** Хранилище для набора формул и вычисляемых значений. Экземпляр `Formula` создается для каждого расчета и "проживает" только на время этого расчета.
*   **Структура:**
    *   `values`: `Map<String, Object>` — классическая карта для хранения как входных данных, так и результатов промежуточных вычислений. Ключ — имя переменной (например, `quantity`, `productWidth`, `totalCost`), значение — ее вычисленное значение.
    *   `executionOrder`: (Возможно) `List<String>` или другой механизм, определяющий порядок вычисления зависимых формул.

---

## Пошаговый алгоритм расчета

*Здесь мы будем шаг за шагом описывать логику...*

### Шаг 1. Инициализация контекста расчета

1.  Создается новый, пустой экземпляр `Formula`.
2.  **Сбор глобальных переменных:**
    *   В `Formula.values` добавляются все переменные из `template.getVariables()`.
    *   В `Formula.values` добавляются/перезаписываются все значения из `userInputs`. Это позволяет пользователю переопределить значения по умолчанию (например, `quantity`).
3.  **Инициализация аккумуляторов:**
    *   В `Formula.values` добавляется переменная `totalCost = 0`. Она будет накапливать итоговую стоимость всех компонентов.
    *   В `Formula.values` добавляется переменная `totalWeight = 0`. Она будет накапливать общий вес всего заказа.

### Шаг 2. Расчет компонентов (`AbstractProductType`)

**В цикле `for each product in template.getProductTypes()`:**

1.  **Создание локального контекста компонента:**
    *   В `Formula.values` добавляются все переменные из текущего `product.getVariables()`. Эти переменные (например, `productWidth`, `productLength`) имеют приоритет над глобальными.
    *   Среди этих переменных уже присутствует `componentPrimeCost`, инициализированная нулем.
2.  **Расчет на основе основного материала:**
    *   Извлекается основной материал: `mainMaterial = product.getDefaultMaterial()`. В `Formula.values` добавляются его параметры (`sheetWidth`, `sheetLength`, `materialDensity` и т.д.).
    *   **Выполняется "Формула Материала" (`materialFormula`)**:
        *   Эта Groovy-формула, хранящаяся в `AbstractProductType`, отвечает за расчеты, связанные с материалом.
        *   **Пример логики внутри формулы:**
            *   `productWeight = productWidth * productLength * materialDensity * quantity`
            *   `totalWeight += productWeight` // Добавляем вес компонента к общему весу заказа

3.  **Определение рабочей области печатного листа:**
    *   Собирается список всего оборудования (`List<AbstractMachine>`), задействованного в операциях (`product.getProductOperations()`) данного компонента.
    *   Проводится проверка, что размеры `mainMaterial` совместимы со всеми машинами.
    *   Находятся **максимальные** непечатные поля (`gap_top`, `gap_bottom`, `gap_left`, `gap_right`) среди всех машин.
    *   В `Formula.values` вычисляются и добавляются переменные:
        *   `workableSheetWidth = sheetWidth - max_gap_left - max_gap_right`
        *   `workableSheetLength = sheetLength - max_gap_top - max_gap_bottom`

4.  **Выполнение "Формулы Настройки Переменных" (`setupVariablesFormula`):**
    *   Выполняется вторая Groovy-формула из `AbstractProductType`. Она содержит всю сложную логику, специфичную для данного типа продукта.
    *   **Пример логики внутри формулы (для `OneSheetDigitalPrintingProductType`):**
        *   Рассчитывается, сколько изделий помещается на `workableSheetWidth` x `workableSheetLength`.
        *   В `Formula.values` устанавливаются/обновляются переменные:
            *   `quantityProductsOnSheet`
            *   `baseSheets = ceil(quantity / quantityProductsOnSheet)` (базовый листаж без брака)
            *   `columnsOnSheet`, `rowsOnSheet`

### Шаг 3. Расчет брака и приладки (цикл по операциям)

1.  **Инициализация переменных для брака:**
    *   В `Formula.values` добавляются переменные, которые будут изменяться формулами:
        *   `finalSheets = baseSheets` (итоговый листаж, который будет увеличиваться)
        *   `finalQuantity = quantity` (фактический тираж, который будет увеличиваться. `quantity` здесь - это неизменный расчетный тираж)
        *   `maxSetupWasteEquivalent = 0.0` (аккумулятор для максимальной приладки в эквиваленте изделий)

**В цикле `for each operation in product.getProductOperations()`:**

1.  **Сбор контекста операции:**
    *   В `Formula.values` добавляются переменные из `operation.getOperation().getAbstractMachine()`.
    *   В `Formula.values` добавляются переменные из `operation.getOperation()`.
    *   В `Formula.values` добавляются/перезаписываются переменные из `operation.getCustomVariables()`.

2.  **Выполнение формулы брака (`operationWasteFormula`):**
    *   Выполняется Groovy-формула из `operation.getCustomVariables()`.
    *   Эта формула **напрямую изменяет** переменные в контексте `Formula.values`. Брак по всем операциям **суммируется**.
    *   **Пример логики внутри формулы:** `finalSheets += 5` или `finalQuantity += quantity * 0.01`.

3.  **Выполнение формулы приладки (`setupWasteFormula`):**
    *   Выполняется Groovy-формула из `operation.getCustomVariables()`.
    *   Эта формула вычисляет приладку для текущей операции и обновляет `maxSetupWasteEquivalent`, если ее требование больше, чем у предыдущих операций. Приладка **не суммируется**, а ищется **максимальная**.
    *   **Пример логики внутри формулы:** `maxSetupWasteEquivalent = Math.max(maxSetupWasteEquivalent, 20)`.

### Шаг 4. Финальная корректировка и расчет стоимости

*Этот шаг выполняется после завершения цикла по операциям для каждого компонента (`AbstractProductType`).*

1.  **Учет приладки:**
    *   Фактический тираж увеличивается на максимальную требуемую приладку:
        *   `finalQuantity += maxSetupWasteEquivalent`

2.  **Финальная проверка и корректировка листажа:**
    *   Проверяется, достаточно ли рассчитанных листов для производства фактического тиража.
    *   **ЕСЛИ** `finalSheets * quantityProductsOnSheet < finalQuantity`,
    *   **ТО** листаж пересчитывается: `finalSheets = ceil(finalQuantity / quantityProductsOnSheet)`.

### Шаг 5. Расчет себестоимости компонента (`primeCost`)

*Этот шаг выполняется после финальной корректировки листажа для каждого компонента (`AbstractProductType`).*

1.  **Расчет стоимости основного материала:**
    *   Извлекается актуальная цена на основной материал (`mainMaterial`) из справочника `PriceOfMaterial`.
    *   `mainMaterialCost = finalSheets * price_of_main_material`.
    *   `componentPrimeCost += mainMaterialCost`. (Переменная `componentPrimeCost` уже существует в контексте).

**В новом цикле `for each operation in product.getProductOperations()`:**

1.  **Выполнение технических формул:**
    *   Выполняется `machineTimeFormula` -> результат `machineTime` (в секундах).
    *   Выполняется `actionFormula` -> результат `actionTime` (в секундах).
    *   Выполняется `materialFormula` -> результат `operationMaterialAmount` (в единицах материала).

2.  **Сбор актуальных цен для операции:**
    *   Извлекается `machineCostPerHour` для `operation.getOperation().getAbstractMachine()` из справочника `PriceOfMachine`.
    *   Извлекается `workerRate` (стоимость нормо-часа работника) из глобального контекста `Formula.values`.
    *   Извлекается `operationMaterialPrice` для `operation.getSelectedMaterial()` из справочника `PriceOfMaterial`.

3.  **Расчет и суммирование стоимости операции:**
    *   `operationMachineCost = (machineTime / 3600) * machineCostPerHour`.
    *   `operationWorkerCost = (actionTime / 3600) * workerRate`.
    *   `operationMaterialCost = operationMaterialAmount * operationMaterialPrice`.
    *   `componentPrimeCost += operationMachineCost + operationWorkerCost + operationMaterialCost`.

4.  **Аккумулирование себестоимости компонента:**
    *   Себестоимость текущего компонента добавляется к общей себестоимости заказа:
    *   `totalCost += componentPrimeCost`.

**--- Конец цикла по `product` ---**

### Шаг 6. Расчет итоговой отпускной цены

*Этот шаг выполняется один раз, после завершения цикла по всем компонентам (`AbstractProductType`).*

1.  **Применение глобальных наценок:**
    *   Из `Formula.values` извлекаются глобальные переменные, заданные в `Templates` (например, `margin`, `tax`).
    *   `sellingPrice = totalCost * (1 + margin/100) * (1 + tax/100)`.

2.  **Округление и расчет финальной стоимости:**
    *   `pricePerOne = sellingPrice / quantity` (делится на **исходный** тираж).
    *   Выполняется формула округления `roundFormula` из `Templates` над `pricePerOne`. Результат -> `roundedPricePerOne`.
    *   `finalTotalCost = roundedPricePerOne * quantity`.

**Результат всего алгоритма — `finalTotalCost`.**

---

## Заметки и TODO для реализации

*В этом разделе будут фиксироваться все новые переменные и формулы, которые нужно будет добавить в сущности на основе этого алгоритма.*

### Новые переменные:

*   `totalCost` (контекст `Formula`): Сумматор итоговой стоимости.
*   `totalWeight` (контекст `Formula`): Сумматор веса всего изделия.
*   `sheetWidth`, `sheetLength`, `materialDensity` (контекст `Formula`): Параметры основного материала.
*   `workableSheetWidth`, `workableSheetLength` (контекст `Formula`): Размеры рабочей области печатного листа.
*   `quantityProductsOnSheet` (контекст `Formula`): Количество изделий на одном печатном листе.
*   `baseSheets` (контекст `Formula`): Базовое количество листов без учета брака.
*   `columnsOnSheet`, `rowsOnSheet` (контекст `Formula`): Количество колонок и рядов при раскладке.
*   `finalSheets` (контекст `Formula`): Итоговый листаж с учетом брака и приладки.
*   `finalQuantity` (контекст `Formula`): Фактический тираж с учетом брака и приладки.
*   `maxSetupWasteEquivalent` (контекст `Formula`): Максимальная приладка в эквиваленте изделий.
*   `componentPrimeCost` (уровень `AbstractProductType`): Себестоимость одного компонента.

### Новые формулы:

*   **`materialFormula`** (уровень `AbstractProductType`): Формула для расчетов, связанных с основным материалом (например, вес).
*   **`setupVariablesFormula`** (уровень `AbstractProductType`): Формула для сложной логики настройки переменных, специфичной для продукта (например, раскладка на листе).
*   **`operationWasteFormula`** (уровень `ProductOperation`): Формула для расчета технологического брака. Напрямую изменяет `finalSheets` или `finalQuantity`.
*   **`setupWasteFormula`** (уровень `ProductOperation`): Формула для расчета приладки. Обновляет `maxSetupWasteEquivalent`.
*   **`machineTimeFormula`** (уровень `ProductOperation`): Формула для расчета времени работы оборудования.
*   **`actionFormula`** (уровень `ProductOperation`): Формула для расчета времени ручной работы сотрудника.
*   **`materialFormula`** (уровень `ProductOperation`): Формула для расчета количества расходного материала операции.
*   **`roundFormula`** (уровень `Templates`): Формула для финального округления цены за единицу.

---

## Пример формулы `setupVariablesFormula` для раскладки изделий

*Этот код на Groovy предназначен для выполнения на Шаге 2.4. Он рассчитывает оптимальное количество изделий на листе и базовый листаж.*

```groovy
// --- Шаг 1: Рассчитываем полный размер изделия с учетом вылетов ---
// Вылеты добавляются с двух сторон, поэтому умножаем на 2.
// Обновляем переменные размера изделия до обрезки
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

return
```

## Прагматичный план доработок (v2.0)

*Этот план сфокусирован на минимально необходимых изменениях, чтобы заставить систему считать, основываясь на существующей логике и UI.*

### 1. Внедрение сущности `Formula`

*   **Проблема:** Сейчас формулы хранятся как обычные `Variable` в `List<Variable>`, что смешивает данные и логику. Редакторы (`OperationEditor`, `MapEditorView`) содержат "костыли" для их фильтрации.
*   **Решение:**
    1.  Создать **новый класс `Formula`** (не JPA-сущность, а, например, `@Embeddable` или класс для JSON-сериализации).
    2.  **Структура `Formula`:**
        *   `key` (String): Имя переменной, которую вычисляет формула (например, `machineTime`).
        *   `expression` (String): Groovy-выражение.
        *   `phase` (enum `CalculationPhase`): Фаза расчета.
        *   `priority` (int): Дополнительный приоритет для сортировки внутри одной фазы.
    3.  **Обновить сущности:** В `Templates`, `AbstractProductType`, `Operation`, `ProductOperation` заменить хранение формул в `List<Variable>` на новое поле `private List<Formula> formulas = new ArrayList<>();` (аннотированное `@JdbcTypeCode(SqlTypes.JSON)`).

### 2. Рефакторинг `PriceCalculationService`

*   **Проблема:** Текущий сервис `PriceCalculationService` имеет жестко закодированную логику и работает со старой моделью `Variable`.
*   **Решение:**
    *   Переписать `PriceCalculationService` для работы с новой моделью `List<Formula>`.
    *   Сервис должен реализовывать **иерархический алгоритм**, описанный выше: создавать контексты для каждого уровня (`global`, `component`, `operation`), собирать и выполнять формулы в правильных фазах, передавая результаты между контекстами.

### 3. Рефакторинг UI (Редакторы)

*   **Проблема:** Редакторы завязаны на `List<Variable>` для хранения формул.
*   **Решение:**
    1.  **`ProductOperationEditor` и `MapEditorView`:** Убрать из них фильтр `isFormulaVariable()`. Они больше не должны знать о формулах.
    2.  **`OperationEditor` и `OneSheetDigitalPrintingProductTypeEditor`:**
        *   **Сохранить существующие 5 `EditableTextArea`** для редактирования формул.
        *   Изменить логику их привязки (`binder`). Вместо поиска `Variable` по ключу, они будут искать `Formula` в `List<Formula>` по ключу (например, "machineTime").
        *   Если формула не найдена, она будет создаваться "на лету" и добавляться в список.
        *   **Никаких сложных UI-компонентов сейчас не создаем.** Цель — сохранить привычный интерфейс, но с новой моделью данных "под капотом".

---

## Необходимые файлы для реализации `PriceCalculationService`

*Чтобы реализовать `PriceCalculationService` в соответствии с этим алгоритмом, необходим доступ к следующим файлам для понимания полной структуры данных.*

### 1. Иерархия шаблона:
*   `Templates.java`
*   `AbstractProductType.java`
*   `OneSheetDigitalPrintingProductType.java` (финальная версия с полями для формулы)
*   `ProductOperation.java`

### 2. Сервис для выполнения скриптов:
*   `GroovyShellService.java` (или аналог)

### 3. Сервисы для получения цен:
*   `PriceOfMaterialService.java` (или репозиторий)
*   `PriceOfMachineService.java` (или репозиторий)

### 4. Сервис расчета:
*   `PriceCalculationService.java` (если существует)
