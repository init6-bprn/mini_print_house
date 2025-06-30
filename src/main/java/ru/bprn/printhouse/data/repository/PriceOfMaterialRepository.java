package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.material.entity.Material;
import ru.bprn.printhouse.data.entity.PriceOfMaterial;

public interface PriceOfMaterialRepository extends JpaRepository<PriceOfMaterial, Long> {
    PriceOfMaterial findOneByMaterial(Material material);
}
