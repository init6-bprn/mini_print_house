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

}
