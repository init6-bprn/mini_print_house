package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.ProductOperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.repository.AbstractProductTypeRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AbstractProductService {
    private final OneSheetDigitalPrintingProductTypeService printingProductTypeService;
    private final AbstractProductTypeRepository abstractProductTypeRepository;
    private final OperationService operationService;
    private final ProductOperationService productOperationService;

    public AbstractProductService(OneSheetDigitalPrintingProductTypeService printingProductTypeService, AbstractProductTypeRepository abstractProductTypeRepository, OperationService operationService, ProductOperationService productOperationService){

        this.printingProductTypeService = printingProductTypeService;
        this.abstractProductTypeRepository = abstractProductTypeRepository;
        this.operationService = operationService;
        this.productOperationService = productOperationService;
    }

    public List<AbstractProductType> findAll() {return this.abstractProductTypeRepository.findAll();}

    public Optional<AbstractProductType> findAllById(UUID id) {return this.abstractProductTypeRepository.findById(id);}

    public AbstractProductType save(AbstractProductType product) {return this.abstractProductTypeRepository.save(product);}

    public void delete (AbstractProductType product) {this.abstractProductTypeRepository.delete(product);}

/*
    public void duplicateOperation(AbstractProductType product, Operation operation) {
        operationService.duplicate(operation).ifPresent(product.getOperationsSet()::add);
        abstractProductTypeRepository.save(product);
    }
*/
        public ProductOperation addOperationToProduct(AbstractProductType product, Operation operation) {
        // Загружаем управляемую (managed) версию продукта из БД
        Optional<AbstractProductType> managedProductOpt = abstractProductTypeRepository.findById(product.getId());
        if (managedProductOpt.isPresent()) {
            AbstractProductType managedProduct = managedProductOpt.get();
            ProductOperation newProductOperation = new ProductOperation(operation);
            newProductOperation.setProduct(managedProduct);
            newProductOperation.setSequence(managedProduct.getProductOperations().size());
            managedProduct.getProductOperations().add(newProductOperation);

            AbstractProductType savedProduct = abstractProductTypeRepository.save(managedProduct);
            // Возвращаем последнюю добавленную операцию из сохраненного (и теперь актуального) продукта
            return savedProduct.getProductOperations().get(savedProduct.getProductOperations().size() - 1);
        }
        return null; // Возвращаем null, если родительский продукт не найден
    }

    public ProductOperation saveProductOperation(ProductOperation productOperation) {
        return productOperationService.save(productOperation);
    }

    public void swapProductOperations(ProductOperation current, boolean moveUp) {
        AbstractProductType product = current.getProduct();
        if (product == null) {
            return;
        }
        List<ProductOperation> operations = product.getProductOperations();
        int currentIndex = operations.indexOf(current);
        if ((moveUp && currentIndex <= 0) || (!moveUp && currentIndex >= operations.size() - 1)) {
            return; // Граничные условия: ничего не делаем
        }
        int neighborIndex = moveUp ? currentIndex - 1 : currentIndex + 1;
        Collections.swap(operations, currentIndex, neighborIndex);
        // Пересчитываем sequence для всего списка, чтобы гарантировать целостность
        for (int i = 0; i < operations.size(); i++) { operations.get(i).setSequence(i); }
        abstractProductTypeRepository.save(product);
    }
}
