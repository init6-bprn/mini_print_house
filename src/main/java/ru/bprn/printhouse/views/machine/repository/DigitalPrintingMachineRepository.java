package ru.bprn.printhouse.views.machine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;

import java.util.List;
import java.util.UUID;

public interface DigitalPrintingMachineRepository extends JpaRepository<DigitalPrintingMachine, UUID> {

  @Query("select c from DigitalPrintingMachine c where lower(c.name) like lower(concat('%', :searchTerm, '%'))")
  List<DigitalPrintingMachine> search(@Param("searchTerm") String searchTerm);

}