package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;

public interface PrintSpeedMaterialDensityRepository extends JpaRepository<PrintSpeedMaterialDensity, Long> {
}