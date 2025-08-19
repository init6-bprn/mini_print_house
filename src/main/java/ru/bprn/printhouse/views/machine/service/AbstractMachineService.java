package ru.bprn.printhouse.views.machine.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.machine.repository.AbstractMachineRepository;

import java.util.List;

@Service
public class AbstractMachineService {

    private final AbstractMachineRepository repository;

    public AbstractMachineService(AbstractMachineRepository repository) {
        this.repository = repository;
    }

    public List<AbstractMachine> findAll() {return repository.findAll();}


}
