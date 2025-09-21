package ru.bprn.printhouse.views.price.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.price.entity.PriceOfMachine;
import ru.bprn.printhouse.views.price.repository.PriceOfMachineRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PriceOfMachineService {
    private final PriceOfMachineRepository priceOfMachineRepository;

    public List<PriceOfMachine> findAll() {
        return priceOfMachineRepository.findAll();
    }

    public List<PriceOfMachine> findByMachine(AbstractMachine machine) {
        if (machine == null) {
            return findAll();
        }
        return priceOfMachineRepository.findByMachine(machine);
    }

    public PriceOfMachine save(PriceOfMachine priceOfMachine) {
        return priceOfMachineRepository.save(priceOfMachine);
    }

    public void delete(PriceOfMachine priceOfMachine) {
        priceOfMachineRepository.delete(priceOfMachine);
    }
}