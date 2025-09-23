package ru.bprn.printhouse.views.templates.service;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.ProductOperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.repository.AbstractProductTypeRepository;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TemplatesModuleService {

    private final TemplatesRepository templatesRepository;
    private final AbstractProductTypeRepository abstractProductTypeRepository;
    private final ProductOperationService productOperationService;

    public TreeDataProvider<Object> getTreeDataProvider(String filter) {
        List<Templates> templates = (filter == null || filter.isEmpty())
                ? templatesRepository.findAll()
                : templatesRepository.search(filter).stream().toList();

        TreeData<Object> data = new TreeData<>();
        for (Templates template : templates) {
            data.addItem(null, template);
            for (AbstractProductType product : template.getProductTypes()) {
                data.addItem(template, product);
                product.getProductOperations().forEach(op -> data.addItem(product, op));
            }
        }
        return new TreeDataProvider<>(data);
    }

    public Object save(Object entity, Object parent) {
        return switch (entity) {
            case Templates t -> templatesRepository.save(t);
            case AbstractProductType pt -> {
                if (parent instanceof Templates p) {
                    Templates managedParent = templatesRepository.findById(p.getId()).orElseThrow();
                    managedParent.getProductTypes().add(pt);
                    templatesRepository.save(managedParent);
                    yield pt;
                }
                yield abstractProductTypeRepository.save(pt);
            }
            case ProductOperation po -> productOperationService.save(po);
            default -> throw new IllegalArgumentException("Unsupported entity type for saving: " + entity.getClass());
        };
    }

    public void delete(Object entity, Object parent) {
        switch (entity) {
            case Templates t -> templatesRepository.delete(t);
            case AbstractProductType pt -> {
                if (parent instanceof Templates p) {
                    templatesRepository.findById(p.getId()).ifPresent(managedParent -> {
                        managedParent.getProductTypes().removeIf(child -> child.getId().equals(pt.getId()));
                        templatesRepository.save(managedParent);
                    });
                }
            }
            case ProductOperation po -> {
                if (parent instanceof AbstractProductType p) {
                    abstractProductTypeRepository.findById(p.getId()).ifPresent(managedParent -> {
                        managedParent.getProductOperations().removeIf(child -> child.getId().equals(po.getId()));
                        abstractProductTypeRepository.save(managedParent);
                    });
                }
            }
            default -> throw new IllegalArgumentException("Unsupported entity type for deletion: " + entity.getClass());
        }
    }

    public Object duplicate(Object entity, Object parent) {
        return switch (entity) {
            case Templates t -> duplicateTemplate(t);
            case AbstractProductType pt -> duplicateProduct(pt, (Templates) parent);
            case ProductOperation po -> duplicateOperation(po, (AbstractProductType) parent);
            default -> throw new IllegalArgumentException("Unsupported entity type for duplication: " + entity.getClass());
        };
    }

    private Templates duplicateTemplate(Templates original) {
        Templates newTemplate = new Templates();
        newTemplate.setName(original.getName() + " - Дубликат");
        newTemplate.setDescription(original.getDescription());
        newTemplate.setVariables(original.getVariables().stream().map(Variable::new).collect(Collectors.toList()));

        Set<AbstractProductType> newProducts = original.getProductTypes().stream()
                .map(this::createTransientProductDuplicate)
                .collect(Collectors.toSet());
        newTemplate.setProductTypes(newProducts);

        return templatesRepository.save(newTemplate);
    }

    private AbstractProductType duplicateProduct(AbstractProductType original, Templates parent) {
        Templates managedParent = templatesRepository.findById(parent.getId()).orElseThrow();
        AbstractProductType newProduct = createTransientProductDuplicate(original);
        managedParent.getProductTypes().add(newProduct);
        templatesRepository.save(managedParent);
        return newProduct;
    }

    private ProductOperation duplicateOperation(ProductOperation original, AbstractProductType parent) {
        AbstractProductType managedParent = abstractProductTypeRepository.findById(parent.getId()).orElseThrow();
        ProductOperation newOperation = new ProductOperation(original);
        newOperation.setProduct(managedParent);
        newOperation.setSequence(managedParent.getProductOperations().size());
        managedParent.getProductOperations().add(newOperation);
        abstractProductTypeRepository.save(managedParent);
        return newOperation;
    }

    private AbstractProductType createTransientProductDuplicate(AbstractProductType original) {
        if (original instanceof OneSheetDigitalPrintingProductType osdpt) {
            var newProduct = new OneSheetDigitalPrintingProductType();
            newProduct.setName(osdpt.getName() + " - Копия");

            // 1. Собираем все уникальные материалы из оригинального продукта
            Set<AbstractMaterials> uniqueOriginalMaterials = new HashSet<>(osdpt.getSelectedMaterials());
            if (osdpt.getDefaultMaterial() != null) {
                uniqueOriginalMaterials.add(osdpt.getDefaultMaterial());
            }

            Set<PrintSheetsMaterial> finalMaterials = uniqueOriginalMaterials.stream()
                    .map(m -> (PrintSheetsMaterial) m)
                    .collect(Collectors.toSet());
            newProduct.setSelectedMaterials(finalMaterials);

            // 2. Устанавливаем defaultMaterial как ссылку на объект, который УЖЕ лежит в новой коллекции
            if (osdpt.getDefaultMaterial() != null) {
                finalMaterials.stream()
                        .filter(m -> m.getId().equals(osdpt.getDefaultMaterial().getId()))
                        .findFirst()
                        .ifPresent(newProduct::setDefaultMaterial);
            }

            // 3. Глубокое копирование переменных
            newProduct.setVariables(osdpt.getVariables().stream().map(Variable::new).collect(Collectors.toList()));

            // 4. Дублируем операции и исправляем их ссылки на материалы
            List<ProductOperation> duplicatedOperations = osdpt.getProductOperations().stream()
                    .map(originalOp -> {
                        ProductOperation newOp = new ProductOperation(originalOp);
                        newOp.setProduct(newProduct);

                        if (newOp.getSelectedMaterial() != null) {
                            finalMaterials.stream()
                                    .filter(m -> m.getId().equals(newOp.getSelectedMaterial().getId()))
                                    .findFirst()
                                    .ifPresent(newOp::setSelectedMaterial);
                        }
                        return newOp;
                    })
                    .collect(Collectors.toList());
            newProduct.setProductOperations(duplicatedOperations);

            return newProduct;
        }
        throw new UnsupportedOperationException("Duplication not supported for " + original.getClass().getName());
    }

    public ProductOperation addOperationToProduct(AbstractProductType product, Operation operation) {
        AbstractProductType managedProduct = abstractProductTypeRepository.findById(product.getId()).orElseThrow();
        ProductOperation newProductOperation = new ProductOperation(operation);
        newProductOperation.setProduct(managedProduct);
        newProductOperation.setSequence(managedProduct.getProductOperations().size());
        managedProduct.getProductOperations().add(newProductOperation);
        abstractProductTypeRepository.save(managedProduct);
        return newProductOperation;
    }

    public void swapProductOperations(ProductOperation current, boolean moveUp) {
        AbstractProductType product = abstractProductTypeRepository.findById(current.getProduct().getId()).orElseThrow();
        List<ProductOperation> operations = product.getProductOperations();
        int currentIndex = operations.indexOf(current);

        if ((moveUp && currentIndex <= 0) || (!moveUp && currentIndex >= operations.size() - 1)) {
            return;
        }
        int neighborIndex = moveUp ? currentIndex - 1 : currentIndex + 1;
        Collections.swap(operations, currentIndex, neighborIndex);

        for (int i = 0; i < operations.size(); i++) {
            operations.get(i).setSequence(i);
        }
        abstractProductTypeRepository.save(product);
    }
}