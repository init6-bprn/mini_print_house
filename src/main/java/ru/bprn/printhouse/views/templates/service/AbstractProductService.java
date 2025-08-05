package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.repository.AbstractProductTypeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AbstractProductService {
    private final OneSheetDigitalPrintingProductTypeService printingProductTypeService;
    private final AbstractProductTypeRepository abstractProductTypeRepository;
    private final OperationService operationService;

    public AbstractProductService(OneSheetDigitalPrintingProductTypeService printingProductTypeService, AbstractProductTypeRepository abstractProductTypeRepository, OperationService operationService){

        this.printingProductTypeService = printingProductTypeService;
        this.abstractProductTypeRepository = abstractProductTypeRepository;
        this.operationService = operationService;
    }

    public List<AbstractProductType> findAll() {return this.abstractProductTypeRepository.findAll();}

    public Optional<AbstractProductType> findAllById(UUID id) {return this.abstractProductTypeRepository.findById(id);}

    public AbstractProductType save(AbstractProductType product) {return this.abstractProductTypeRepository.save(product);}

    public void delete (AbstractProductType product) {this.abstractProductTypeRepository.delete(product);}

    public AbstractProductType duplicateProductType(AbstractProductType productType) {
        return switch (productType) {
            case OneSheetDigitalPrintingProductType product -> printingProductTypeService.duplicate(product);
            default -> null;
        };
    }

    public void duplicateOperation(AbstractProductType product, Operation operation) {
        operationService.duplicate(operation).ifPresent(product.getOperationsSet()::add);
        abstractProductTypeRepository.save(product);
    }
}
