package ru.bprn.printhouse.views.templates.service;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.*;

@Service
public class TemplatesService {

    private final TemplatesRepository repository;
    private final AbstractProductService abstractProductService;

    public TemplatesService (TemplatesRepository repository, AbstractProductService abstractProductService) {
        this.repository = repository;
        this.abstractProductService = abstractProductService;
    }

    public List<Templates> findAll() {return this.repository.findAll();}

    public void delete(Templates templates) {this.repository.delete(templates);}

    public Templates save(Templates templates) {return  this.repository.save(templates);}

    public void saveAndFlush(Templates templates) {this.repository.saveAndFlush(templates);}

    public Optional<Templates> findById(Long id) {return this.repository.findById(id);}

    public Set<AbstractProductType> getProductTypeForTemplate(Templates templates) {
        var temp = findById(templates.getId());
        return temp.map(Templates::getProductTypes).orElse(null);
    }

    public void duplicateTemplate(Templates template){
        var newTemplate = new Templates();
        newTemplate.setDescription(template.getDescription());
        newTemplate.setName(template.getName()+" - Дубликат");
        Set<AbstractProductType> set = new HashSet<>();
        for (AbstractProductType product:template.getProductTypes()) {
            var dc = duplicateProduct(product);
            if (dc != null) set.add(dc);
        }
        newTemplate.setProductTypes(set);
        this.save(newTemplate);
    }

    public AbstractProductType duplicateProduct(AbstractProductType productType) {return this.abstractProductService.duplicateProductType(productType);}

    public void addProductToTemplate(Templates template, AbstractProductType productType) {
        var temp = findById(template.getId());
        var prod = this.abstractProductService.findAllById(productType.getId());
        if (temp.isPresent()&& prod.isPresent()) {
            var t = temp.get();
            var p = prod.get();
            t.getProductTypes().add(p);
            repository.save(t);
        }
    }

    public void duplicateOperation(AbstractProductType product, Operation operation) {this.abstractProductService.duplicateOperation(product, operation);}

    public TreeDataProvider<Object> populateGrid(String filter) {
        Collection<Templates> collection;
        if (filter == null || filter.isEmpty())
            collection = this.findAll();
        else collection = this.repository.search(filter);

        TreeData<Object> data = new TreeData<>();
        for (Templates obj:collection) {
            data.addItem(null, obj);
            for (AbstractProductType product: obj.getProductTypes()) {
                data.addItem(obj, product);
            }

        }
        return new TreeDataProvider<>(data);
    }

}
