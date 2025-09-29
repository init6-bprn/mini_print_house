package ru.bprn.printhouse.views.templates.service;

import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.material.repository.PrintSheetsMaterialRepository;
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
    private final PrintSheetsMaterialRepository printSheetsMaterialRepository;

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

    public List<Templates> findAllTemplates(String filter) {
        return (filter == null || filter.isEmpty())
                ? templatesRepository.findAll()
                : templatesRepository.search(filter).stream().toList();
    }

    @Transactional
    public Object save(Object entity, Object parent) {
        return switch (entity) {
            case Templates t -> templatesRepository.save(t);
            case AbstractProductType pt -> {
                // Если ID нет, это новый продукт, который нужно добавить к родителю
                if (pt.getId() == null && parent instanceof Templates p) {
                    Templates managedParent = templatesRepository.findById(p.getId()).orElseThrow();
                    managedParent.getProductTypes().add(pt);
                    templatesRepository.save(managedParent);
                    yield pt;
                }
                // Иначе это обновление существующего продукта
                yield abstractProductTypeRepository.save(pt); // save() выполнит merge для существующей сущности
            }
            case ProductOperation po -> {
                // Если ID нет, это новая операция, которую нужно добавить к родителю
                if (po.getId() == null && parent instanceof AbstractProductType p) {
                    // Используем перегруженный метод, который принимает уже созданный объект
                    // и правильно его сохраняет, возвращая управляемую версию.
                    yield addOperationToProduct(p, po);
                }
                // Иначе это обновление существующей операции.
                // save() вернет обновленный управляемый экземпляр.
                yield productOperationService.save(po);
            }
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
        Templates savedParent = templatesRepository.saveAndFlush(managedParent);
        // Возвращаем управляемый экземпляр из сохраненного родителя
        // Ищем по имени, так как ID может быть не самым надежным способом после flush
        return savedParent.getProductTypes().stream()
                .filter(p -> p.getName().equals(newProduct.getName()))
                .findFirst()
                .orElseThrow();
    }

    private ProductOperation duplicateOperation(ProductOperation original, AbstractProductType parent) {
        AbstractProductType managedParent = abstractProductTypeRepository.findById(parent.getId()).orElseThrow();
        ProductOperation newOperation = new ProductOperation(original);
        newOperation.setProduct(managedParent);
        newOperation.setSequence(managedParent.getProductOperations().isEmpty() ? 0 : managedParent.getProductOperations().size());
        managedParent.getProductOperations().add(newOperation); // Добавляем транзиентный объект в коллекцию
        AbstractProductType savedParent = abstractProductTypeRepository.saveAndFlush(managedParent); // Сохраняем родителя, каскадно сохраняя операцию
        // Возвращаем управляемый экземпляр из сохраненного родителя
        return savedParent.getProductOperations().get(savedParent.getProductOperations().size() - 1); // Берем последний добавленный
    }

    private AbstractProductType createTransientProductDuplicate(AbstractProductType original) {
        if (original instanceof OneSheetDigitalPrintingProductType osdpt) {
            var newProduct = new OneSheetDigitalPrintingProductType();
            newProduct.setName(osdpt.getName() + " - Копия");

            // 1. Собираем ID материалов из оригинального (detached) продукта
            Set<UUID> materialIds = osdpt.getSelectedMaterials().stream()
                    .map(AbstractMaterials::getId)
                    .collect(Collectors.toSet());

            UUID defaultMaterialId = null;
            if (osdpt.getDefaultMaterial() != null) {
                defaultMaterialId = osdpt.getDefaultMaterial().getId();
                materialIds.add(defaultMaterialId);
            }

            // 2. Загружаем управляемые (managed) экземпляры материалов из БД
            if (!materialIds.isEmpty()) {
                Set<PrintSheetsMaterial> managedMaterials = new HashSet<>(printSheetsMaterialRepository.findAllById(materialIds));
                newProduct.setSelectedMaterials(managedMaterials);

                // 3. Устанавливаем defaultMaterial как ссылку на управляемый объект из загруженной коллекции
                if (defaultMaterialId != null) {
                    UUID finalDefaultMaterialId = defaultMaterialId;
                    managedMaterials.stream()
                            .filter(m -> m.getId().equals(finalDefaultMaterialId))
                            .findFirst()
                            .ifPresent(newProduct::setDefaultMaterial);
                }
            }

            // 4. Глубокое копирование переменных
            newProduct.setVariables(osdpt.getVariables().stream().map(Variable::new).collect(Collectors.toList()));

            // 5. Дублируем операции
            List<ProductOperation> duplicatedOperations = osdpt.getProductOperations().stream()
                    .map(originalOp -> {
                        ProductOperation newOp = new ProductOperation(originalOp);
                        // Устанавливаем связь с новым (еще не сохраненным) продуктом.
                        newOp.setProduct(newProduct);
                        return newOp;
                    })
                    .collect(Collectors.toList());
            newProduct.setProductOperations(duplicatedOperations);

            return newProduct;
        }
        throw new UnsupportedOperationException("Duplication not supported for " + original.getClass().getName());
    }

    // Этот метод вызывается из меню для создания операции из ШАБЛОНА
    public ProductOperation addOperationToProduct(AbstractProductType product, Operation operation) {
        ProductOperation newProductOperation = new ProductOperation(operation);
        return addOperationToProduct(product, newProductOperation);
    }

    /**
     * Приватный метод, который содержит основную логику добавления новой ProductOperation к родителю.
     * Он гарантирует, что возвращается управляемый (managed) экземпляр с ID.
     */
    private ProductOperation addOperationToProduct(AbstractProductType product, ProductOperation newProductOperation) {
        AbstractProductType managedProduct = abstractProductTypeRepository.findById(product.getId()).orElseThrow();
        newProductOperation.setProduct(managedProduct);
        newProductOperation.setSequence(managedProduct.getProductOperations().size());
        managedProduct.getProductOperations().add(newProductOperation);
        AbstractProductType savedProduct = abstractProductTypeRepository.save(managedProduct);
        // Возвращаем последнюю добавленную операцию из СОХРАНЕННОГО родителя.
        // Теперь у нее есть ID и она является управляемой сущностью.
        return savedProduct.getProductOperations().get(savedProduct.getProductOperations().size() - 1);
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