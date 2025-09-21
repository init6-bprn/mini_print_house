# План разработки модуля "Оформление заказов"

Этот документ является "рыбой" для проектирования и разработки нового модуля.

## Концепция

Модуль позволит клиентам (или менеджерам) на основе существующих шаблонов (`Templates`) конфигурировать конкретный заказ (выбирать тираж, материалы, изменять доступные параметры), видеть итоговую стоимость и размещать заказ в системе.

---

## Этап 1: Проектирование модели данных

Новые сущности для хранения информации о заказах. Важно "отвязать" созданный заказ от шаблона, скопировав все данные на момент создания. Это гарантирует, что редактирование шаблона в будущем не повлияет на уже оформленные заказы.

1.  **`CustomerOrder` (Заказ клиента)**
    *   `id`: `UUID`
    *   `orderNumber`: `String` — Уникальный номер заказа (напр., "2024-10-01").
    *   `customer`: `User` — Ссылка на зарегистрированного пользователя-заказчика (может быть `null` для гостевых заказов).
    *   `customerName`: `String` — Имя клиента (для гостей или для уточнения).
    *   `customerContacts`: `String` — Контактные данные (email, телефон).
    *   `isLegalEntity`: `boolean` — Флаг, указывающий, является ли клиент юрлицом.
    *   `companyDetails`: `String` (JSON/TEXT) — Реквизиты компании, если это юрлицо.
    *   `status`: `OrderStatus` (enum) — Статус заказа: `DRAFT` (Черновик), `PENDING_PAYMENT` (Ожидает оплаты), `IN_PROGRESS` (В работе), `COMPLETED` (Выполнен), `CANCELLED` (Отменен).
    *   `totalPrice`: `int` — Итоговая стоимость всего заказа.
    *   `items`: `List<OrderItem>` — Список позиций в заказе (связь One-to-Many).
    *   `createdAt`, `updatedAt`: `LocalDateTime`.

2.  **`OrderItem` (Позиция в заказе)**
    *   `id`: `UUID`
    *   `customerOrder`: `CustomerOrder` — Ссылка на родительский заказ.
    *   `sourceProductTypeId`: `UUID` — ID шаблона `Templates`, на основе которого создана позиция.
    *   `name`: `String` — Наименование позиции (напр., "Визитка 90х50, мелованная бумага, 1000 шт").
    *   `quantity`: `Integer` — Тираж.
    *   `price`: `int` — Итоговая цена за эту позицию в минимальных денежных единицах(за весь тираж).
    *   `priceForOne` : `int` — Цена за одну единицу продукта`.
    *   `configuration`: `String` (JSON) — **Ключевое поле**. Сериализованный в JSON объект, который "замораживает" всю конфигурацию продукта на момент заказа. Он будет хранить копию всех операций и их переменных.

3.  **`OrderItemConfiguration` (Конфигурация позиции заказа)** - это не сущность, а структура для хранения в JSON-поле `OrderItem.configuration`.
    *   `productName`: `String`
    *   `sizeX`, `sizeY`, `bleed`: `Double`
    *   `selectedMainMaterialId`: `UUID` — ID выбранного основного материала (например, бумаги).
    *   `abstractProductType`: `List<ConfiguredAbstractProductType>`
    *   `operations`: `List<ConfiguredOperation>`

    **`ConfiguredAbstractProductType` (Структура для сконфигурированного типа продукта)**
    *   `sourceAbstractProductTypeId`: `UUID` — ID исходного `AbstractProductType` из шаблона.
    *   `name`: `String` — Имя компонента (например, "Обложка" или "Внутренний блок").
    *   `sizeX`, `sizeY`, `bleed`: `Double` — Размеры и вылеты, если применимо.
    *   `selectedMainMaterialId`: `UUID` — ID выбранного основного материала для этого компонента.
    *   `variables`: `List<ConfiguredVariable>` — Список измененных пользователем переменных, относящихся к этому компоненту.
    *   `operations`: `List<ConfiguredOperation>` — Список сконфигурированных операций для этого компонента.

4.  **`ConfiguredOperation` (Структура для сконфигурированной операции)**
    *   `productOperationId`: `UUID` — ID исходной `ProductOperation` из шаблона.
    *   `name`: `String`
    *   `isSwitchedOff`: `boolean`
    *   `selectedMaterialId`: `UUID` — ID выбранного материала для данной операции (например, краски).
    *   `cost`: `BigDecimal` (рассчитанная стоимость этой операции)
    *   `variables`: `List<ConfiguredVariable>` — Список **измененных пользователем** переменных.

5.  **`ConfiguredVariable` (Структура для сконфигурированной переменной)**
    *   `key`, `value`, `description`: `String`

---

## Этап 2: Пользовательский интерфейс (UI)

Предлагается реализовать классический путь пользователя: `Каталог -> Конфигуратор -> Корзина -> Оформление`.

1.  **`ProductCatalogView` (Каталог продукции)**
    *   **Что делает**: Заменяет старый `ProductsView`. Отображает все доступные для заказа продукты (`AbstractProductType`) в виде карточек.
    *   **Группировка**: Карточки можно сгруппировать по родительским `Templates` (например, секции "Визитки", "Листовки").
    *   **Поиск и фильтрация**: Сверху страницы — поле для полнотекстового поиска. Сбоку — панель с фильтрами (фасетами) по категориям, размерам, типам материалов.
    *   **Элементы (Карточка продукта)**:
        *   Изображение, название, краткое описание.
        *   **Блок быстрого заказа**:
        *   **Отображение цены**: Показывается расчетная цена за единицу и за тираж. Цена должна динамически пересчитываться при изменении тиража в блоке быстрого заказа.
            *   Поле для ввода тиража (значение по умолчанию из `Templates.quantity`).
            *   Кнопка "В корзину" для добавления товара с конфигурацией по умолчанию.
        *   Кнопка "Настроить", которая открывает `ProductConfiguratorDialog`.

2.  **`ProductConfiguratorDialog` (Диалог конфигурации продукта)**
    *   **Что делает**: Модальное окно, которое открывается по клику на "Настроить и заказать".
    *   **Входные данные**: Принимает ID `AbstractProductType`.
    *   **Поля для пользователя**:
        *   Поле для ввода **тиража** (`quantity`).
        *   Выпадающий список для выбора **основного материала** (бумаги) из тех, что доступны в `AbstractProductType`.
        *   Для каждой настраиваемой операции (`ProductOperation`):
            *   Чекбокс "Включить/Отключить", если `switchOffAllowed = true`.
            *   Выпадающий список для выбора **материала операции** (например, цветности печати), если у операции есть доступные материалы.
            *   Поля для редактирования **переменных**, которые помечены как видимые (`show = true`).
    *   **Интерактивность**: Внизу диалога отображается цена, которая пересчитывается в реальном времени при изменении любого параметра.
    *   **Действие**: Кнопка "Добавить в корзину".

3.  **`ShoppingCartView` (Корзина)**
    *   **Что делает**: Стандартная корзина, доступная, например, по иконке в `MainLayout`.
    *   **Функционал**: Показывает список добавленных `OrderItem`, позволяет менять их количество или удалять. Отображает общую стоимость заказа.
    *   **Действие**: Кнопка "Оформить заказ", которая ведет на `CheckoutView`.

4.  **`CheckoutView` и `OrderHistoryView`**
    *   На последующих этапах: страницы для ввода данных клиента, выбора доставки/оплаты и просмотра истории своих заказов.

---

## Этап 3: Сервисный слой и бизнес-логика

1.  **`PriceCalculationService` (Сервис расчета цены)**
    *   **Основной метод**: `calculatePrice(AbstractProductType product, int quantity, Map<String, Object> userVariables)`
    *   **Общая логика**: Сервис итерирует по компонентам (`AbstractProductType`) в шаблоне. Для каждого компонента выполняется цепочка расчетов, состоящая из специфической логики компонента и последующего расчета его операций.
    *   **Детальная цепочка расчета на примере `OneSheetDigitalPrintingProductType`**:

        **Общий принцип: Разделение технологического и экономического расчета**

        В основе логики лежит разделение двух аспектов:
        1.  **Технологическая калькуляция**: Расчет физических величин (количество листов, время работы оборудования, время работы сотрудника, количество расходных материалов). Эта часть стабильна и редко меняется.
        2.  **Экономический расчет**: Применение цен к физическим величинам. Цены на материалы, амортизацию и работу сотрудников часто меняются и хранятся в отдельных справочниках (например, `PriceOfAbstractMaterials`, `PriceOfAbstractMachine`).

        Такой подход позволяет при повторном заказе использовать ту же технологическую цепочку, но подставлять актуальные на данный момент цены.


        **Часть 1: Подготовка и расчет листажа (Количество основного материала)**

        1.  **Сбор глобального контекста**:
            *   `quantity`: Тираж из конфигурации.
            *   `productWidth`, `productLength`, `bleed`: Размеры изделия и вылеты из `OneSheetDigitalPrintingProductType`.
            *   `selectedMainMaterial`: Основной материал (бумага), выбранный пользователем.

        2.  **Проверка совместимости оборудования**:
            *   Собираются все `ProductOperation` для данного компонента.
            *   Для каждой операции определяется используемое оборудование (`AbstractMachine`).
            *   Находятся максимальные поддерживаемые машинами размеры материала (`max_machine_width`, `max_machine_length`).
            *   Проверяется, что `selectedMainMaterial.width <= max_machine_width` и `selectedMainMaterial.length <= max_machine_length`. Если нет, расчет невозможен.

        3.  **Расчет рабочей области печатного листа**:
            *   Берется размер листа `selectedMainMaterial`.
            *   Из всех задействованных машин (`AbstractMachine`) собираются переменные непечатных полей (`gap_top`, `gap_bottom`, `gap_left`, `gap_right`).
            *   Находятся **максимальные** значения для каждого из четырех отступов.
            *   `workableAreaWidth = selectedMainMaterial.width - max_gap_left - max_gap_right`
            *   `workableAreaLength = selectedMainMaterial.length - max_gap_up - max_gap_down`

        4.  **Расчет раскладки (если `multiplication = true`)**:
            *   `productWidthBeforeCut = productWidth + bleed * 2`
            *   `productLengthBeforeCut = productLength + bleed * 2`
            *   Вычисляется, сколько изделий (`productWidthBeforeCut`, `productLengthBeforeCut`) можно разместить на `workableArea`. Это `quantityProductsOnMainMaterial`.
            *   Вспомогательные переменные `rows` и `columns` также заполняются.
            * (если `multiplication = false`) `quantityProductsOnMainMaterial = 1` `rows` и `columns` равны 1

        5.  **Расчет начального листажа**:
            *   `requiredSheets = ceil(quantity / quantityProductsOnMainMaterial)`

        6.  **Расчет брака и приладки**:
            *   Создаются аккумуляторы: `totalOperationWasteSheets`, `totalOperationWasteQuantity`, `maxSetupWasteSheets`, `maxSetupWasteQuantity`.
            *   Для каждой `ProductOperation` вычисляются формулы `customOperationWasteFormula` и `customSetupWasteFormula`. Эти формулы могут возвращать значение для увеличения листажа или тиража.
            *   Брак **суммируется**: `totalOperationWasteSheets += result_from_formula`, `totalOperationWasteQuantity += result_from_formula`.
            *   Приладка **выбирается максимальная**: `maxSetupWasteSheets = max(maxSetupWasteSheets, result_from_formula)`, `maxSetupWasteQuantity = max(maxSetupWasteQuantity, result_from_formula)`.

        7.  **Расчет итогового тиража и листажа**:
            *   `finalQuantity = quantity + totalOperationWasteQuantity + maxSetupWasteQuantity`
            *   `finalSheets = requiredSheets + totalOperationWasteSheets + maxSetupWasteSheets`

        8.  **Финальная корректировка листажа**:
            *   Проверяется условие: `finalQuantity <= finalSheets * quantityProductsOnMainMaterial`.
            *   Если условие не выполняется, `finalSheets = ceil(finalQuantity / quantityProductsOnMainMaterial))` 

        **Часть 2: Техническая калькуляция операций (Расчет времен и количеств)**

        9.  **Расчет физических величин для каждой операции**:
            *   Для каждой `ProductOperation` в контекст скриптового движка передаются все рассчитанные ранее переменные (`finalQuantity`, `finalSheets` и т.д.).
            *   Вычисляются формулы `customMachineTimeFormula`, `customActionFormula`, `customMaterialFormula`.
            *   Результаты (время работы станка в секундах, время работы работника в секундах, количество материала операции в единицах) сохраняются для следующего этапа.

        **Часть 3: Экономический расчет (Применение цен и расчет итоговой стоимости)**

        10. **Сбор актуальных цен и расчет себестоимости (`primeCost`)**:
            *   Из соответствующих сервисов/таблиц цен (`PriceOf...`) извлекаются актуальные цены:
                *   `price_of_main_material`
                *   `amortization_price` (для каждой машины)
                *   `worker_price` (ставка нормо-часа)
                *   `operation_material_price` (для каждого расходника)
            *   `mainMaterialCost` = `finalSheets` * `price_of_main_material`.
            *   `operationsTotalCost` = Сумма стоимостей всех операций. Стоимость каждой операции:
                *   `machineCost = machine_time * amortization_price`
                *   `workerCost = worker_time * worker_price`
                *   `operationMaterialCost = operation_material_quantity * operation_material_price`
            *   `primeCost` (себестоимость) = `mainMaterialCost` + `operationsTotalCost`.

        11. **Расчет отпускной цены**:
            *   На `primeCost` начисляются маржа, налоги и стоимость банковских услуг (эквайринг).
            *   `finalPrice = primeCost * (1 + margin/100) * (1 + tax/100) * (1 + banking/100)`

        12. **Округление и расчет финальной стоимости тиража**:
            *   `pricePerOne = finalPrice / quantity` (делится на исходный тираж).
            *   `roundedPricePerOne` = Округление `pricePerOne` согласно маске/формуле `roundMask` из `Templates`.
            *   `finalTotalCost` = `roundedPricePerOne` * `quantity`.

2.  **`OrderService` (Сервис управления заказами)**
    *   `addItemToCart(productTypeId, quantity, userVariables)`: Создает `OrderItem`, вызывает `PriceCalculationService`, сериализует конфигурацию в JSON и сохраняет позицию в "корзине" (которая может быть временным `CustomerOrder` в статусе `DRAFT`).

---

## Примеры написания формул

Этот раздел содержит примеры для `operationWasteFormula` (брак) и `setupWasteFormula` (приладка), которые технолог может использовать при настройке `Operation`.

### Формула брака (`operationWasteFormula`)

Эта формула напрямую изменяет переменные `finalSheets` и `finalQuantity` в контексте. Брак по всем операциям суммируется.

**Пример 1: Добавить 5 листов на брак**
```groovy
finalSheets += 5
```

**Пример 2: Добавить 1% от тиража на брак (в изделиях)**
```groovy
finalQuantity += quantity * 0.01
```

### Формула приладки (`setupWasteFormula`)

Эта формула вычисляет требуемую приладку и обновляет переменную `maxSetupWasteEquivalent`, если текущее значение больше предыдущего.

**Пример 1: Приладка требует 10 листов**
```groovy
// Рассчитываем эквивалент в изделиях и обновляем максимум
def setupEquivalent = 10 * quantityProductsOnMainMaterial
maxSetupWasteEquivalent = Math.max(maxSetupWasteEquivalent, setupEquivalent)
```

**Пример 2: Приладка требует 20 изделий**
```groovy
// Просто обновляем максимум
maxSetupWasteEquivalent = Math.max(maxSetupWasteEquivalent, 20)
```

**Пример 3: Сложная логика (если тираж маленький, приладка 5 листов, иначе 10 изделий)**
```groovy
if (quantity < 500) {
    def setupEquivalent = 5 * quantityProductsOnMainMaterial
    maxSetupWasteEquivalent = Math.max(maxSetupWasteEquivalent, setupEquivalent)
} else {
    maxSetupWasteEquivalent = Math.max(maxSetupWasteEquivalent, 10)
}
```
    *   `placeOrder(CustomerOrder order)`: Финализирует заказ, меняет его статус и сохраняет в БД.
    *   `getOrdersForUser(User user)`: Возвращает историю заказов для пользователя.

---

## Предложение по реализации

Я могу помочь с реализацией первого и самого важного шага — **созданием `ProductCatalogView` и `ProductConfiguratorDialog`**. Это позволит вам "оживить" созданные шаблоны и увидеть, как работает логика расчета.

---

## Схема иерархии данных, переменных и материалов

Эта схема описывает, как сущности связаны друг с другом и какие данные (переменные и материалы) можно получить на каждом уровне. Это поможет понять, какой контекст доступен для скриптового движка на разных этапах расчета.

**Иерархия:**
`Templates` -> `AbstractProductType` -> `ProductOperation` -> `Operation` -> `AbstractMachine`

---

**1. `Templates` (Шаблон продукта)**

*   **Связи:** Содержит один или несколько `AbstractProductType`.
*   **Переменные (`List<Variable>`):**
    *   `quantity`: Тираж по умолчанию.
    *   `round`: Флаг математического округления.
    *   `roundMask`: Формула/маска для гибкого округления.
    *   *Источник:* `TemplateVariableService`.
*   **Материалы:** Не имеет.

---

**2. `AbstractProductType` (Компонент продукта, напр., "Обложка")**

*   **Связи:** Принадлежит `Templates`, содержит одну или несколько `ProductOperation`.
*   **Переменные (`List<Variable>`):**
    *   Зависят от конкретного типа. Для `OneSheetDigitalPrintingProductType`:
        *   `productWidth`, `productLength`: Размеры готового изделия.
        *   `bleed`: Вылеты.
        *   `materialFormula`: Формула расчета количества основного материала (листажа).
        *   `multiplication`: Флаг "замостить".
        *   ... и другие системные переменные для расчета (`quantityProductsOnMainMaterial`, `rows`, `columns` и т.д.).
    *   *Источник:* `ProductTypeVariableService`.
*   **Материалы:**
    *   Реализует интерфейс `HasMateria`.
    *   `getDefaultMat()`: Возвращает основной материал по умолчанию (например, бумага).
    *   `getSelectedMat()`: Возвращает набор доступных для выбора основных материалов.
    *   *Источник:* Поля `defaultMaterial` и `selectedMaterials` в `OneSheetDigitalPrintingProductType`.

---

**3. `ProductOperation` (Конкретная операция в продукте)**

*   **Связи:** Принадлежит `AbstractProductType`, ссылается на `Operation`.
*   **Переменные (`List<Variable> customVariables`):**
    *   Это **копия** переменных из `Operation`, которая может быть изменена для конкретного продукта.
    *   Например, можно переопределить `waste_percent` (процент брака) для операции резки именно в этом продукте.
    *   Переменные, помеченные как `show = true`, доступны для редактирования пользователем в конфигураторе заказа.
    *   *Источник:* Копируются из `Operation.variables` при создании, хранятся в `ProductOperation`.
*   **Материалы:**
    *   `selectedMaterial`: **Выбранный** материал для данной операции (например, конкретный тип ламината или краски).
    *   *Источник:* Поле `selectedMaterial` в `ProductOperation`.

---

**4. `Operation` (Шаблон/справочник операции)**

*   **Связи:** Используется `ProductOperation` как шаблон. Может быть связан с `AbstractMachine`.
*   **Переменные (`List<Variable>`):**
    *   Определяют параметры, специфичные для шаблона операции. Например: `резка_сложность`, `ламинация_скорость_приладки`.
    *   *Источник:* Определяются в редакторе `OperationEditor` (`MapEditorView`).
*   **Материалы:**
    *   Реализует интерфейс `HasMateria`.
    *   `getDefaultMat()`: Материал операции по умолчанию (например, "Цветная печать").
    *   `getSelectedMat()`: Набор доступных материалов для этой операции (например, "Цветная печать", "Черно-белая печать").
    *   *Источник:* Поля `defaultMaterial` и `listOfMaterials` в `Operation`.

---

**5. `AbstractMachine` (Оборудование)**

*   **Связи:** Связана с `Operation`. Одна операция может выполняться на одном типе оборудования.
*   **Переменные (`List<Variable>`):**
    *   Определяют физические и технические характеристики оборудования.
    *   `gap_top`, `gap_bottom`, `gap_left`, `gap_right`: Непечатные поля, которые определяют рабочую область.
    *   `cost_per_hour`: Стоимость часа работы оборудования (амортизация).
    *   ... и другие параметры, специфичные для машины.
    *   *Источник:* Определяются в редакторе соответствующей машины (напр., `DigitalPrintingMachineEditor`).
*   **Материалы:** Не имеет.

---

**Общая логика сбора контекста для расчета:**

При расчете стоимости для `ProductOperation` контекст переменных собирается иерархически, с переопределением "сверху вниз":

1.  **Базовый слой:** Переменные из `Templates` (`quantity`).
2.  **Слой компонента:** Переменные из `AbstractProductType` (`productWidth`, `bleed`...).
3.  **Слой оборудования:** Переменные из `AbstractMachine`, связанного с операцией (`gap_top`...).
4.  **Слой шаблона операции:** Переменные из `Operation`.
5.  **Слой конкретной операции:** Переменные из `ProductOperation.customVariables` (имеют наивысший приоритет и переопределяют все предыдущие).