package ru.bprn.printhouse.data.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.OneSheetDigitalPrintingFlow;

@Repository
public interface OneSheetDigitalPrintingFlowRepository extends ListCrudRepository<OneSheetDigitalPrintingFlow, Long> {
}