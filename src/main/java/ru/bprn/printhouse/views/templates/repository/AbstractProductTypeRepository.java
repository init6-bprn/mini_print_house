package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;

import java.util.UUID;

public interface AbstractProductTypeRepository extends JpaRepository<AbstractProductType, UUID> {
}