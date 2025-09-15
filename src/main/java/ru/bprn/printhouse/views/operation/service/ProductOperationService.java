package ru.bprn.printhouse.views.operation.service;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.repository.ProductOperationRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProductOperationService {

    private final ProductOperationRepository repository;

    public ProductOperationService(ProductOperationRepository repository) {
        this.repository = repository;
    }

    public Optional<ProductOperation> findById(UUID id) {
        return repository.findById(id);
    }

    public ProductOperation save(ProductOperation productOperation) {
        return repository.save(productOperation);
    }

    public void delete(ProductOperation productOperation) {
        repository.delete(productOperation);
    }

    /**
     * Creates a deep copy of a ProductOperation entity.
     * The new entity is not persisted yet.
     */
    public ProductOperation duplicate(ProductOperation original) {
        return new ProductOperation(original);
    }
}