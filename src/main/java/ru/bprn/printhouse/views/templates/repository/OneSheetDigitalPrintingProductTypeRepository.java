package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;

import java.util.List;
import java.util.UUID;

public interface OneSheetDigitalPrintingProductTypeRepository extends JpaRepository<OneSheetDigitalPrintingProductType, UUID> {
    @Query("select c from OneSheetDigitalPrintingProductType c where lower(c.name) like lower(concat('%', :searchTerm, '%')) ")
    List<OneSheetDigitalPrintingProductType> search(@Param("searchTerm") String searchTerm);
}