package ru.bprn.printhouse.views.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;

public interface PrintingMaterialsRepository extends JpaRepository<PrintingMaterials, Long> {
}