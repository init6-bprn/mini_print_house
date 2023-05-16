package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;

import java.util.List;

public interface PrintSpeedMaterialDensityRepository extends JpaRepository<PrintSpeedMaterialDensity, Long> {

    List<PrintSpeedMaterialDensity> findPrintSpeedMaterialDensitiesByPrintMashine(PrintMashine printMashine);

}