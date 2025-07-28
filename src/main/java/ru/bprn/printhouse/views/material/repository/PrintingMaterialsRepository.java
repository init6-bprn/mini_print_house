package ru.bprn.printhouse.views.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;

import java.util.List;
import java.util.UUID;

public interface PrintingMaterialsRepository extends JpaRepository<PrintingMaterials, UUID> {

    @Query("select c from PrintingMaterials c where lower(c.name) like lower(concat('%', :searchTerm, '%'))")
    List<PrintingMaterials> search(@Param("searchTerm") String searchTerm);
}