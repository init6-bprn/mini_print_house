package ru.bprn.printhouse.views.machine.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.machine.repository.DigitalPrintingMachineRepository;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;
import ru.bprn.printhouse.views.material.repository.PrintingMaterialsRepository;

import java.util.*;

@Service
public class DigitalPrintingMachineService {
    private final DigitalPrintingMachineRepository repository;
    private final PrintingMaterialsRepository materialsRepository;

    public DigitalPrintingMachineService(DigitalPrintingMachineRepository repository, PrintingMaterialsRepository materialsRepository) {
        this.repository = repository;
        this.materialsRepository = materialsRepository;
    }

    @Transactional
    public DigitalPrintingMachine save(DigitalPrintingMachine machine) {
        updatePrintingMaterial(machine);
        return repository.save(machine);
    }

    private void updatePrintingMaterial(DigitalPrintingMachine newMachine) {
        DigitalPrintingMachine machine;
        if (newMachine.getId()!=null) machine = repository.findById(newMachine.getId()).orElse(newMachine);
        else machine = newMachine;

        Set<AbstractMaterials> newMaterials = new HashSet<>(newMachine.getAbstractMaterials());

        Set<AbstractMaterials> currentMaterials = machine.getAbstractMaterials();
        Set<AbstractMaterials> allMaterials = new HashSet<>();
        allMaterials.addAll(currentMaterials);
        allMaterials.addAll(newMaterials);

        for (AbstractMaterials materials : allMaterials) {
            boolean removed = currentMaterials.contains(materials) && !newMaterials.contains(materials);
            boolean added = !currentMaterials.contains(materials) && newMaterials.contains(materials);

            if (removed) materials.getAbstractMachines().remove(machine);
            if (added) materials.getAbstractMachines().add(machine);

            if (removed || added && materials instanceof PrintingMaterials)
                materialsRepository.save((PrintingMaterials) materials);
        }
    }

    public List<DigitalPrintingMachine> findAll(){return this.repository.findAll();}

    @Transactional
    public void delete(DigitalPrintingMachine machine) {
        deletePrintMaterial(machine);
        this.repository.delete(machine);
    }

    private void deletePrintMaterial(DigitalPrintingMachine machine) {
        Set<AbstractMaterials> allMaterials = new HashSet<>(machine.getAbstractMaterials());
        for (AbstractMaterials material:allMaterials) {
            material.getAbstractMachines().remove(machine);
            materialsRepository.save((PrintingMaterials) material);
        }
    }

    public List<DigitalPrintingMachine> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }

    public void duplicate(DigitalPrintingMachine bean) {
        if (bean!= null){
            var printingMachine = new DigitalPrintingMachine();
            printingMachine.setName("Дубликат "+bean.getName());
            printingMachine.setGap(bean.getGap());
            printingMachine.setMaxSizeX(bean.getMaxSizeX());
            printingMachine.setMaxSizeY(bean.getMaxSizeY());
            printingMachine.setSizeOfClick(bean.getSizeOfClick());
            printingMachine.setSearchStr(bean.getSearchStr());
            this.repository.saveAndFlush(printingMachine);
        }
    }
}
