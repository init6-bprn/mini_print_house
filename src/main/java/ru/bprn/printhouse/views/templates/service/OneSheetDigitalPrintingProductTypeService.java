package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;

import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.ProductOperationService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.repository.OneSheetDigitalPrintingProductTypeRepository;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class OneSheetDigitalPrintingProductTypeService {
    private final OneSheetDigitalPrintingProductTypeRepository repository;
    private final ProductOperationService productOperationService;

    public OneSheetDigitalPrintingProductTypeService (OneSheetDigitalPrintingProductTypeRepository repository, ProductOperationService productOperationService) {

        this.repository = repository;
        this.productOperationService = productOperationService;
    }

    public OneSheetDigitalPrintingProductType save(OneSheetDigitalPrintingProductType productType){
        return this.repository.save(productType);
    }

    public void delete(OneSheetDigitalPrintingProductType productType) {
        this.repository.delete(productType);
    }

    public List<OneSheetDigitalPrintingProductType> findAll() {return this.repository.findAll();}

    public OneSheetDigitalPrintingProductType duplicate(OneSheetDigitalPrintingProductType productType) {
        var newProduct = new OneSheetDigitalPrintingProductType();
        newProduct.setName(productType.getName() + " - Копия");

        // Копируем связанные сущности, избегая дубликатов detached-объектов
        Set<AbstractMaterials> uniqueMaterials = new HashSet<>(productType.getSelectedMaterials());
        if (productType.getDefaultMaterial() != null) {
            uniqueMaterials.add(productType.getDefaultMaterial());
        }

        Set<PrintSheetsMaterial> finalMaterials = uniqueMaterials.stream()
                .map(m -> (PrintSheetsMaterial) m)
                .collect(Collectors.toSet());
        newProduct.setSelectedMaterials(finalMaterials);

        // Устанавливаем defaultMaterial как ссылку на объект, который УЖЕ лежит в коллекции selectedMaterials
        if (productType.getDefaultMaterial() != null) {
            finalMaterials.stream()
                    .filter(m -> m.getId().equals(productType.getDefaultMaterial().getId()))
                    .findFirst()
                    .ifPresent(newProduct::setDefaultMaterial);
        }

        // Глубокое копирование списка переменных, где теперь хранятся все поля
        List<Variable> copiedVariables = productType.getVariables().stream()
                .map(Variable::new) // Используем конструктор копирования Variable
                .collect(Collectors.toList());
        newProduct.setVariables(copiedVariables);

        // Дублируем вложенные ProductOperation
        List<ProductOperation> duplicatedOperations = productType.getProductOperations().stream()
        .map(originalOp -> {
            ProductOperation newOp = productOperationService.duplicate(originalOp);
            newOp.setProduct(newProduct); // Устанавливаем связь с новым продуктом

            // КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: Заменяем ссылку на материал в операции на канонический экземпляр из коллекции нового продукта
            if (newOp.getSelectedMaterial() != null) {
                finalMaterials.stream()
                        .filter(m -> m.getId().equals(newOp.getSelectedMaterial().getId()))
                        .findFirst()
                        .ifPresent(newOp::setSelectedMaterial);
            }
            return newOp;
        })
                .collect(Collectors.toList());
        newProduct.setProductOperations(duplicatedOperations);
        return newProduct;
    }
}
