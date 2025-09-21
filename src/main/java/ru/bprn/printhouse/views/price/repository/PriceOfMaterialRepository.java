package ru.bprn.printhouse.views.price.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.price.entity.PriceOfMaterial;

import java.util.List;
import java.util.UUID;

public interface PriceOfMaterialRepository extends JpaRepository<PriceOfMaterial, UUID> {
    List<PriceOfMaterial> findByMaterial(AbstractMaterials material);
}