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
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.*;
import java.util.stream.Collectors;

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

    public Optional<Templates> findById(UUID id) {return this.repository.findById(id);}

    public Set<AbstractProductType> getProductTypeForTemplate(Templates templates) {
        var temp = findById(templates.getId());
        return temp.map(Templates::getProductTypes).orElse(null);
    }

    public String save(Object object, Templates currentTemplate, boolean isNew) {
        String s ="Сохранено";
        switch (object) {
            case Templates templates-> save(templates);
            case AbstractProductType productType -> {
                if (isNew) {
                    addProductToTemplateAndSaveAll(currentTemplate, productType);
                } else {
                    abstractProductService.save(productType);
                }
            }
            case ProductOperation productOperation ->abstractProductService.saveProductOperation(productOperation);
            default -> s = "Не сохранено";
        }
        return  s;
    }

    public String delete(Object object, Object parent) {
        String s = "Удалено";
        switch (object) {
            case Templates template -> delete(template);
            case AbstractProductType entity -> {
                if (parent instanceof Templates template) {
                    findById(template.getId()).ifPresent(managedTemplate -> {
                        managedTemplate.getProductTypes().removeIf(p -> p.getId().equals(entity.getId()));
                        save(managedTemplate);
                    });
                }
            }
            case ProductOperation productOperation -> {
                if (parent instanceof AbstractProductType productType) {
                    abstractProductService.findAllById(productType.getId()).ifPresent(managedProduct -> {
                        managedProduct.getProductOperations().removeIf(op -> op.getId().equals(productOperation.getId()));
                        abstractProductService.save(managedProduct);
                    });
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
/* 
    public void addProductToTemplate(Templates template, AbstractProductType productType) {
        // Находим управляемую (managed) версию родительского шаблона
        findById(template.getId()).ifPresent(managedTemplate -> {
            // Добавляем новый (transient) продукт в коллекцию
            managedTemplate.getProductTypes().add(productType);
            // Сохраняем родителя. CascadeType.ALL на коллекции позаботится о сохранении нового продукта.
            save(managedTemplate);
        });
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
*/
    public List<Templates> findAllTemplates(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {
            return repository.findAll();
        } else {
            return repository.searchList(stringFilter);
        }
    }

}
