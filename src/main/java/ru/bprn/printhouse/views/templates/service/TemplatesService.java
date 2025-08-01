package ru.bprn.printhouse.views.templates.service;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.ChainsRepository;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.*;

@Service
public class TemplatesService {

    private final TemplatesRepository repository;
    private final ChainsRepository chainsRepository;
    private final OneSheetDigitalPrintingProductTypeService printingProductTypeService;

    public TemplatesService (TemplatesRepository repository, ChainsRepository chainsRepository, OneSheetDigitalPrintingProductTypeService printingProductTypeService) {
        this.repository = repository;
        this.chainsRepository = chainsRepository;
        this.printingProductTypeService = printingProductTypeService;
    }

    public List<Templates> findAll() {return this.repository.findAll();}

    public List<Chains> findAllChains(String filter) {
        if (filter==null||filter.isEmpty()) return this.chainsRepository.findAll();
        else return this.chainsRepository.search(filter);}

    public List<Templates> findAllAsAbstractTemplates() {
        List<Templates> aList = new ArrayList<>();
        for (Templates t : this.repository.findAll())
            aList.add((Templates) t);
        return aList ;
    }

    public void delete(Templates templates) {this.repository.delete(templates);}

    public Templates save(Templates templates) {return  this.repository.save(templates);}

    public void saveAndFlush(Templates templates) {this.repository.saveAndFlush(templates);}

    public Optional<Templates> findById(Long id) {return this.repository.findById(id);}

    public Set<AbstractProductType> getChainsForTemplate(Templates templates) {
        var temp = findById(templates.getId());
        return temp.map(Templates::getProductTypes).orElse(null);
    }

    public AbstractProductType duplicateChain(AbstractProductType productType) {
        AbstractProductType abstractProductType;
        switch (productType){
            case OneSheetDigitalPrintingProductType product:
                abstractProductType = printingProductTypeService.duplicate(product);
                break;
            default: abstractProductType = new AbstractProductType();
        }
        //Переписать надо тутачке...
        //newChain.setStrJSON(chain.getStrJSON());
        //chainsRepository.save(newChain);
        return abstractProductType;
    }

    public void duplicateTemplate(Templates template){
        var newTemplate = new Templates();
        newTemplate.setDescription(template.getDescription());
        newTemplate.setName(template.getName()+" - Дубликат");
        Set<AbstractProductType> set = new HashSet<>();
        for (AbstractProductType c:template.getProductTypes()) {
            var dc = this.duplicateChain(c);
            if (dc != null) set.add(dc);
        }
        newTemplate.setProductTypes(set);
        this.save(newTemplate);
    }

    public TreeDataProvider<Object> populateGrid(String filter) {
        Collection<Templates> collection;
        if (filter == null || filter.isEmpty())
            collection = this.findAllAsAbstractTemplates();
        else collection = this.repository.search(filter);

        TreeData<Object> data = new TreeData<>();
        for (Templates obj:collection) {
            data.addItem(null, obj);

        }

        for (Templates temp : collection) {
            if (temp != null) {
                Collection<AbstractProductType> c = new ArrayList<>(temp.getProductTypes());
                data.addItems(temp, c);
            }
        }
        return new TreeDataProvider<>(data);
    }

}
