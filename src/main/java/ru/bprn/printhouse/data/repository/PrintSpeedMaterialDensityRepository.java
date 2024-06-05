package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;

import java.util.List;
@Repository
public interface PrintSpeedMaterialDensityRepository extends JpaRepository<PrintSpeedMaterialDensity, Long> {

    List<PrintSpeedMaterialDensity> findPrintSpeedMaterialDensitiesByPrintMashine(PrintMashine printMashine);

}