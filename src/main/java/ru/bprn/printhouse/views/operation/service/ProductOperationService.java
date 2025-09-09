package ru.bprn.printhouse.views.operation.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.repository.ProductOperationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductOperationService {

    private final ProductOperationRepository productOperationRepository;

    public Optional<ProductOperation> findById(UUID id) {
        return productOperationRepository.findById(id);
    }

    public List<ProductOperation> findAll() {
        return productOperationRepository.findAll();
    }

    public ProductOperation save(ProductOperation productOperation) {
        return productOperationRepository.save(productOperation);
    }

    public void delete(ProductOperation productOperation) {
        productOperationRepository.delete(productOperation);
    }

}
