package ru.bprn.printhouse.views.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceOfMaterialRepository extends JpaRepository<PriceOfMaterial, UUID> {
    Optional<PriceOfMaterial> findTopByMaterialAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(AbstractMaterials material, LocalDate date);

    List<PriceOfMaterial> findByMaterial(AbstractMaterials material);
}