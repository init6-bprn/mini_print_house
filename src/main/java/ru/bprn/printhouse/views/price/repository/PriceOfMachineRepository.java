package ru.bprn.printhouse.views.price.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.price.entity.PriceOfMachine;

import java.util.List;
import java.util.UUID;

public interface PriceOfMachineRepository extends JpaRepository<PriceOfMachine, UUID> {
    List<PriceOfMachine> findByMachine(AbstractMachine machine);
}