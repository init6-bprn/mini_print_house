package ru.bprn.printhouse.views.products.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.products.entity.PriceOfMachine;
import ru.bprn.printhouse.views.products.repository.PriceOfMachineRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * Возвращает последнюю актуальную стоимость нормо-часа для указанного оборудования.
     * Если цена не найдена, возвращает 0.
     * @param machine Оборудование, для которого ищется цена.
     * @return BigDecimal цена или BigDecimal.ZERO.
     */
    public BigDecimal getActualPriceFor(AbstractMachine machine) {
        return priceOfMachineRepository.findTopByMachineAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(machine, LocalDate.now())
                .map(price -> BigDecimal.valueOf(price.getPrice()))
                .orElse(BigDecimal.ZERO);
    }
}