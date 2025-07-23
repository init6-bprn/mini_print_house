package ru.bprn.printhouse.views.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;

import java.util.UUID;

public interface PrintSheetsMaterialRepository extends JpaRepository<PrintSheetsMaterial, UUID> {
}