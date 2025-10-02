package ru.bprn.printhouse;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import ru.bprn.printhouse.data.entity.CalculationPhase;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.repository.FormulasRepository;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.data.repository.StandartSizeRepository;
import ru.bprn.printhouse.data.repository.TypeOfMaterialRepository;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.machine.repository.DigitalPrintingMachineRepository;
import ru.bprn.printhouse.views.machine.service.MachineVariableService;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;
import ru.bprn.printhouse.views.material.repository.PrintSheetsMaterialRepository;
import ru.bprn.printhouse.views.material.repository.PrintingMaterialsRepository;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.repository.OperationRepository;
import ru.bprn.printhouse.views.operation.service.OperationVariableService;
import ru.bprn.printhouse.views.products.entity.PriceOfMachine;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.products.repository.PriceOfMachineRepository;
import ru.bprn.printhouse.views.products.repository.PriceOfMaterialRepository;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Этот компонент автоматически заполняет базу данных тестовыми данными при запуске.
 * Он активен только в профиле "dev".
 */
@Component
@Profile("dev") // Активируем только для профиля разработки
@AllArgsConstructor
@Order(1) // Указываем, что этот Runner должен выполниться одним из первых
public class DatabaseSeeder implements ApplicationRunner {

    private final TemplatesRepository templatesRepository;
    private final OperationRepository operationRepository;
    private final DigitalPrintingMachineRepository machineRepository;
    private final PrintSheetsMaterialRepository printSheetsMaterialRepository;
    private final PrintingMaterialsRepository printingMaterialsRepository;
    private final TypeOfMaterialRepository typeOfMaterialRepository;
    private final StandartSizeRepository standartSizeRepository;
    private final PriceOfMachineRepository priceOfMachineRepository;
    private final PriceOfMaterialRepository priceOfMaterialRepository;
    private final FormulasRepository formulasRepository;
    

    // Сервисы для инициализации переменных
    private final TemplateVariableService templateVariableService;
    private final ProductTypeVariableService productTypeVariableService;
    private final OperationVariableService operationVariableService;
    private final MachineVariableService machineVariableService;

    @Override
    public void run(ApplicationArguments args) {
        // Если база уже содержит шаблоны, ничего не делаем.
        // Это полезно, если вы временно переключили ddl-auto в 'update'.
        if (templatesRepository.count() > 0) {
            return;
        }

        // --- Заполняем справочник стандартных размеров ---
        seedStandardSizes();

        // --- Заполняем справочник типов материалов ---
        seedTypeOfMaterial();

        // --- Создаем связанные объекты ---

        // 1. Создаем оборудование (ЦПМ)
        DigitalPrintingMachine cpm = new DigitalPrintingMachine();
        cpm.setName("Konica Minolta C3070");
        cpm.initializeVariables(machineVariableService); // Инициализируем переменные по умолчанию
        DigitalPrintingMachine savedCpm = machineRepository.save(cpm);

        // Устанавливаем цену для созданной машины
        PriceOfMachine cpmPrice = new PriceOfMachine();
        cpmPrice.setMachine(savedCpm);
        cpmPrice.setPrice(1500.0); // Стоимость нормо-часа
        cpmPrice.setEffectiveDate(LocalDateTime.now().minusDays(1)); // Устанавливаем вчерашней датой для гарантии актуальности
        priceOfMachineRepository.save(cpmPrice);

        // 2. Создаем материалы
        // Находим тип "Мелованная бумага", который был создан в seed-методе.
        TypeOfMaterial type = typeOfMaterialRepository.findByName("Coated Paper")
                .orElseThrow(() -> new RuntimeException("Тип материала 'Coated Paper' не найден в базе данных."));


        PrintSheetsMaterial paper = new PrintSheetsMaterial();
        paper.setName("Мелованная бумага 300г/м2, SRA3");
        paper.setTypeOfMaterial(type);
        paper.setSizeX(450);
        paper.setSizeY(320);
        paper.setThickness(300);
        PrintSheetsMaterial savedPaper = printSheetsMaterialRepository.save(paper);

        // Устанавливаем цену для бумаги
        PriceOfMaterial paperPrice = new PriceOfMaterial();
        paperPrice.setMaterial(savedPaper);
        paperPrice.setPrice(15.5); // Цена за лист
        paperPrice.setEffectiveDate(LocalDateTime.now().minusDays(1));
        priceOfMaterialRepository.save(paperPrice);

        PrintingMaterials toner = new PrintingMaterials();
        toner.setName("Цветная печать CMYK");
        toner.setUnitsOfMeasurement("клик");
        toner.setSizeOfClick(244);

        PrintingMaterials toner1 = new PrintingMaterials();
        toner1.setName("Черно-белая печать Balck only");
        toner1.setUnitsOfMeasurement("клик");
        toner1.setSizeOfClick(244);

        // Устанавливаем связь со стороны владельца (AbstractMaterials)
        toner.setAbstractMachines(Set.of(savedCpm));
        toner1.setAbstractMachines(Set.of(savedCpm));
        List<PrintingMaterials> savedToners = printingMaterialsRepository.saveAll(List.of(toner, toner1));

        // Устанавливаем цены для кликов
        PriceOfMaterial colorClickPrice = new PriceOfMaterial();
        colorClickPrice.setMaterial(savedToners.get(0));
        colorClickPrice.setPrice(12.0); // Цена за цветной клик
        colorClickPrice.setEffectiveDate(LocalDateTime.now().minusDays(1));

        PriceOfMaterial bwClickPrice = new PriceOfMaterial();
        bwClickPrice.setMaterial(savedToners.get(1));
        bwClickPrice.setPrice(1.2); // Цена за ч/б клик
        bwClickPrice.setEffectiveDate(LocalDateTime.now().minusDays(1));

        priceOfMaterialRepository.saveAll(List.of(colorClickPrice, bwClickPrice));

        // 3. Создаем шаблон операции "Цифровая печать"
        Operation digitalPrintOp = new Operation();
        digitalPrintOp.setName("Цифровая печать");
        digitalPrintOp.setAbstractMachine(savedCpm);

        // Устанавливаем, что для этой конкретной операции доступны ОБА материала,
        // которые есть у машины. Технолог мог бы выбрать и подмножество.
        digitalPrintOp.setListOfMaterials(Set.copyOf(savedToners));
        // Устанавливаем тонер как материал по умолчанию для этой операции
        digitalPrintOp.setDefaultMaterial(savedToners.get(0));
        digitalPrintOp.initializeVariables(operationVariableService);
        
        // Создаем и сохраняем шаблонные формулы
        String wasteFormulaExpression = """
                // Добавляем 1% от тиража на брак, но не менее 3-х изделий.
                finalQuantity += Math.max(3, Math.ceil(quantity * 0.01))""";
        Formulas wasteFormulaTemplate = new Formulas();
        wasteFormulaTemplate.setName("Стандартный брак для ЦПМ (1% > 3шт)");
        wasteFormulaTemplate.setDescription("Добавляет 1% от тиража на брак, но не менее 3-х изделий.");
        wasteFormulaTemplate.setFormula(wasteFormulaExpression);
        wasteFormulaTemplate.setPhase(CalculationPhase.WASTE_CALCULATION);
        wasteFormulaTemplate.setPriority(10);
        Formulas savedWasteTemplate = formulasRepository.save(wasteFormulaTemplate);

        String setupFormulaExpression = """
                // Приладка требует 10 листов.
                def setupEquivalent = 10 * quantityProductsOnMainMaterial;
                maxSetupWasteEquivalent = Math.max(maxSetupWasteEquivalent, setupEquivalent)""";
        Formulas setupFormulaTemplate = new Formulas();
        setupFormulaTemplate.setName("Стандартная приладка для ЦПМ (10 листов)");
        setupFormulaTemplate.setDescription("Устанавливает приладку в 10 листов и пересчитывает ее в эквивалент изделий.");
        setupFormulaTemplate.setFormula(setupFormulaExpression);
        setupFormulaTemplate.setPhase(CalculationPhase.WASTE_CALCULATION);
        setupFormulaTemplate.setPriority(20); // Приоритет выше, чтобы выполнялась после брака
        Formulas savedSetupTemplate = formulasRepository.save(setupFormulaTemplate);

        // Устанавливаем в операцию и выражения, и ссылки на шаблоны
        digitalPrintOp.setWasteExpression(wasteFormulaExpression);
        digitalPrintOp.setWasteFormulaTemplate(savedWasteTemplate);

        digitalPrintOp.setSetupExpression(setupFormulaExpression);
        digitalPrintOp.setSetupFormulaTemplate(savedSetupTemplate);
        
        // Создаем и сохраняем шаблонные формулы для расчета времени и расхода
        String machineTimeExpr = """
                // Время работы машины = (листаж / скорость) * 3600 секунд.
                // Примем скорость для Konica Minolta C3070 равной 3960 листов/час (66 л/мин).
                return (finalSheets / 3960.0) * 3600""";
        Formulas machineTimeTemplate = new Formulas();
        machineTimeTemplate.setName("Время работы ЦПМ");
        machineTimeTemplate.setDescription("Расчет времени работы ЦПМ на основе скорости и итогового листажа.");
        machineTimeTemplate.setFormula(machineTimeExpr);
        machineTimeTemplate.setPhase(CalculationPhase.TECHNICAL_CALCULATION);
        machineTimeTemplate.setPriority(10);
        Formulas savedMachineTimeTemplate = formulasRepository.save(machineTimeTemplate);

        String actionTimeExpr = """
                // Время работы оператора = 5 минут на подготовку + 2 минуты на каждую 1000 листов
                return 300 + (finalSheets / 1000) * 120""";
        Formulas actionTimeTemplate = new Formulas();
        actionTimeTemplate.setName("Время работы оператора ЦПМ");
        actionTimeTemplate.setDescription("Расчет времени ручной работы оператора.");
        actionTimeTemplate.setFormula(actionTimeExpr);
        actionTimeTemplate.setPhase(CalculationPhase.TECHNICAL_CALCULATION);
        actionTimeTemplate.setPriority(20);
        Formulas savedActionTimeTemplate = formulasRepository.save(actionTimeTemplate);

        String materialAmountExpr = """
                // Расход кликов равен количеству листов
                return finalSheets""";
        Formulas materialAmountTemplate = new Formulas();
        materialAmountTemplate.setName("Расход кликов ЦПМ");
        materialAmountTemplate.setDescription("Расход материала (кликов) равен итоговому листажу.");
        materialAmountTemplate.setFormula(materialAmountExpr);
        materialAmountTemplate.setPhase(CalculationPhase.TECHNICAL_CALCULATION);
        materialAmountTemplate.setPriority(30);
        Formulas savedMaterialAmountTemplate = formulasRepository.save(materialAmountTemplate);

        digitalPrintOp.setMachineTimeExpression(machineTimeExpr);
        digitalPrintOp.setMachineTimeFormulaTemplate(savedMachineTimeTemplate);
        digitalPrintOp.setActionTimeExpression(actionTimeExpr);
        digitalPrintOp.setActionTimeFormulaTemplate(savedActionTimeTemplate);
        digitalPrintOp.setMaterialAmountExpression(materialAmountExpr);
        digitalPrintOp.setMaterialAmountFormulaTemplate(savedMaterialAmountTemplate);

        operationRepository.save(digitalPrintOp);

        // 4. Создаем главный шаблон продукта "Визитка"
        Templates businessCardTemplate = new Templates();
        businessCardTemplate.setName("Визитка 90x50");
        businessCardTemplate.setDescription("Стандартная визитка на плотной бумаге");
        businessCardTemplate.initializeVariables(templateVariableService);

        // 5. Создаем компонент продукта "Однолистовая печать" для визитки
        OneSheetDigitalPrintingProductType productType = new OneSheetDigitalPrintingProductType();
        productType.setName("Основа визитки");
        productType.setDefaultMaterial(savedPaper);
        // Для продукта типа "Однолистовая печать" доступны только листовые материалы.
        // В нашем случае это только 'paper'.
        productType.setSelectedMaterials(Set.of(savedPaper)); //
        productType.initializeVariables(productTypeVariableService);

        // 6. Создаем конкретную операцию для этого компонента на основе шаблона
        ProductOperation productOperation = new ProductOperation(digitalPrintOp);
        productOperation.setName("Печать визиток");
        productOperation.setProduct(productType);

        // 7. Собираем все вместе
        productType.setProductOperations(List.of(productOperation));
        businessCardTemplate.setProductTypes(Set.of(productType));

        // 8. Сохраняем главный шаблон. Благодаря Cascade, все дочерние объекты сохранятся.
        templatesRepository.save(businessCardTemplate);
    }

    private void seedStandardSizes() {
        if (standartSizeRepository.count() > 0) {
            return; // Не заполнять, если данные уже есть
        }

        List<StandartSize> sizes = List.of(
                new StandartSize(null, "A1", 841.0, 594.0),
                new StandartSize(null, "A2", 594.0, 420.0),
                new StandartSize(null, "A3", 420.0, 297.0),
                new StandartSize(null, "A4", 297.0, 210.0),
                new StandartSize(null, "A5", 210.0, 148.5),
                new StandartSize(null, "A6", 148.5, 105.0),
                new StandartSize(null, "A7", 105.0, 74.0),
                new StandartSize(null, "Визитка РФ (90x50)", 90.0, 50.0),
                new StandartSize(null, "Визитка Евро (85x55)", 85.0, 55.0),
                new StandartSize(null, "Открытка (150x100)", 150.0, 100.0),
                new StandartSize(null, "Календарик (100x70)", 100.0, 70.0)
        );
        standartSizeRepository.saveAll(sizes);
    }

    /**
     * Заполняет справочник типов материалов.
     */
    private void seedTypeOfMaterial() {
        if (typeOfMaterialRepository.count() > 0) {
            return; // Не заполнять, если данные уже есть
        }

        List<TypeOfMaterial> types = List.of(
                new TypeOfMaterial(null, "Plain Paper"),
                new TypeOfMaterial(null, "Coated Paper"),
                new TypeOfMaterial(null, "Syntetic Paper"),
                new TypeOfMaterial(null, "High quality Paper"),
                new TypeOfMaterial(null, "Laser jet Film"),
                new TypeOfMaterial(null, "Ink jet Paper")
        );
        typeOfMaterialRepository.saveAll(types);
    }
}