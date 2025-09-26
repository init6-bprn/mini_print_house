package ru.bprn.printhouse;

import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;

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
        printSheetsMaterialRepository.save(paper);

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
        printingMaterialsRepository.saveAll(List.of(toner, toner1));

        // 3. Создаем шаблон операции "Цифровая печать"
        Operation digitalPrintOp = new Operation();
        digitalPrintOp.setName("Цифровая печать");
        digitalPrintOp.setAbstractMachine(savedCpm);

        // Устанавливаем, что для этой конкретной операции доступны ОБА материала,
        // которые есть у машины. Технолог мог бы выбрать и подмножество.
        digitalPrintOp.setListOfMaterials(Set.of(toner, toner1));
        // Устанавливаем тонер как материал по умолчанию для этой операции
        digitalPrintOp.setDefaultMaterial(toner);
        digitalPrintOp.initializeVariables(operationVariableService);
        operationRepository.save(digitalPrintOp);

        // 4. Создаем главный шаблон продукта "Визитка"
        Templates businessCardTemplate = new Templates();
        businessCardTemplate.setName("Визитка 90x50");
        businessCardTemplate.setDescription("Стандартная визитка на плотной бумаге");
        businessCardTemplate.initializeVariables(templateVariableService);

        // 5. Создаем компонент продукта "Однолистовая печать" для визитки
        OneSheetDigitalPrintingProductType productType = new OneSheetDigitalPrintingProductType();
        productType.setName("Основа визитки");
        productType.setDefaultMaterial(paper);
        // Для продукта типа "Однолистовая печать" доступны только листовые материалы.
        // В нашем случае это только 'paper'.
        productType.setSelectedMaterials(Set.of(paper)); //
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