package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;

import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.ProductOperationService;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.repository.OneSheetDigitalPrintingProductTypeRepository;

import java.util.List;
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

        // Копируем связанные сущности
        newProduct.getSelectedMaterials().addAll(productType.getSelectedMaterials());
        newProduct.setDefaultMaterial(productType.getDefaultMaterial());

        // Глубокое копирование списка переменных, где теперь хранятся все поля
        List<Variable> copiedVariables = productType.getVariables().stream()
                .map(Variable::new) // Используем конструктор копирования Variable
                .collect(Collectors.toList());
        newProduct.setVariables(copiedVariables);

        // Дублируем вложенные ProductOperation
        List<ProductOperation> duplicatedOperations = productType.getProductOperations().stream()
                .map(productOperationService::duplicate)
                .peek(newOp -> newOp.setProduct(newProduct)) // Устанавливаем связь с новым продуктом
                .collect(Collectors.toList());
        newProduct.setProductOperations(duplicatedOperations);
        return save(newProduct);
    }
}
