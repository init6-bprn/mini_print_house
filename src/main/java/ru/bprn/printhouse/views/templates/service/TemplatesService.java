package ru.bprn.printhouse.views.templates.service;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.ProductOperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.*;

@Service
public class TemplatesService {

    private final TemplatesRepository repository;
    private final AbstractProductService abstractProductService;
    private final OperationService operationService;
    private final ProductOperationService productOperationService;

    public TemplatesService (TemplatesRepository repository, AbstractProductService abstractProductService, OperationService operationService, ProductOperationService productOperationService) {
        this.repository = repository;
        this.abstractProductService = abstractProductService;
        this.operationService = operationService;
        this.productOperationService = productOperationService;
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

    public String save(Object object, Templates currentTemplate) {
        String s ="Сохранено";
        switch (object) {
            case Templates templates-> save(templates);
            case AbstractProductType productType -> addProductToTemplateAndSaveAll(currentTemplate, productType);
            case ProductOperation productOperation ->abstractProductService.saveProductOperation(productOperation);
            default -> s = "Не сохранено";
        }
        return  s;
    }

    public String delete (Object object, Object parent){
        String s ="Сохранено";
        switch (object) {
            case Templates template -> delete(template);
            case AbstractProductType entity -> {
                if (parent instanceof Templates) {
                    ((Templates) parent).getProductTypes().remove(entity);
                    save((Templates) parent);
                }
            }
            case ProductOperation productOperation -> {
                if (parent instanceof AbstractProductType) {
                    ((AbstractProductType) parent).getProductOperations().remove(productOperation);
                    abstractProductService.save((AbstractProductType) parent);
                }
            }
            default -> s ="Не знаю, что удалить!";
        }
        return s;
    }

    public void addProductToTemplateAndSaveAll(Templates template, AbstractProductType product) {
        Templates temp = this.findById(template.getId()).orElse(null);
        if (temp!=null) {
            temp.getProductTypes().add(abstractProductService.save(product));
            this.save(temp);
        }
    }

    public void duplicateTemplate(Templates template){
        var newTemplate = new Templates();
        newTemplate.setDescription(template.getDescription());
        newTemplate.setName(template.getName()+" - Дубликат");
        newTemplate.setMaxQuantity(template.getMaxQuantity());
        newTemplate.setMinQuantity(template.getMinQuantity());
        newTemplate.setQuantity(template.getQuantity());
        newTemplate.setRoundForMath(template.isRoundForMath());
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
            t.getProductTypes().add(duplicateProduct(p));
            repository.save(t);
        }
    }

    public ProductOperation duplicateProductOperation(AbstractProductType parentProduct, ProductOperation original) {
        if (original == null || original.getProduct() == null) return null;
        ProductOperation newProductOperation = productOperationService.duplicate(original);
        newProductOperation.setProduct(parentProduct);
        // Устанавливаем sequence как следующий по порядку в списке родителя
        newProductOperation.setSequence(parentProduct.getProductOperations().size());
        parentProduct.getProductOperations().add(newProductOperation);
        AbstractProductType savedParent = abstractProductService.save(parentProduct);
        return savedParent.getProductOperations().stream()
                .max(java.util.Comparator.comparing(ProductOperation::getSequence))
                .orElse(null);
    }

    public ProductOperation addOperationToProduct(AbstractProductType product, Operation operation) {
        return this.abstractProductService.addOperationToProduct(product, operation);
    }

    public void swapProductOperations(ProductOperation productOperation, boolean moveUp) {
        abstractProductService.swapProductOperations(productOperation, moveUp);
    }

    public TreeDataProvider<Object> populateGrid(String filter) {
        Collection<Templates> collection;
        if (filter == null || filter.isEmpty())
            collection = this.findAll();
        else collection = this.repository.search(filter);

        TreeData<Object> data = new TreeData<>();
        for (Templates obj:collection) {
            data.addItem(null, obj);
            for (AbstractProductType product: obj.getProductTypes()) {
                if (product != null) {
                    data.addItem(obj, product);
                    product.getProductOperations().forEach(op -> data.addItem(product, op));
                }
            }

        }
        return new TreeDataProvider<>(data);
    }

}
