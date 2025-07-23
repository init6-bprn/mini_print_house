package ru.bprn.printhouse.views.machine.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.machine.repository.DigitalPrintingMachineRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DigitalPrintingMachineService {
    private final DigitalPrintingMachineRepository repository;

    public DigitalPrintingMachineService(DigitalPrintingMachineRepository repository) {
        this.repository = repository;
    }

    public DigitalPrintingMachine save(DigitalPrintingMachine machine) {return this.repository.save(machine);}

    public List<DigitalPrintingMachine> findAll(){return this.repository.findAll();}

    public void delete(DigitalPrintingMachine machine) {this.repository.delete(machine);}

    public List<DigitalPrintingMachine> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }
    public Optional<DigitalPrintingMachine> duplicate(DigitalPrintingMachine bean) {
        if (bean!= null){
            var printingMachine = new DigitalPrintingMachine();
            printingMachine.setName(bean.getName());
            printingMachine.setGap(bean.getGap());
            printingMachine.setMaxSizeX(bean.getMaxSizeX());
            printingMachine.setMaxSizeY(bean.getMaxSizeY());
            printingMachine.setSizeOfClick(bean.getSizeOfClick());
            printingMachine.setAbstractMaterials(bean.getAbstractMaterials());
            printingMachine.setSearchStr(bean.getSearchStr());
            return Optional.of(this.repository.save(printingMachine));
        }
        else return Optional.empty();
    }
}
