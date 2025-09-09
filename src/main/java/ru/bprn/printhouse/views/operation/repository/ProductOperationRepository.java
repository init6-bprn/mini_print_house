package ru.bprn.printhouse.views.operation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;

import java.util.UUID;

public interface ProductOperationRepository extends JpaRepository<ProductOperation, UUID> {
}