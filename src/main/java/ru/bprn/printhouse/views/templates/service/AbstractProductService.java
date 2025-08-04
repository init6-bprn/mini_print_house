package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.repository.AbstractProductTypeRepository;

import java.util.List;

@Service
public class AbstractProductService {
    private final OneSheetDigitalPrintingProductTypeService printingProductTypeService;
    private final AbstractProductTypeRepository abstractProductTypeRepository;

    public AbstractProductService(OneSheetDigitalPrintingProductTypeService printingProductTypeService, AbstractProductTypeRepository abstractProductTypeRepository){

        this.printingProductTypeService = printingProductTypeService;
        this.abstractProductTypeRepository = abstractProductTypeRepository;
    }

    public List<AbstractProductType> findAll() {return this.abstractProductTypeRepository.findAll();}

    public AbstractProductType save(AbstractProductType product) {return this.abstractProductTypeRepository.save(product);}

    public void delete (AbstractProductType product) {this.abstractProductTypeRepository.delete(product);}

    public AbstractProductType duplicateProductType(AbstractProductType productType) {

        return switch (productType) {
            case OneSheetDigitalPrintingProductType product -> printingProductTypeService.duplicate(product);
            default -> null;
        };
    }
}
