package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.DigitalPrintTemplate;

@Repository
public interface DigitalPrintTemplateRepository extends JpaRepository<DigitalPrintTemplate, Long> {
}
