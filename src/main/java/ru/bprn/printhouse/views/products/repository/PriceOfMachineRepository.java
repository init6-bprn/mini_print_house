package ru.bprn.printhouse.views.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.products.entity.PriceOfMachine;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceOfMachineRepository extends JpaRepository<PriceOfMachine, UUID> {
    Optional<PriceOfMachine> findTopByMachineAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(AbstractMachine machine, LocalDate date);

    List<PriceOfMachine> findByMachine(AbstractMachine machine);
}