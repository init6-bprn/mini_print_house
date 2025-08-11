package ru.bprn.printhouse.views.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;

import java.util.List;
import java.util.UUID;

public interface PrintSheetsMaterialRepository extends JpaRepository<PrintSheetsMaterial, UUID> {
    @Query("select c from PrintSheetsMaterial c where lower(c.name) like lower(concat('%', :searchTerm, '%'))")
    List<PrintSheetsMaterial> search(@Param("searchTerm") String searchTerm);
}