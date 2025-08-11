package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.repository.OneSheetDigitalPrintingProductTypeRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class OneSheetDigitalPrintingProductTypeService {
    private final OneSheetDigitalPrintingProductTypeRepository repository;
    private final OperationService operationService;

    public OneSheetDigitalPrintingProductTypeService (OneSheetDigitalPrintingProductTypeRepository repository, OperationService operationService) {

        this.repository = repository;
        this.operationService = operationService;
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
        newProduct.setName(productType.getName());
        newProduct.setProductSizeX(productType.getProductSizeX());
        newProduct.setProductSizeY(productType.getProductSizeY());
        newProduct.setSelectedMaterials(productType.getSelectedMaterials());
        newProduct.setDefaultMaterial(productType.getDefaultMaterial());
        newProduct.setBleed(productType.getBleed());
        newProduct.setMaterialFormula(productType.getMaterialFormula());
        newProduct.setVariables(productType.getVariables());
        /*
        if (!productType.getOperationsSet().isEmpty()) {
            Set<Operation> operationSet = new HashSet<>();
            for (Operation operation : productType.getOperationsSet())
                this.operationService.duplicate(operation).ifPresent(operationSet::add);  //  <--- косяк здесь
            newProduct.setOperationsSet(operationSet);   //  <--- косяк здесь и выше
        }

         */
        return save(newProduct);
    }
}
