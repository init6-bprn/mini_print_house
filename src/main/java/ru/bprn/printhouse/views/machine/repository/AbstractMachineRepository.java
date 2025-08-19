package ru.bprn.printhouse.views.machine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;

import java.util.UUID;

public interface AbstractMachineRepository extends JpaRepository<AbstractMachine, UUID> {
}