package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.repository.OneSheetDigitalPrintingProductTypeRepository;

import java.util.List;


@Service
public class OneSheetDigitalPrintingProductTypeService {
    private final OneSheetDigitalPrintingProductTypeRepository repository;

    public OneSheetDigitalPrintingProductTypeService (OneSheetDigitalPrintingProductTypeRepository repository) {

        this.repository = repository;
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
        return save(newProduct);
    }
}
